package com.simprints.id.testTemplates

import android.support.test.InstrumentationRegistry
import com.simprints.id.testTools.StorageUtils
import com.simprints.id.testTools.log
import io.realm.RealmConfiguration
import org.junit.Before


interface FirstUseLocal {

    var realmConfiguration: RealmConfiguration?

    @Before
    fun setUp() {
        // Clear any internal data
        log("FirstUseTest.setUp(): cleaning internal data")
        StorageUtils.clearApplicationData(InstrumentationRegistry.getContext(), realmConfiguration!!)
    }
}
