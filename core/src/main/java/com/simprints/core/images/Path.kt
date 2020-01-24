package com.simprints.core.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

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
    constructor(dir: String) : this(arrayOf(dir))

    /**
     * Composes the path, separating the directories by a /
     */
    fun compose() = dirs.joinToString("/")

    /**
     * Removes a directory. e.g.: if the current path is dir1/dir2/dir3/dir4 and
     * the directory to be removed is dir1, then the output will be dir2/dir3/dir4.
     *
     * @param dir the directory to be removed
     * @return the path without the directory
     */
    fun remove(dir: String): Path = remove(arrayOf(dir))

    /**
     * Removes a subset of directories. e.g.: if the current path is dir1/dir2/dir3/dir4 and
     * the subset to be removed is dir1/dir2, then the output will be dir3/dir4.
     *
     * @param subset the subset to be removed
     * @return the path without the subset
     */
    fun remove(subset: Array<String>): Path {
        val resultDirs = dirs.toMutableList().apply {
            removeAll(subset)
        }.toTypedArray()

        return Path(resultDirs)
    }

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

        /**
         * Parses a file path into a Path object, excluding the file name
         *
         * @param file the file to be parsed
         * @return the path, excluding the file name
         */
        fun parse(file: File): Path {
            val pathString = if (file.isDirectory)
                file.path
            else
                file.path.replace(file.name, "")

            val dirs = pathString.split('/').filter { it.isNotEmpty() }.toTypedArray()
            return Path(dirs)
        }
    }

}
