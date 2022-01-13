package com.simprints.id.data.consent.longconsent.local

import androidx.annotation.VisibleForTesting
import com.simprints.core.login.LoginInfoManager
import java.io.BufferedReader
import java.io.File

class LongConsentLocalDataSourceImpl(
    absolutePath: String,
    private val loginInfoManager: LoginInfoManager
) : LongConsentLocalDataSource {

    companion object {
        const val FOLDER = "long-consents"
        const val FILE_TYPE = "txt"
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val baseFilePath: File by lazy {
        createBaseFilePath(absolutePath)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val projectFilePath: File by lazy {
        createLocalFilePath(baseFilePath.absolutePath)
    }

    override fun isLongConsentPresentInLocal(language: String): Boolean {
        val fileName = "$language.${FILE_TYPE}"
        return File(projectFilePath, fileName).exists()
    }

    override fun createFileForLanguage(language: String) =
        File(projectFilePath, "$language.$FILE_TYPE")

    private fun createBaseFilePath(absolutePath: String) =
        File(absolutePath + File.separator + FOLDER)

    private fun createLocalFilePath(absolutePath: String): File {
        val pathName = absolutePath + File.separator + loginInfoManager.getSignedInProjectIdOrEmpty()
        val file = File(pathName)

        if (!file.exists()) {
            file.mkdirs()
        }

        return file
    }

    override fun deleteLongConsents() {
        baseFilePath.listFiles()?.forEach { baseFile ->
            if (baseFile.isDirectory) {
                deleteFilesInDirectory(baseFile)
            }
            baseFile.delete()
        }
    }

    private fun deleteFilesInDirectory(baseFile: File) {
        baseFile.listFiles().forEach { it.delete() }
    }

    override fun getLongConsentText(language: String) =
        if (isLongConsentPresentInLocal(language)) {
            val br: BufferedReader = createFileForLanguage(language).bufferedReader()
            val fileContent = StringBuffer("")

            br.forEachLine {
                fileContent.append(it + "\n")
            }

            fileContent.toString()
        } else {
            ""
        }
}
