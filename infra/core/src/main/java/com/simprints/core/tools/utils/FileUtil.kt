package com.simprints.core.tools.utils

import java.io.File

object FileUtil {
    private fun createFile(
        filePath: String,
        fileName: String,
    ) = File(filePath, fileName)

    fun createFile(
        parent: File,
        child: String,
    ) = File(parent, child)

    fun exists(
        filePath: String,
        fileName: String,
    ): Boolean = createFile(filePath, fileName).exists()

    fun readBytes(file: File) = file.readBytes()

    fun writeBytes(
        file: File,
        bytes: ByteArray,
    ) = file.writeBytes(bytes)
}
