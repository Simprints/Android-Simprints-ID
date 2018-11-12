package com.simprints.id.testTemplates

import android.support.test.InstrumentationRegistry
import android.util.Base64
import com.simprints.id.data.analytics.eventData.realm.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.realm.SessionRealmConfig
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.testTools.StorageUtils
import com.simprints.id.testTools.log
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before

interface FirstUseLocal {

    companion object {
        val realmKey: ByteArray = Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", Base64.NO_WRAP)

        private val sessionLocalDbKey = LocalDbKey(RealmSessionEventsDbManagerImpl.SESSIONS_REALM_DB_FILE_NAME, realmKey)
        val sessionRealmConfiguration = SessionRealmConfig.get(sessionLocalDbKey.projectId, sessionLocalDbKey.value)
    }

    var peopleRealmConfiguration: RealmConfiguration

    fun setUp() {
        log("FirstUseTest.setUp(): cleaning internal data")
        StorageUtils.clearApplicationData(InstrumentationRegistry.getTargetContext())
        StorageUtils.clearRealmDatabase(peopleRealmConfiguration)
        StorageUtils.clearRealmDatabase(sessionRealmConfiguration)
    }

    fun tearDown() {
        log("FirstUseTest.tearDown(): cleaning internal data")
        StorageUtils.clearApplicationData(InstrumentationRegistry.getTargetContext())
        StorageUtils.clearRealmDatabase(peopleRealmConfiguration)
        StorageUtils.clearRealmDatabase(sessionRealmConfiguration)
    }
}
