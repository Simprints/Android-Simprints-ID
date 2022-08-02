package com.simprints.id.data.consent.longconsent.local

import androidx.annotation.VisibleForTesting
import com.simprints.id.tools.utils.FileUtil
import com.simprints.infra.login.LoginManager
import java.io.BufferedReader
import java.io.File

class LongConsentLocalDataSourceImpl(
    absolutePath: String,
    private val loginManager: LoginManager,
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
        return FileUtil.exists(projectFilePath.path, fileName)
    }

    override fun createFileForLanguage(language: String): File {
        val fileName = "$language.${FILE_TYPE}"
        return FileUtil.createFile(projectFilePath.path, fileName)
    }

    private fun createBaseFilePath(absolutePath: String): File {
        val filePath = absolutePath + File.separator + FOLDER
        return FileUtil.createDirectory(filePath)
    }

    private fun createLocalFilePath(absolutePath: String): File {
        val filePath = absolutePath + File.separator + loginManager.getSignedInProjectIdOrEmpty()
        val file = FileUtil.createDirectory(filePath)

        if (!file.exists()) {
            file.mkdirs()
        }

        return file
    }

    override fun deleteLongConsents() {
        getLongConsentFiles()?.forEach { baseFile ->
            if (baseFile.isDirectory) {
                deleteFilesInDirectory(baseFile)
            }
            baseFile.delete()
        }
    }

    private fun getLongConsentFiles() = baseFilePath.listFiles()

    private fun deleteFilesInDirectory(baseFile: File) {
        baseFile.listFiles()?.forEach { it.delete() }
    }

    override fun getLongConsentText(language: String) =
        if (isLongConsentPresentInLocal(language)) {
            val file = createFileForLanguage(language)
            val br: BufferedReader = FileUtil.readFile(file)
            val fileContent = StringBuffer("")

            br.forEachLine {
                fileContent.append(it + "\n")
            }

            fileContent.toString()
        } else {
            ""
        }
}
