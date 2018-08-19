package com.simprints.id.activities

import android.net.NetworkInfo
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.firebase.FirebaseApp
import com.google.gson.stream.JsonReader
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.activities.collectFingerprints.ViewPagerCustom
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.data.analytics.events.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.events.SessionEventsManager
import com.simprints.id.data.analytics.events.SessionEventsManagerImpl
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.DependencyRule.ReplaceRule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.ShadowViewPager
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createRoboActivity
import com.simprints.id.testUtils.roboletric.setupSessionEventsManagerToAvoidRealmCall
import com.simprints.id.tools.AppState
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.Scanner
import com.simprints.libscanner.ScannerCallback
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import de.mrapp.android.dialog.view.ViewPager
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_launch.view.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

// Attempt to write unit test for scanner with roboeletric
//
//
//@RunWith(RobolectricTestRunner::class)
//@Config(application = TestApplication::class, shadows = [ShadowViewPager::class])
//class CollectFingerprintsActivityTest : RxJavaTest, DaggerForTests() {
//
//    @Inject
//    lateinit var preferencesManager: PreferencesManager
//    @Inject
//    lateinit var simNetworkUtilsMock: SimNetworkUtils
//    @Inject
//    lateinit var appState: AppState
//
//    @Inject
//    lateinit var sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager
//
//    override var module by lazyVar {
//        AppModuleForTests(app,
//            bluetoothComponentAdapterRule = ReplaceRule { mockBluetoothAdapter },
//            localDbManagerRule = MockRule,
//            sessionEventsLocalDbManagerRule = MockRule,
//            simNetworkUtilsRule = MockRule)
//    }
//
//    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter
//
//    @Before
//    override fun setUp() {
//        FirebaseApp.initializeApp(RuntimeEnvironment.application)
//        app = (RuntimeEnvironment.application as TestApplication)
//        super.setUp()
//        testAppComponent.inject(this)
//
//        whenever(simNetworkUtilsMock.mobileNetworkType).thenReturn("LTE")
//        whenever(simNetworkUtilsMock.connectionsStates).thenReturn(listOf(SimNetworkUtils.Connection("WIFI", NetworkInfo.DetailedState.CONNECTED)))
//
//        setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManagerMock)
//        ShadowLog.stream = System.out
//    }
//
//    @Test
//    @Config(sdk = [21])
//    fun test() {
//
//        val test = CompletableFuture<Boolean>()
//        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN)))
//
//        preferencesManager.scheduledPeopleSyncWorkRequestId = "request_id"
//        preferencesManager.scheduledSessionsSyncWorkRequestId = "request_id"
//
//        val controller = createRoboActivity<LaunchActivity>().start().resume().visible()
//        val activity = controller.get()
//
//        val consentButton = activity.findViewById<Button>(R.id.consentAcceptButton)
//        Assert.assertEquals(true, test.get())
//    }
//
//    @Test
//    @Config(sdk = [27])
//    fun threeBadScanAndNotMaxReached_thenAddAFinger() {
//
//        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN)))
//
//        appState.scanner = spy(Scanner("F0:AC:D7:C8:CB:22", mockBluetoothAdapter))
//
//        appState.scanner.connect( object : ScannerCallback {
//            override fun onSuccess() {
//
//                appState.scanner.un20Wakeup(object : ScannerCallback {
//                    override fun onFailure(error: SCANNER_ERROR?) {
//                        Log.d("Test", "Scanner onFailure: $error")
//                    }
//                    override fun onSuccess() {
//                        Log.d("Test", "Scanner success un20Wakeup")
//                    }
//                })
//            }
//
//            override fun onFailure(error: SCANNER_ERROR?) {
//                Log.d("Test", "Scanner onFailure: $error")
//            }
//        })
//
//        preferencesManager.calloutAction = CalloutAction.VERIFY
//
//        val controller = createRoboActivity<CollectFingerprintsActivity>().start().resume().visible()
//        val activity = controller.get()
//
//        activity.findViewById<Button>(R.id.scan_button).apply {
//            performClick()
//            Thread.sleep(10000)
//
//            performClick()
//            Thread.sleep(10000)
//
//            performClick()
//            Thread.sleep(10000)
//        }
//
//        activity.findViewById<ViewPagerCustom>(R.id.view_pager).also {
//            Assert.assertEquals(it.currentItem, 0)
//        }
//    }
//}
