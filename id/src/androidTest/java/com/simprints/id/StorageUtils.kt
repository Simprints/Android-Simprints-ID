package com.simprints.id

import android.content.Context

import com.simprints.cerberuslibrary.RealmUtility
import com.simprints.remoteadminclient.Configuration
import com.simprints.remoteadminclient.api.DefaultApi
import com.simprints.remoteadminclient.auth.ApiKeyAuth
import com.squareup.okhttp.OkHttpClient

import java.io.File
import java.util.concurrent.TimeUnit

import io.realm.RealmConfiguration

object StorageUtils {

    @JvmStatic
    fun clearApplicationData(context: Context, realmConfiguration: RealmConfiguration) {
        clearRealmDatabase(realmConfiguration)
        val cacheDirectory = context.cacheDir ?: return
        val applicationDirectory = File(cacheDirectory.parent)
        if (applicationDirectory.exists()) {
            val fileNames = applicationDirectory.list() ?: return
            for (fileName in fileNames) {
                if (fileName != "lib") {
                    deleteFile(File(applicationDirectory, fileName))
                }
            }
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
