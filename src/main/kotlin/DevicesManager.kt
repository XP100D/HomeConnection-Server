package s

import com.google.gson.Gson
import java.io.File
import java.io.PrintStream
import java.lang.StringBuilder
import java.util.*

class DevicesManager : DevicesFolder() {

    companion object {
        private const val IP_STRING = "ip"
        private const val NAME_STRING = "name"
        private const val PORT_STRING = "port"
    }

    fun authDevice(data: String, deviceIp: String) : Device? {
        val list = separateArguments(data, "-".toRegex())
        val ip = separateArguments(deviceIp,"/".toRegex()).last()

        if(list.size > 2) {
            for (property in list) {
                val deviceName = list[1]
                val port = parseToInt(list[2])
                val device = Device(ip, port, deviceName)
                device.macAddress = list[0]
                if (createDeviceFile(device)) {
                    return device
                }
            }
        }
        return null
    }

    /** String format >>  {json object content}={other json object content}=  << */
    fun allDevicesInJsonString() : String {
        val devicesInJson = StringBuilder()

        if(devicesDir.exists()) {
            val deviceslist = devicesDir.listFiles()!!
            val separatorLimit = deviceslist.size - 1

            for((currentNumberOfObjectSeparators, deviceFile) in deviceslist.withIndex()) {
                val newJsonObjectInString = readDeviceFile(deviceFile)
                devicesInJson.append(newJsonObjectInString)

                if (currentNumberOfObjectSeparators < separatorLimit) {
                    devicesInJson.append("=")
                }
            }
        }

        return devicesInJson.toString()
    }

    /**
     * Reads the devices file in the data directoryReads the directory that contains the device files, instantiate each device and add it to a map. Returns a map of devices
     * Lê o diretório que contem os arquivos de dispositivos, instancia cada Dispositivo e o adiciona em um mapa. Retorna um mapa de dispositivos
     *
     * The map content: key is the device ip and the value is the instance of the device that has this ip
     */

    fun listDevices(): MutableMap<String, Device> {
        val devicesMap = mutableMapOf<String, Device>()

        if (devicesDir.exists()) {
            val listOfDevicesFile = devicesDir.listFiles()

            if (listOfDevicesFile != null) {
                for (file in listOfDevicesFile) {
                    val device = Gson().fromJson(readDeviceFile(file), Device::class.java)
                    if (device != null) {
                        devicesMap[device.ip] = device
                    }
                }
            }
        }

        return devicesMap
    }

    private fun createDeviceFile(device: Device) : Boolean {
        val formattedMacAddress = changeBytesSeparatorOnMacAddressName(device.macAddress)
        //val text = "$IP_STRING=${device.ip}\n$NAME_STRING=${device.name}\n$PORT_STRING=${device.port}"
        val text = createJsonString(device)

        val deviceFile = File(devicesDir, "$formattedMacAddress.json")
        if (deviceFile.exists()) {
            deviceFile.delete()
            if(deviceFile.createNewFile()) {
                writeOnDeviceFile(deviceFile, text)
                return true
            }
        } else {
            if(deviceFile.createNewFile()) {
                writeOnDeviceFile(deviceFile, text)
                return true
            }
        }
        return false
    }

    private fun writeOnDeviceFile(file: File, text: String) {
        if (file.exists() && file.canWrite()) {
            val writer = PrintStream(file)

            writer.println(text)
            writer.flush()
            writer.close()
        }
    }

    private fun createJsonString(device: Device): String {
        val string = StringBuilder()
        string.append("{\n")
        string.append("\t\"${IP_STRING}\": \"${device.ip}\",\n")
        string.append("\t\"${NAME_STRING}\": \"${device.name}\",\n")
        string.append("\t\"${PORT_STRING}\": \"${device.port}\"\n")
        string.append("}")

        val gson = Gson()
        val obj: Device = gson.fromJson(string.toString(), Device::class.java)
        println(obj.toString())

        return string.toString()
    }

    /**
     * Returns all content of the device in a string
     */
    private fun readDeviceFile(deviceFile: File) : String {
        val text = StringBuilder()

        if (deviceFile.exists() && deviceFile.canRead()) {
            val scan = Scanner(deviceFile)

            while (scan.hasNextLine()) {
                text.append(scan.nextLine())
            }
        }
        return text.toString()
    }

    private fun changeBytesSeparatorOnMacAddressName(macAddress: String, old: String = ":", new: String = "-"):String {
        return macAddress.replace(old,new)
    }
}