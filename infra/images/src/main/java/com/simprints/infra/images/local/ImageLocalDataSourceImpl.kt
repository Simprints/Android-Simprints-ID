package com.simprints.infra.images.local

import android.content.Context
import com.simprints.core.DispatcherIO
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.inject.Inject

internal class ImageLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val keyHelper: SecurityManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : ImageLocalDataSource {
    private val imageRootPath = "${ctx.filesDir}/$IMAGES_FOLDER"

    init {
        createDirectoryIfNonExistent(imageRootPath)
    }

    override suspend fun encryptAndStoreImage(
        imageBytes: ByteArray,
        projectId: String,
        relativePath: Path,
    ): SecuredImageRef? = withContext(dispatcher) {
        val fullPath = Path.combine(buildProjectPath(projectId), relativePath).compose()

        createDirectoryIfNonExistent(fullPath)

        val file = File(fullPath)
        Simber.d(file.absoluteFile.toString())

        try {
            if (relativePath.compose().isEmpty()) {
                throw FileNotFoundException()
            }

            keyHelper.getEncryptedFileBuilder(file, ctx).openFileOutput().use { stream ->
                stream.write(imageBytes)
                val subsetToRemove = Path.parse(imageRootPath)
                val path = Path.parse(file.path).remove(subsetToRemove)
                SecuredImageRef(path)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override suspend fun decryptImage(image: SecuredImageRef): FileInputStream? = withContext(dispatcher) {
        val absolutePath = buildAbsolutePath(image.relativePath)
        val file = File(absolutePath)
        val encryptedFile = keyHelper.getEncryptedFileBuilder(file, ctx)
        try {
            encryptedFile.openFileInput()
        } catch (t: Throwable) {
            Simber.i("Image decryption failed", t)
            null
        }
    }

    override suspend fun listImages(projectId: String?): List<SecuredImageRef> = withContext(dispatcher) {
        val imageRoot =
            if (projectId == null) File(imageRootPath) else File(buildProjectPath(projectId))

        imageRoot
            .walk()
            .filterNot { it.isDirectory }
            .map { file ->
                val subsetToRemove = Path.parse(imageRootPath)
                val path = Path.parse(file.path).remove(subsetToRemove)
                SecuredImageRef(path)
            }.toList()
    }

    override suspend fun deleteImage(image: SecuredImageRef): Boolean = withContext(dispatcher) {
        val absolutePath = buildAbsolutePath(image.relativePath)
        val file = File(absolutePath)
        file.delete()
    }

    private fun createDirectoryIfNonExistent(path: String) {
        val file = File(path)
        val fileName = file.name
        val directory = File(path.replace(fileName, ""))

        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    private fun buildAbsolutePath(path: Path): String = Path.combine(imageRootPath, path).compose()

    private fun buildProjectPath(projectId: String): String = buildAbsolutePath(Path(arrayOf(PROJECTS_FOLDER, projectId)))

    companion object {
        const val IMAGES_FOLDER = "images"
        const val PROJECTS_FOLDER = "projects"
    }
}
