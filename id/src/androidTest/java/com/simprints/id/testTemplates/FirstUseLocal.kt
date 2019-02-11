package com.simprints.id.testTemplates

import androidx.test.InstrumentationRegistry
import com.simprints.id.testtools.StorageUtils
import com.simprints.testframework.android.log
import io.realm.RealmConfiguration

interface FirstUseLocal {

    var peopleRealmConfiguration: RealmConfiguration?
    var sessionsRealmConfiguration: RealmConfiguration?

    fun setUp() {
        log("FirstUseTest.setUp(): cleaning internal data")

        StorageUtils.clearApplicationData(InstrumentationRegistry.getTargetContext())
        StorageUtils.clearRealmDatabase(peopleRealmConfiguration)
        StorageUtils.clearRealmDatabase(sessionsRealmConfiguration)
    }

    fun tearDown() {
        log("FirstUseTest.tearDown(): cleaning internal data")
        StorageUtils.clearApplicationData(InstrumentationRegistry.getTargetContext())
        StorageUtils.clearRealmDatabase(peopleRealmConfiguration)
        StorageUtils.clearRealmDatabase(sessionsRealmConfiguration)
    }
}
