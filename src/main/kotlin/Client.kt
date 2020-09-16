package s

import java.net.Socket
import java.util.*

class Client (val socket: Socket){

    private val output = socket.outputStream
    private val inputStream = socket.inputStream
    val ip = socket.inetAddress!!

    fun getReceivedCommand(getJustFirstLine: Boolean = true): String {
        val input = Scanner(inputStream)
        var command = ""

        if (getJustFirstLine) {
            if (input.hasNextLine()) {
                command = input.nextLine()
            }
        } else {
            if (input.hasNextLine()) {
                command += input.nextLine()
            }
        }

        return command
    }

    fun sendFeedback(message: String?): Boolean {
        return try {
            output.write("$message\n".toByteArray())
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        try {
            socket.close()
        } catch (e: Exception) {
            print("")
        }
    }
}