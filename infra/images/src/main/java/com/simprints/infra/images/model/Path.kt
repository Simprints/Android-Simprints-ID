package com.simprints.infra.images.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.Serializable

/**
 * An abstraction of a file path
 *
 * @property parts
 *           the parts of the path, including file name.
 *           e.g.: for dir1/dir2/file.txt [parts] should be
 *           @sample [arrayOf("dir1", "dir2", "file.txt")]
 */
@Keep
@Parcelize
data class Path(
    val parts: Array<String>,
) : Parcelable,
    Serializable {
    /**
     * Constructor with a string path
     * @param pathString the path as a string
     */
    constructor(pathString: String) : this(parse(pathString).parts)

    /**
     * Composes the path, separating the parts by a /
     */
    fun compose(): String = parts.joinToString(File.separator)

    /**
     * Removes a directory. e.g.: if the current path is dir1/dir2/dir3/file.txt and
     * the directory to be removed is dir1, then the output will be dir2/dir3/file.txt.
     *
     * @param subPathString the sub-path to be removed, as a string
     * @return the path without sub-path
     */
    fun remove(subPathString: String): Path = remove(parse(subPathString))

    /**
     * Removes a sub-path. e.g.: if the current path is dir1/dir2/dir3/file.txt and
     * the sub-path to be removed is dir1/dir2, then the output will be dir3/file.txt.
     *
     * @param subPath the sub-path to be removed
     * @return the path without the sub-path
     */
    fun remove(subPath: Path): Path = remove(subPath.parts)

    /**
     * Removes a subset of the path. e.g.: if the current path is dir1/dir2/dir3/file.txt and
     * the subset to be removed is dir1/dir2, then the output will be dir3/file.txt.
     *
     * @param subset the subset to be removed
     * @return the path without the subset
     */
    fun remove(subset: Array<String>): Path {
        val resultParts = parts
            .toMutableList()
            .apply {
                removeAll(subset.toSet())
            }.toTypedArray()

        return Path(resultParts)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Path) {
            return false
        }

        return parts.contentEquals(other.parts)
    }

    override fun hashCode(): Int = parts.contentHashCode() * 2 + 27

    override fun toString(): String = super.toString()

    companion object {
        /**
         * Joins two paths
         *
         * @param first the first part.
         * @param last the last part.
         * @return a Path object combining both parts
         */
        fun combine(
            first: String,
            last: Path,
        ): Path {
            val parts = arrayOf(first, *last.parts)
            return Path(parts)
        }

        /**
         * Parses a path string into a Path object
         *
         * @param pathString the path string to be parsed
         * @return the path
         */
        fun parse(pathString: String): Path {
            val parts = pathString.split(File.separator).filter { it.isNotEmpty() }.toTypedArray()
            return Path(parts)
        }
    }
}
