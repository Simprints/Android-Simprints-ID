package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.templates.FirstUseLocal
import com.simprints.id.templates.HappyWifi
import com.simprints.id.tools.CalloutCredentials
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class HappyAuthTests: FirstUseLocal, HappyWifi {

    private val calloutCredentials = CalloutCredentials(
        "project_id",
        "the_one_and_only_module",
        "the_lone_user",
        "legacy_project_id")

    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        "local_db_key_value".toByteArray(),
        calloutCredentials.legacyApiKey?: "")

    override var realmConfiguration: RealmConfiguration? = null

    @Before
    override fun setUp() {
        super<HappyWifi>.setUp()
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value)

        super<FirstUseLocal>.setUp()
    }
}
