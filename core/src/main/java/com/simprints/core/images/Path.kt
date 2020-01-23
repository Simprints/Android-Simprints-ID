package com.simprints.core.images

/**
 * An abstraction of a directory structure
 *
 * @property dirs
 *           the directories within the structure.
 *           e.g.: for dir1/dir2/dir3 [dirs] should be @sample [arrayOf("dir1", "dir2", "dir3")]
 */
data class Path(private val dirs: Array<String>) {

    /**
     * Constructor with a single directory
     * @param dir the directory
     */
    constructor(dir: String): this(arrayOf(dir))

    fun compose() = dirs.joinToString("/")

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Path)
            return false

        return dirs.contentEquals(other.dirs)
    }

    override fun hashCode(): Int = dirs.contentHashCode() * 2 + 27

    companion object {
        fun join(first: String, subDirs: Path): Path {
            val dirs = arrayOf(first, *subDirs.dirs)
            return Path(dirs)
        }
    }

}
