package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import android.util.Base64.NO_WRAP
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.templates.FirstUseLocal
import com.simprints.id.templates.HappyWifi
import com.simprints.id.launchAppFromIntentEnrol
import com.simprints.id.tools.CalloutCredentials
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthTests_FirstUseHappyWifi: FirstUseLocal, HappyWifi {

    private val calloutCredentials = CalloutCredentials(
        "EGkJFvCS7202A07I0fup",
        "the_one_and_only_module",
        "the_lone_user",
        "ec9a52bf-6803-4237-a647-6e76b133fc29")

    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", NO_WRAP),
        calloutCredentials.legacyApiKey?: "")

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Before
    override fun setUp() {
        super<HappyWifi>.setUp()
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value)

        super<FirstUseLocal>.setUp()
    }

    @Test
    fun validLegacyCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), activityTestRule)
    }

    @Test
    fun validCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), activityTestRule)
    }
}
