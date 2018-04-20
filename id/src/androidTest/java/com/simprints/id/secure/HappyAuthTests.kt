package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.templates.FirstUseLocal
import com.simprints.id.templates.FirstUseRemote
import com.simprints.id.templates.HappyWifi
import com.simprints.id.tools.CalloutCredentials
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class HappyAuthTests: FirstUseLocal, FirstUseRemote, HappyWifi {

    override val calloutCredentials: CalloutCredentials = CalloutCredentials(
        "00000002-0000-0000-0000-000000000000",
        "the_one_and_only_module",
        "the_lone_user")

    override var realmConfiguration: RealmConfiguration? = null

    override fun setUp() {
        super<HappyWifi>.setUp()
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(calloutCredentials.apiKey, byteArrayOf())

        super<FirstUseLocal>.setUp()
        super<FirstUseRemote>.setUp()
    }
}
