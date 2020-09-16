/**
 * Directories:
 *  The server's main directory will be created with the (Server) name in the home directory of the user who started the software.
 *  Others directories will be create inside the main folder
 *
 *  Devices:
 *      This folder stores files with data from registered devices. Your content doesn't need to be edited manually.
 *  Settings:
 *      A directory that contains the server settings such as the port number.
 *
 */

package s

import java.util.Scanner
import java.io.PrintStream
import java.io.File
import java.lang.System

private val mainPath: String = System.getProperty("user.home") + File.separator + "Server"

interface MainFolder {

    // Creates the program's main directory if it doesn't already exist.
    // Cria o diretório principal do programa caso não exista.
    fun serverFolder(path: String = mainPath): File? {
        val serverDir = File(path)
        if(!(serverDir.exists())) {
            try {
                serverDir.mkdirs()
            }
            catch(e: Exception) {
                return null
            }  
        }
        return serverDir
    }
}
/*----------------------------------------------------Device-File--------------------------------------------------------------------------*/
abstract class DevicesFolder : MainFolder {

    private val devicesDirName = "devices"
    protected val devicesDir = File(serverFolder(), devicesDirName)

    init {
        devicesDir.mkdirs()
    }
}   

/*-----------------------------------------------------Config-File------------------------------------------------------------------------ */
class SettingsFile : MainFolder {
    private val serverDirectory = serverFolder()
    private val directorySettingsName = "settings"
    private val archiveSettingsName = "serverMainSettings.txt"
    private var archiveSettingsObject: File? = null

    init {

        if (serverDirectory != null) {
            archiveSettingsObject = createServerConfigurationArchive(createConfigDirectory(serverDirectory))
        }

    }

    private fun createConfigDirectory(parentFile: File) : File {
        val settingsDirectory = File(parentFile, directorySettingsName)
        settingsDirectory.mkdirs()
        return settingsDirectory
    }

    // If the configuration file doesn't exist, it'll be created, and then, the default value of PORT will be write on file.
    // Se o arquivo de configuração não existir, ele será criado e, em seguida, o valor padrão da porta será escrito no arquivo.
    private fun createServerConfigurationArchive(parentFile: File): File {
       val serverConfigurationArchive = File(parentFile, archiveSettingsName)

        if(serverConfigurationArchive.createNewFile()) {
            val writer = PrintStream(serverConfigurationArchive)
            writer.println("PORT:")

            writer.flush()
            writer.close()
        }
        return serverConfigurationArchive
    }

    fun readConfigFile() : MutableMap<String, Int> {
            val fileContent = mutableMapOf<String, Int>()

            if(archiveSettingsObject != null) {
                val input = Scanner(archiveSettingsObject!!)

                while (input.hasNext()) {
                    val line = separateArguments(input.nextLine())
                    val argument = line[0]
                    val value = parseToInt(line[1])
                    fileContent[argument] = value
                }
            }
            return fileContent
    }

    private fun separateArguments(lineText: String): List<String> = lineText.split(":")

}