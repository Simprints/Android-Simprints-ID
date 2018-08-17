package com.simprints.id.coreFeatures

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.collectFingerprints.ViewPagerCustom
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.whenever
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.HappyWifi
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class CaptureFingerActivityTest : DaggerForAndroidTests(), FirstUseLocal, HappyWifi {

    private val calloutCredentials = CalloutCredentials(
        "bWOFHInKA2YaQwrxZ7uJ",
        "the_one_and_only_module",
        "the_lone_user",
        "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f")

    private val realmKey = Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", Base64.NO_WRAP)
    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        realmKey,
        calloutCredentials.legacyApiKey)

    private val projectSecret = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val scanTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter })
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager

    @Before
    override fun setUp() {
        super<HappyWifi>.setUp()
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(realmKey, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        super<FirstUseLocal>.setUp()

        signOut()
    }

    @Test
    fun threeBadScanAndNotMaxReached_thenAddAFinger() {

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN)))

        whenever(settingsPreferencesManagerSpy.getRemoteConfigFingerStatus()).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.RIGHT_THUMB to true ))

        // Launch and sign in
        launchActivityEnrol(calloutCredentials, scanTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        setupActivityAndContinue()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        val viewPager = scanTestRule.activity.findViewById<ViewPagerCustom>(R.id.view_pager)
        Assert.assertEquals(viewPager.currentItem, 3)
    }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
