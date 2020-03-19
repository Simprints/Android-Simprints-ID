package com.simprints.id.data.consent.longconsent

import com.simprints.id.data.consent.longconsent.LongConsentRepositoryImpl.Companion.DEFAULT_LANGUAGE
import com.simprints.id.data.consent.longconsent.LongConsentRepositoryImpl.Companion.FILE_PATH
import com.simprints.id.data.consent.longconsent.LongConsentRepositoryImpl.Companion.FILE_TYPE
import com.simprints.id.data.loginInfo.LoginInfoManager
import java.io.BufferedReader
import java.io.File

class LongConsentLocalDataSourceImpl(absolutePath: String,
                                     val loginInfoManager: LoginInfoManager) : LongConsentLocalDataSource {

    private val baseFilePath: File by lazy {
        createBaseFilePath(absolutePath)
    }

    private val filePathForProject: File by lazy {
        createLocalFilePath(baseFilePath.absolutePath)
    }

    override fun isLongConsentPresentInLocal(language: String): Boolean {
        val fileName = if (language.isEmpty()) {
            "${DEFAULT_LANGUAGE}.${FILE_TYPE}"
        } else {
            "$language.${FILE_TYPE}"
        }

        return File(filePathForProject, fileName).exists()
    }

    override fun createFileForLanguage(language: String)  =
        File(filePathForProject, "$language.$FILE_TYPE")

    private fun createBaseFilePath(absolutePath: String) = File (absolutePath +
        File.separator +
        FILE_PATH)

    private fun createLocalFilePath(absolutePath: String): File {
        val filePath = File(absolutePath +
            File.separator +
            loginInfoManager.getSignedInProjectIdOrEmpty())

        if (!filePath.exists()) {
            filePath.mkdirs()
        }

        return filePath
    }

    override fun deleteLongConsents() {
        getAllLongConsentFiles()?.forEach { baseFile ->
            if(baseFile.isDirectory) {
                deleteFilesInDirectory(baseFile)
            }
            baseFile.delete()
        }
    }

    private fun getAllLongConsentFiles() =
        baseFilePath.listFiles()

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
