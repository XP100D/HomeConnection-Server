/**
 * Developer: Natanael S. dos Anjos
 *
 *
 * This software receives TCP / IP requests and fetches the requested device from the database itself.
 * The message received from the Client is transmitted to the device.
 *
 * Each client request creates a new Thread that processes the String received command with format:
 *                         [targetIpDevice]=[commandForTargetDevice]
 *                 or this format to the device register on server database:
 *          [macAddressOnStringFormat]-[nameOfDevice]-[portToConnectWithDeviceLater]
 *
 * If the MAC address format is detected in data entry, the client will be processed as a device that tries to register with the server.
 * But only the MAC address in String format in the first position of the input text with two more parameters will be accepted as a valid device.
 * In case of the third parameter isn't the correct number of connection port with device, this server will not able to connect with it in future.
 */

package s

import java.net.ServerSocket
import java.lang.Thread
import java.net.InetSocketAddress
import kotlin.concurrent.thread
import s.getConfigValue as getConfigValue

val deviceManager = DevicesManager()
var mapOfDevices = mutableMapOf<String, Device>()

fun main() {
    lateinit var server: ServerSocket

    //Try start Server
    while(true) {
        val mainPort: Int  = getConfigValue("PORT")
        val serverTmp = startServer(mainPort)
        if(serverTmp != null) {
            server = serverTmp
            mapOfDevices = deviceManager.listDevices()
            break
        }
    }

    while(true) {
        val socketClient = Client(server.accept())
        doIt(socketClient)
    }
}

fun doIt(socketClient: Client) {

    thread {
        val textReceived = socketClient.getReceivedCommand()

        //Checks whether the connected client is a device requesting registration with the server.
        val macAddress = catchMacAddress(separateArguments(textReceived, "-".toRegex()))

        if (macAddress.isNullOrEmpty()) {
            val deviceIpAndCommand: List<String> = separateDestinationAndCommand(textReceived)

            if (deviceIpAndCommand.size > 1) {
                val ip = deviceIpAndCommand[0]
                val commandReceivedFromClient = deviceIpAndCommand[1]

                val device = ipExists(ip) //returns the device's name if it exists on server database

                if (device != null) {
                    if (device.sendCommand(commandReceivedFromClient) && device.statOfDevice != null) {
                        socketClient.sendFeedback(device.statOfDevice)
                        if (commandReceivedFromClient != Device.COMMAND_STATUS && device.statOfDevice != Device.INVALID_COMMAND) {
                            println("${dateAndTime()} ${device.name} is ${device.statOfDevice} now")

                        } else if (device.statOfDevice == Device.INVALID_COMMAND) {
                            socketClient.sendFeedback(Device.INVALID_COMMAND)
                            println("${dateAndTime()} $device doesn't have ${commandReceivedFromClient.toUpperCase()} command")
                        } else {
                            print("")
                        }

                    } else {
                        socketClient.sendFeedback("DISCONNECTED")
                        println("${dateAndTime()} ${device.name} is not connected on server")
                    }

                } else {
                    socketClient.sendFeedback("UNKNOW_DEVICE")
                    println("${dateAndTime()} Device ($ip) doesn't exist in the Server Device List\n \t\t\t command ${commandReceivedFromClient.toUpperCase()} solicited by ${socketClient.ip}")
                }

            } else {
                socketClient.disconnect()
            }
        } else {
            val deviceIp = socketClient.socket.inetAddress.toString()
            val newDevice = deviceManager.authDevice(textReceived,deviceIp)
            if(newDevice != null) {
                mapOfDevices[newDevice.ip] = newDevice
                println("${dateAndTime()} Device (${newDevice.name}:${newDevice.ip}) has been registered")
            }
        }
    }
}

fun ipExists(textIp: String): Device? {
    return mapOfDevices[textIp]
}

//separates the target device and the command from the input text.
fun separateDestinationAndCommand(input: String): List<String> {
    return input.split("=")
}

fun getConfigValue(nameOfConfig: String): Int {
    val configFile =  SettingsFile()
    val configFileContentMap = configFile.readConfigFile()

    return configFileContentMap[nameOfConfig] ?: 0
}

fun startServer(port: Int): ServerSocket? {
    val serverSocket = ServerSocket()
    serverSocket.setPerformancePreferences(2, 1, 0)


    return try {
        serverSocket.bind(InetSocketAddress(port))
        println("${dateAndTime()} Server started at port: $port ")
        serverSocket
    }
    catch(e: Exception) {
        val date = dateAndTime()
        println("\t----------ERROR STARTING SERVER AT PORT $port----------")
        println("\t                ---$date---")
        println()
        Thread.sleep(5000)
        null
    }
}