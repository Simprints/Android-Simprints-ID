package com.simprints.id.testtools

import android.content.Context
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.integration.testtools.exceptions.TestingSuiteError
import com.simprints.testtools.android.log
import io.realm.Realm
import io.realm.RealmConfiguration

object StorageUtils {

    fun clearApplicationData(context: Context) {
        clearSharedPrefs(context)
    }

    private fun clearSharedPrefs(context: Context) {
        log("StorageUtils.clearApplicationData(): clearing shared prefs.")
        context.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE).edit().clear().commit()
    }

    fun clearRealmDatabase(realmConfiguration: RealmConfiguration?) {
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
        } else {
            throw TestingSuiteError("Realm config uninitialised for testing")
        }
    }
}
