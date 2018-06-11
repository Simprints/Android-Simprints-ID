package com.simprints.id.testTools

import android.content.Context
import com.simprints.id.data.prefs.PreferencesManagerImpl
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File

object StorageUtils {

    fun clearApplicationData(context: Context, realmConfiguration: RealmConfiguration) {
        clearSharedPrefs(context)
        clearRealmDatabase(realmConfiguration)
    }

    private fun clearCache(context: Context) {
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

    private fun clearSharedPrefs(context: Context) {
        log("StorageUtils.clearApplicationData(): clearing shared prefs.")
        context.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE).edit().clear().commit()
    }

    private fun clearRealmDatabase(realmConfiguration: RealmConfiguration?) {
        if (realmConfiguration != null) {
            val realm = Realm.getInstance(realmConfiguration)
            try {
                realm.beginTransaction()
                realm.deleteAll()
                realm.commitTransaction()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                realm.close()
            }
        }
    }
}
