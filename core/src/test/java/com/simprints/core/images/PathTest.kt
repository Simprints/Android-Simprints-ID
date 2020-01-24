package com.simprints.core.images

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class PathTest {

    @Test
    fun shouldComposePathOutOfSingleDir() {
        val path = Path("dir")

        assertThat(path.compose()).isEqualTo("dir")
    }

    @Test
    fun shouldComposePathOutOfMultipleDirs() {
        val dirs = arrayOf("dir1", "dir2", "dir3")
        val path = Path(dirs)

        assertThat(path.compose()).isEqualTo("dir1/dir2/dir3")
    }

    @Test
    fun shouldCombinePaths() {
        val first = "/home/test"
        val subDirs = Path(arrayOf("dir1", "dir2", "dir3"))

        val result = Path.combine(first, subDirs)

        assertThat(result.compose()).isEqualTo("/home/test/dir1/dir2/dir3")
    }

    @Test
    fun shouldParseFileExcludingFileName() {
        val file = File("my/test/file.txt")

        val actual = Path.parse(file).compose()

        assertThat(actual).isEqualTo("my/test")
    }

    @Test
    fun shouldParseFileWhenSourceIsDirectory() {
        val file = File("my/test/directory").apply { mkdirs() }

        val actual = Path.parse(file).compose()

        assertThat(actual).isEqualTo("my/test/directory")
    }

    @Test
    fun shouldParsePathStringExcludingFileName() {
        val pathString = "my/test/file.txt"

        val actual = Path.parse(pathString).compose()

        assertThat(actual).isEqualTo("my/test")
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
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "dir4"))

        val subset = arrayOf("dir1", "dir2")
        val actual = originalPath.remove(subset).compose()

        assertThat(actual).isEqualTo("dir3/dir4")
    }

    @Test
    fun whenSubsetToRemoveIsNotContainedInPath_shouldNotRemoveAnything() {
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "dir4"))

        val actual = originalPath.remove("dir700").compose()

        assertThat(actual).isEqualTo("dir1/dir2/dir3/dir4")
    }

    @Test
    fun shouldRemoveSubPathFromPath() {
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "dir4"))

        val subPath = Path(arrayOf("dir1", "dir2"))
        val actual = originalPath.remove(subPath).compose()

        assertThat(actual).isEqualTo("dir3/dir4")
    }

    @Test
    fun whenSubPathToRemoveIsNotContainedInPath_shouldNotRemoveAnything() {
        val originalPath = Path(arrayOf("dir1", "dir2", "dir3", "dir4"))

        val subPath = Path("dir700")
        val actual = originalPath.remove(subPath).compose()

        assertThat(actual).isEqualTo("dir1/dir2/dir3/dir4")
    }

}
