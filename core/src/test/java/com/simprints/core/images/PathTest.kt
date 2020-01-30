package com.simprints.core.images

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class PathTest {

    @Test
    fun shouldComposePathOutOfString() {
        val path = Path("dir/file.txt")

        assertThat(path.compose()).isEqualTo("dir/file.txt")
    }

    @Test
    fun shouldComposePathOutOfMultipleParts() {
        val dirs = arrayOf("dir1", "dir2", "file.txt")
        val path = Path(dirs)

        assertThat(path.compose()).isEqualTo("dir1/dir2/file.txt")
    }

    @Test
    fun shouldCombinePaths() {
        val first = "/home/test"
        val subDirs = Path(arrayOf("dir1", "dir2", "file.txt"))

        val result = Path.combine(first, subDirs)

        assertThat(result.compose()).isEqualTo("/home/test/dir1/dir2/file.txt")
    }

    @Test
    fun shouldParsePathStringWhenSourceIsDirectory() {
        val file = File("my/test/directory").apply { mkdirs() }
        val pathString = file.path

        val actual = Path.parse(pathString).compose()

        assertThat(actual).isEqualTo("my/test/directory")
    }

    @Test
    fun shouldRemoveSubsetFromPath() {
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "file.txt"))

        val subset = arrayOf("dir1", "dir2")
        val actual = originalPath.remove(subset).compose()

        assertThat(actual).isEqualTo("dir3/file.txt")
    }

    @Test
    fun whenSubsetToRemoveIsNotContainedInPath_shouldNotRemoveAnything() {
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "file.txt"))

        val actual = originalPath.remove("dir700").compose()

        assertThat(actual).isEqualTo("dir1/dir2/dir3/file.txt")
    }

    @Test
    fun shouldRemoveSubPathFromPath() {
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "file.txt"))

        val subPath = Path(arrayOf("dir1", "dir2"))
        val actual = originalPath.remove(subPath).compose()

        assertThat(actual).isEqualTo("dir3/file.txt")
    }

    @Test
    fun whenSubPathToRemoveIsNotContainedInPath_shouldNotRemoveAnything() {
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "file.txt"))

        val subPath = Path("dir700")
        val actual = originalPath.remove(subPath).compose()

        assertThat(actual).isEqualTo("dir1/dir2/dir3/file.txt")
    }

}
