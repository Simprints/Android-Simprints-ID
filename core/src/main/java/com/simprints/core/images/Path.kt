package com.simprints.core.images

data class Path(private val dirs: Array<String>) {

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
