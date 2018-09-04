package com.simprints.id.tools

import android.os.Environment
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FileLoggingTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        try {

            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val direct = File("$path/SimprintsLogs")

            if (!direct.exists()) {
                direct.mkdir()
            }

            val fileNameTimeStamp = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val logTimeStamp = SimpleDateFormat("E MMM dd yyyy 'at' hh:mm:ss:SSS aaa", Locale.getDefault()).format(Date())

            val file = File("$path/YoScholarDeliveryLogs${File.separator}$fileNameTimeStamp.html")

            file.createNewFile()

            if (file.exists()) {

                val fileOutputStream = FileOutputStream(file, true)

                fileOutputStream.write("<p style=\"background:lightgray;\"><strong style=\"background:lightblue;\">&nbsp&nbsp$logTimeStamp :&nbsp&nbsp</strong>&nbsp&nbsp$message</p>".toByteArray())
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error while logging into file : $e")
        }
    }

    companion object {

        private val TAG = FileLoggingTree::class.java.simpleName
    }
}
