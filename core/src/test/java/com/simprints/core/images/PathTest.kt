package com.simprints.core.images

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
    fun shouldJoinPaths() {
        val first = "/home/test"
        val subDirs = Path(arrayOf("dir1", "dir2", "dir3"))

        val result = Path.join(first, subDirs)

        assertThat(result.compose()).isEqualTo("/home/test/dir1/dir2/dir3")
    }

}
