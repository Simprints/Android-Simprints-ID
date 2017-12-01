package com.simprints.id

import android.content.Context
import com.simprints.cerberuslibrary.RealmUtility
import io.realm.RealmConfiguration
import java.io.File

object StorageUtils {

    @JvmStatic
    fun clearApplicationData(context: Context, realmConfiguration: RealmConfiguration) {
        clearRealmDatabase(realmConfiguration)
        val cacheDirectory = context.cacheDir ?: return
        val applicationDirectory = File(cacheDirectory.parent)
        if (applicationDirectory.exists()) {
            val fileNames = applicationDirectory.list() ?: return
            fileNames
                    .filter { it != "lib" }
                    .forEach { deleteFile(File(applicationDirectory, it)) }
        }
    }

    private fun deleteFile(file: File?): Boolean {
        var deletedAll = true
        if (file != null) {
            if (file.isDirectory) {
                val children = file.list()
                for (child in children) {
                    deletedAll = deleteFile(File(file, child)) && deletedAll
                }
            } else {
                deletedAll = file.delete()
            }
        }
        return deletedAll
    }

    private fun clearRealmDatabase(realmConfiguration: RealmConfiguration?) {
        if (realmConfiguration == null) return
        RealmUtility().clearRealmDatabase(realmConfiguration)
    }
}
