package com.simprints.id.core.images

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
import androidx.security.crypto.MasterKeys
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import com.simprints.core.images.SecuredImageManager
import com.simprints.core.images.SecuredImageRef
import timber.log.Timber
import java.io.File
import java.io.FileInputStream


class SecuredImageManagerImpl(val ctx: Context,
                              private val hasher: Hasher = Hasher()) : SecuredImageManager {

    companion object {
        private const val IMAGES_FOLDER = "images"
    }

    private val imageFolderPath = "${ctx.filesDir}/$IMAGES_FOLDER"

    init {
        val imageFolder = File(imageFolderPath)
        if (!imageFolder.exists()) {
            imageFolder.mkdir()
        }
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(AES256_GCM_SPEC)

    override fun storeImageForEnrol(image: ByteArray, template: ByteArray): SecuredImageRef? {
        return storeImage(image, hasher.hash(template.toString()))
    }

    override fun storeImage(imageBytes: ByteArray, filename: String): SecuredImageRef? {
        val file = File(imageFolderPath, filename)
        Timber.d(file.absoluteFile.toString())

        file.deleteOnExit()

        return try {
            getEncryptedFile(file).openFileOutput().use { stream ->
                stream.write(imageBytes)
                SecuredImageRef(file.absolutePath)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override fun readImage(path: SecuredImageRef): FileInputStream? {
        val file = File(path.path)
        val encryptedFile = getEncryptedFile(file)
        return try {
            encryptedFile.openFileInput()
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override fun listImages(): List<SecuredImageRef> {
        val imageFolder = File(imageFolderPath)
        return imageFolder.listFiles().map {
            SecuredImageRef(it.absolutePath)
        }
    }

    override fun deleteImage(path: SecuredImageRef): Boolean {
        val file = File(path.path)
        return file.delete()
    }

    private fun getEncryptedFile(file: File): EncryptedFile =
        EncryptedFile.Builder(
            file,
            ctx,
            masterKeyAlias,
            AES256_GCM_HKDF_4KB
        ).build()

}
