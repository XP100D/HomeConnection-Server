package s

import java.text.SimpleDateFormat
import java.util.*

fun timer(runWhenEndTime: Runnable, secondsInFuture: Int) {
    val date = Date()
    val initTime = date.time
    val timeInFuture = initTime+(secondsInFuture*1000)

    while(true) {
        if (timeInFuture >= System.currentTimeMillis()) {
            Thread.sleep(1000)
            println("$timeInFuture - ${System.currentTimeMillis()}")
        } else {
            runWhenEndTime.run()
            break
        }
    }
}
fun parseToInt(text: String?): Int {
    return try {
        if (text.isNullOrEmpty()) {
            0
        } else {
            val number = text.toInt()
            number
        }
    } catch(e: Exception) {
        0
    }
}

fun dateAndTime(): String {
    val millisInLong = System.currentTimeMillis()
    val simpleDateFormat = SimpleDateFormat("dd/MM/yy - HH:mm:ss |")
    return simpleDateFormat.format(millisInLong)
}

fun separateArguments(textToSeparate: String, delimiter: Regex = Regex("=")) : List<String> {
    return textToSeparate.split(delimiter)
}

fun catchMacAddress(list: List<String>? = null ) : String? {
    list?.forEach {
        if (isMacAddress(it)) {
            return it
        }
    }
    return null
}

private fun isMacAddress(text: String) : Boolean{
    val regex = "^(([0-9a-fA-F]{2}):){5}([0-9a-fA-F]{2})$".toRegex()
    return regex.matches(text)
}


