package com.simprints.core.images.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class PathTest {

    @Test
    fun shouldComposePathOutOfString() {
        val path = com.simprints.id.data.images.model.Path("dir/file.txt")

        assertThat(path.compose()).isEqualTo("dir/file.txt")
    }

    @Test
    fun shouldComposePathOutOfMultipleParts() {
        val dirs = arrayOf("dir1", "dir2", "file.txt")
        val path = com.simprints.id.data.images.model.Path(dirs)

        assertThat(path.compose()).isEqualTo("dir1/dir2/file.txt")
    }

    @Test
    fun shouldCombinePaths() {
        val first = "/home/test"
        val subDirs = com.simprints.id.data.images.model.Path(
                arrayOf(
                        "dir1",
                        "dir2",
                        "file.txt"
                )
        )

        val result = com.simprints.id.data.images.model.Path.combine(first, subDirs)

        assertThat(result.compose()).isEqualTo("/home/test/dir1/dir2/file.txt")
    }

    @Test
    fun shouldParsePathStringWhenSourceIsDirectory() {
        val file = File("my/test/directory").apply { mkdirs() }
        val pathString = file.path

        val actual = com.simprints.id.data.images.model.Path.parse(pathString).compose()

        assertThat(actual).isEqualTo("my/test/directory")
    }

    @Test
    fun shouldRemoveSubsetFromPath() {
        val originalPath = com.simprints.id.data.images.model.Path(
                arrayOf(
                        "dir1",
                        "dir2",
                        "dir3",
                        "file.txt"
                )
        )

        val subset = arrayOf("dir1", "dir2")
        val actual = originalPath.remove(subset).compose()

        assertThat(actual).isEqualTo("dir3/file.txt")
    }

    @Test
    fun whenSubsetToRemoveIsNotContainedInPath_shouldNotRemoveAnything() {
        val originalPath = com.simprints.id.data.images.model.Path(
                arrayOf(
                        "dir1",
                        "dir2",
                        "dir3",
                        "file.txt"
                )
        )

        val actual = originalPath.remove("dir700").compose()

        assertThat(actual).isEqualTo("dir1/dir2/dir3/file.txt")
    }

    @Test
    fun shouldRemoveSubPathFromPath() {
        val originalPath = com.simprints.id.data.images.model.Path(
                arrayOf(
                        "dir1",
                        "dir2",
                        "dir3",
                        "file.txt"
                )
        )

        val subPath =
                com.simprints.id.data.images.model.Path(arrayOf("dir1", "dir2"))
        val actual = originalPath.remove(subPath).compose()

        assertThat(actual).isEqualTo("dir3/file.txt")
    }

    @Test
    fun whenSubPathToRemoveIsNotContainedInPath_shouldNotRemoveAnything() {
        val originalPath = com.simprints.id.data.images.model.Path(
                arrayOf(
                        "dir1",
                        "dir2",
                        "dir3",
                        "file.txt"
                )
        )

        val subPath = com.simprints.id.data.images.model.Path("dir700")
        val actual = originalPath.remove(subPath).compose()

        assertThat(actual).isEqualTo("dir1/dir2/dir3/file.txt")
    }

}
