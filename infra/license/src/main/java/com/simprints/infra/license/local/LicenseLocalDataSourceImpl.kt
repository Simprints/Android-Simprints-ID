package com.simprints.infra.license.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.simprints.core.DispatcherIO
import com.simprints.infra.license.local.LicenseLocalDataSource.Companion.LICENSES_FOLDER
import com.simprints.infra.license.local.LicenseLocalDataSource.Companion.LICENSE_NAME
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class LicenseLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyHelper: SecurityManager,
    @DispatcherIO private val dispatcherIo: CoroutineDispatcher,
) : LicenseLocalDataSource {

    private val licensePath = "${context.filesDir}/${LICENSES_FOLDER}/${LICENSE_NAME}"

    init {
        createDirectoryIfNonExistent(licensePath)
    }

    override suspend fun getLicense(): String? = withContext(dispatcherIo) {
        getFileFromStorage() ?: getFileFromAssets()
    }

    override suspend fun saveLicense(license: String): Unit = withContext(dispatcherIo) {
        createDirectoryIfNonExistent(licensePath)

        val file = File(licensePath)

        try {
            keyHelper.getEncryptedFileBuilder(file, context).openFileOutput()
                .use { it.write(license.toByteArray()) }
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun createDirectoryIfNonExistent(path: String) {
        val file = File(path)
        val fileName = file.name
        val directory = File(path.replace(fileName, ""))

        if (!directory.exists())
            directory.mkdirs()
    }

    @VisibleForTesting
    fun getFileFromAssets(): String? = try {
        context.assets.open(LICENSE_NAME).use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

    override suspend fun deleteCachedLicense(): Unit = withContext(dispatcherIo) {
        try {
            val deleted = File(licensePath).delete()
            Simber.d("Deleted cached licenses successfully = $deleted")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun getFileFromStorage(): String? = try {
        val file = File(licensePath)
        val encryptedFile = keyHelper.getEncryptedFileBuilder(file, context)
        encryptedFile.openFileInput().use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

}
