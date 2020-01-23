package com.simprints.id.data.db.image.local

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
import androidx.security.crypto.MasterKeys
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import com.simprints.core.images.Path
import com.simprints.core.images.SecuredImageRef
import timber.log.Timber
import java.io.File
import java.io.FileInputStream

class ImageLocalDataSourceImpl(private val ctx: Context) : ImageLocalDataSource {

    companion object {
        private const val IMAGES_FOLDER = "images"
    }

    private val imageRootPath = "${ctx.filesDir}/$IMAGES_FOLDER"

    init {
        val imageFolder = File(imageRootPath)
        if (!imageFolder.exists()) {
            imageFolder.mkdir()
        }
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(AES256_GCM_SPEC)

    override fun encryptAndStoreImage(
        imageBytes: ByteArray,
        subDirs: Path,
        fileName: String
    ): SecuredImageRef? {
        val path = Path.join(imageRootPath, subDirs).compose()

        createDirectoryIfNonExistent(path)

        val file = File(path, fileName)
        Timber.d(file.absoluteFile.toString())

        return try {
            getEncryptedFile(file).openFileOutput().use { stream ->
                stream.write(imageBytes)
                SecuredImageRef(file.absolutePath)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        } finally {
            file.delete()
        }
    }

    override fun decryptImage(image: SecuredImageRef): FileInputStream? {
        val file = File(image.path)
        val encryptedFile = getEncryptedFile(file)
        return try {
            encryptedFile.openFileInput()
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override fun listImages(): List<SecuredImageRef> {
        val imageRoot = File(imageRootPath)
        return imageRoot.walk()
            .filterNot { it.isDirectory }
            .map { SecuredImageRef(it.absolutePath) }
            .toList()
    }

    override fun deleteImage(image: SecuredImageRef): Boolean {
        val file = File(image.path)
        return file.delete()
    }

    private fun createDirectoryIfNonExistent(path: String) {
        val directory = File(path)

        if (!directory.exists())
            directory.mkdirs()
    }

    private fun getEncryptedFile(file: File): EncryptedFile =
        EncryptedFile.Builder(
            file,
            ctx,
            masterKeyAlias,
            AES256_GCM_HKDF_4KB
        ).build()

}
