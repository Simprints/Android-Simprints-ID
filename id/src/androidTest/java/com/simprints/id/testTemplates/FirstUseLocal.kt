package com.simprints.id.testTemplates

import android.support.test.InstrumentationRegistry
import com.simprints.id.data.analytics.eventData.realm.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.realm.SessionRealmConfig
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.testTools.DEFAULT_REALM_KEY
import com.simprints.id.testTools.StorageUtils
import com.simprints.id.testTools.log
import io.realm.RealmConfiguration

interface FirstUseLocal {

    companion object {
        private val sessionLocalDbKey = LocalDbKey(RealmSessionEventsDbManagerImpl.SESSIONS_REALM_DB_FILE_NAME, DEFAULT_REALM_KEY)
        private val sessionRealmConfiguration = SessionRealmConfig.get(sessionLocalDbKey.projectId, sessionLocalDbKey.value)
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
