package com.simprints.id.tools.utils

import java.io.BufferedReader
import java.io.File

object FileUtil {

    fun createDirectory(filePath: String) = File(filePath)

    fun createFile(filePath: String, fileName: String) =
        File(filePath, fileName)

    fun exists(filePath: String, fileName: String): Boolean =
        createFile(filePath, fileName).exists()

    fun readFile(file: File): BufferedReader = file.bufferedReader()

}
