package com.simprints.id.templates

import android.support.test.InstrumentationRegistry
import com.simprints.id.tools.StorageUtils
import com.simprints.id.tools.log
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
