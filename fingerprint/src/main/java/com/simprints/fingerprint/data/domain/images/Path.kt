package com.simprints.fingerprint.data.domain.images

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * This class represents the file storage path for a fingerprint image
 *
 * @property parts  the parts of the path, including file name e.g for dir1/dir2/file.txt [parts]
 *                  should be arrayOf("dir1", "dir2", "file.txt")
 */
@Parcelize
class Path(val parts: Array<String>) : Parcelable
