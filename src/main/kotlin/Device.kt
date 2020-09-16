package s

import java.net.Socket
import java.io.InputStream
import java.util.Scanner
import java.net.InetSocketAddress

class Device (val ip: String, val port: Int = 80, var name: String = "") {

    var statOfDevice: String? = null
    var macAddress = ""

    //Static Property in Kotlin
    companion object {
        @JvmStatic val REGISTER_CODE: String = "2510"
        @JvmStatic val AUTHENTICATION_OK = "itsOk"
        @JvmStatic val COMMAND_STATUS = "status"
        @JvmStatic val COMMAND_UNLOCK = "unlock"
        @JvmStatic val COMMAND_TURN_ON = "turnOn"
        @JvmStatic val COMMAND_TURN_OFF = "turnOff"
        @JvmStatic val INVALID_COMMAND = "INVALID_COMMAND"
        @JvmStatic val COMMAND_RETURN_MAC_ADDRESS = "returnMacAddress"
        @JvmStatic val COMMAND_REBOOT = "rebootThis"

    }

    private fun connect(ip: String, port: Int): Socket? {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port))
            socket.soTimeout = 3000
            socket
        } catch(e: Exception) {
            null
        }
    }

    fun sendCommand(command: String, isToGetName: Boolean = false): Boolean {
        val socket = connect(this.ip, this.port)

        return if(socket != null && socket.isConnected) {

            val output = socket.outputStream
            val inputStream = socket.inputStream

            receiveReturnAndChangeDeviceStat(inputStream, isToGetName)
            output.write("$command\n".toByteArray())
            receiveReturnAndChangeDeviceStat(inputStream, isToGetName)


            socket.close()

            true
        } else false
    }

    // Returns the device's status. If there is a problem with the communication, it'll return null.
    // Retorna o status do dispositivo. Se houver algum problema com a comunicação, ela retornará nula.
    private fun receiveReturnAndChangeDeviceStat(inputStream: InputStream?, isToSetName: Boolean = false) {
        val input = Scanner(inputStream!!)

        if(input.hasNextLine()) {
            if(!(isToSetName)) {
                this.statOfDevice = input.nextLine()
            } else {
                this.name = input.nextLine()
            }
        }
    }

    override fun toString(): String {
       super.toString()

        val text = StringBuilder()
        text.append("IP: ${this.ip}\n")
        text.append("NAME: ${this.name}\n")
        text.append("PORT: ${this.port}\n")
        text.append("STATUS: ${this.statOfDevice}\n")

        return text.toString()
    }
}