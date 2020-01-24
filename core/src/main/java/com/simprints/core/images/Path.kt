package com.simprints.core.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * An abstraction of a directory structure
 *
 * @property dirs
 *           the directories within the structure.
 *           e.g.: for dir1/dir2/dir3 [dirs] should be @sample [arrayOf("dir1", "dir2", "dir3")]
 */
@Parcelize
data class Path(private val dirs: Array<String>) : Parcelable {

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
        /**
         * Joins a path to a subset of paths
         *
         * @param first
         *        the first part. Can be either one or multiple directories separated by a /
         * @param subDirs
         *        the latter part. Can also be either one or multiple directories as a Path
         *        object.
         * @return a Path object combining both parts
         */
        fun combine(first: String, subDirs: Path): Path {
            val dirs = arrayOf(first, *subDirs.dirs)
            return Path(dirs)
        }
    }

}
