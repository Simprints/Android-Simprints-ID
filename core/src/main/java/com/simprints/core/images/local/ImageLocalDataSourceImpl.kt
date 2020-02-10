package com.simprints.core.images.local

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
import androidx.security.crypto.MasterKeys
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import com.simprints.core.images.model.Path
import com.simprints.core.images.model.SecuredImageRef
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

internal class ImageLocalDataSourceImpl(private val ctx: Context) : ImageLocalDataSource {

    private val imageRootPath = "${ctx.filesDir}/$IMAGES_FOLDER"

    init {
        createDirectoryIfNonExistent(imageRootPath)
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(AES256_GCM_SPEC)

    override fun encryptAndStoreImage(imageBytes: ByteArray, relativePath: Path): SecuredImageRef? {
        val fullPath = Path.combine(imageRootPath, relativePath).compose()

        createDirectoryIfNonExistent(fullPath)

        val file = File(fullPath)
        Timber.d(file.absoluteFile.toString())

        return try {
            if (relativePath.compose().isEmpty())
                throw FileNotFoundException()

            getEncryptedFile(file).openFileOutput().use { stream ->
                stream.write(imageBytes)
                SecuredImageRef(Path.parse(file.absolutePath))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override fun decryptImage(image: SecuredImageRef): FileInputStream? {
        val absolutePath = buildAbsolutePath(image.relativePath)
        val file = File(absolutePath)
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
            .map { file ->
                val subsetToRemove = Path.parse(imageRootPath)
                val path = Path.parse(file.path).remove(subsetToRemove)
                SecuredImageRef(path)
            }
            .toList()
    }

    override fun deleteImage(image: SecuredImageRef): Boolean {
        val absolutePath = buildAbsolutePath(image.relativePath)
        val file = File(absolutePath)
        return file.delete()
    }

    private fun createDirectoryIfNonExistent(path: String) {
        val file = File(path)
        val fileName = file.name
        val directory = File(path.replace(fileName, ""))

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

    private fun buildAbsolutePath(path: Path): String {
        return Path.combine(imageRootPath, path).compose()
    }

    private companion object {
        const val IMAGES_FOLDER = "images"
    }

}
