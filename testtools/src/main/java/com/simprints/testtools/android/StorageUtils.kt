package com.simprints.testtools.android

import android.annotation.SuppressLint
import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File

object StorageUtils {

    @SuppressLint("ApplySharedPref")
    fun clearSharedPrefs(context: Context, prefFileName: String, prefFileMode: Int) {
        context.getSharedPreferences(prefFileName, prefFileMode).edit().clear().commit()
    }

    fun clearRealmDatabase(realmConfiguration: RealmConfiguration?) {
        if (realmConfiguration != null) {
            var realm: Realm? = null
            try {
                realm = Realm.getInstance(realmConfiguration)
                realm.beginTransaction()
                realm.deleteAll()
                realm.commitTransaction()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                realm?.close()
            }
        } else {
            throw IllegalStateException("Realm config uninitialised for testing")
        }
    }

    fun deleteAllDatabases(ctx: Context) {
        File(ctx.filesDir, "files").deleteRecursively()
    }
}
