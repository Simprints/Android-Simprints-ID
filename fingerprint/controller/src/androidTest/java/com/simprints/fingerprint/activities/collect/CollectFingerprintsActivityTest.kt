package com.simprints.fingerprint.activities.collect

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.widget.Button
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.google.common.truth.Truth.assertThat
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.FingerState
import com.simprints.fingerprint.activities.collect.state.ScanResult
import com.simprints.fingerprint.activities.collect.tryagainsplash.SplashScreenActivity
import com.simprints.fingerprint.activities.connect.ConnectScannerActivity
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FingerprintGenerator
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.mock.MockTimer
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectFingerprintsActivityTest {

    private lateinit var scenario: ActivityScenario<CollectFingerprintsActivity>

    private val state = MutableLiveData<CollectFingerprintsState>()
    private val vibrate = MutableLiveData<LiveDataEvent>()
    private val noFingersScannedToast = MutableLiveData<LiveDataEvent>()
    private val launchAlert = MutableLiveData<LiveDataEventWithContent<AlertError>>()
    private val launchReconnect = MutableLiveData<LiveDataEvent>()
    private val finishWithFingerprints =
        MutableLiveData<LiveDataEventWithContent<List<Fingerprint>>>()

    private val mockTimer = MockTimer()
    private val timeHelper: FingerprintTimeHelper = mockk(relaxed = true) {
        every { newTimer() } returns mockTimer
    }
    private val sessionEventsManager: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns mockk {
                every { qualityThreshold } returns 60
                every { displayHandIcons } returns true
                every { vero2 } returns mockk {
                    every { displayLiveFeedback } returns false
                    every { captureStrategy } returns Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
                    every { imageSavingStrategy } returns Vero2Configuration.ImageSavingStrategy.NEVER
                }
            }
        }
    }
    private val scanner: ScannerWrapper = mockk<ScannerWrapper>(relaxUnitFun = true).apply {
        every { isLiveFeedbackAvailable() } returns false
        coEvery { disconnect() } just Runs
    }
    private val scannerManager: ScannerManager =
        spyk(ScannerManagerImpl(mockk(), mockk(), mockk(), mockk())) {
            coEvery { checkBluetoothStatus() } just Runs
        }

    private val imageManager: FingerprintImageManager = mockk(relaxed = true)

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val mockCoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val vm: CollectFingerprintsViewModel = spyk(
        CollectFingerprintsViewModel(
            scannerManager, configManager, imageManager, timeHelper,
            sessionEventsManager, mockk(), mockk(), EncodingUtilsImplForTests,
            mockCoroutineScope,
        )
    ) {
        every { stateLiveData } returns this@CollectFingerprintsActivityTest.state
        every { vibrate } returns this@CollectFingerprintsActivityTest.vibrate
        every { noFingersScannedToast } returns this@CollectFingerprintsActivityTest.noFingersScannedToast
        every { launchAlert } returns this@CollectFingerprintsActivityTest.launchAlert
        every { launchReconnect } returns this@CollectFingerprintsActivityTest.launchReconnect
        every { finishWithFingerprints } returns this@CollectFingerprintsActivityTest.finishWithFingerprints

        every { start(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            val fingers = (args.first() as List<FingerIdentifier>)
            this@CollectFingerprintsActivityTest.state.value = startingState(fingers)
        }

        every { state } answers { this@CollectFingerprintsActivityTest.state.value!! }
        every { isImageTransferRequired() } returns true
    }

    private fun startingState(fingers: List<FingerIdentifier>): CollectFingerprintsState =
        CollectFingerprintsState(
            fingerStates = fingers.map { FingerState(it, listOf(CaptureState.NotCollected)) },
            currentFingerIndex = 0,
            isAskingRescan = false,
            isShowingConfirmDialog = false,
            isShowingSplashScreen = false
        )


//    @get:Rule
//    val koinTestRule = KoinTestRule(modules = listOf(module {
//        factory<MasterFlowManager> { mockk { every { getCurrentAction() } returns Action.IDENTIFY } }
//        viewModel { vm }
//    }))

    @Before
    fun setUp() {
        every { scannerManager.scanner } returns scanner
    }

    @Test
    fun startingState_producesCorrectUi() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            it.assertViewPager(count = 2, currentIndex = 0)
            it.assertScanButtonText(R.string.scan_label)
        }
    }

    @Test
    fun scanning_producesCorrectUi() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            state.value = startingState(TWO_FINGERS_IDS).updateCurrentFingerState { toScanning() }
            it.assertViewPager(count = 2, currentIndex = 0)
            it.assertScanButtonText(R.string.cancel_button)
        }
    }

    @Test
    fun transferringImage_producesCorrectUi() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            state.value = startingState(TWO_FINGERS_IDS).updateCurrentFingerState { toScanning() }
            state.value = startingState(TWO_FINGERS_IDS).updateCurrentFingerState {
                toTransferringImage(ScanResult(GOOD_QUALITY, TEMPLATE, null, 60))
            }
            it.assertViewPager(count = 2, currentIndex = 0)
            it.assertScanButtonText(R.string.please_wait_button)
        }
    }

    @Test
    fun goodScan_producesCorrectUi() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            state.value = startingState(TWO_FINGERS_IDS).updateCurrentFingerState {
                toCollected(
                    ScanResult(
                        GOOD_QUALITY,
                        TEMPLATE,
                        null,
                        60
                    )
                )
            }
            it.assertViewPager(count = 2, currentIndex = 0)
            it.assertScanButtonText(R.string.good_scan_message)
        }
    }

    @Test
    fun badScan_producesCorrectUi() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            state.value = startingState(TWO_FINGERS_IDS).updateCurrentFingerState {
                toCollected(
                    ScanResult(
                        BAD_QUALITY,
                        TEMPLATE,
                        null,
                        60
                    )
                )
            }
            it.assertViewPager(count = 2, currentIndex = 0)
            it.assertScanButtonText(R.string.rescan_label)
        }
    }

    @Test
    fun rescan_producesCorrectUi() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            state.value = startingState(TWO_FINGERS_IDS).updateCurrentFingerState {
                toCollected(
                    ScanResult(
                        GOOD_QUALITY,
                        TEMPLATE,
                        null,
                        60
                    )
                )
            }.copy(isAskingRescan = true)
            it.assertViewPager(count = 2, currentIndex = 0)
            it.assertScanButtonText(R.string.rescan_label_question)
        }
    }

    @Test
    fun currentFingerIndexUpdates_scrollsProperly() {
        scenario = ActivityScenario.launch(collectTaskRequest(FOUR_FINGERS_IDS).toIntent())

        val startingState = startingState(FOUR_FINGERS_IDS)
        val initialState = startingState.copy(
            fingerStates = listOf(
                FingerState(
                    FOUR_FINGERS_IDS[0],
                    listOf(CaptureState.Collected(ScanResult(GOOD_QUALITY, TEMPLATE, null, 60)))
                ),
                FingerState(
                    FOUR_FINGERS_IDS[1],
                    listOf(CaptureState.Collected(ScanResult(BAD_QUALITY, TEMPLATE, null, 60)))
                )
            )
        )

        scenario.onActivity {
            state.value = initialState
            it.assertViewPager(count = 4, currentIndex = 0)
            it.assertScanButtonText(R.string.good_scan_message)
            state.value = initialState.copy(currentFingerIndex = 1)
            it.assertViewPager(count = 4, currentIndex = 1)
            it.assertScanButtonText(R.string.rescan_label)
            state.value = initialState.copy(currentFingerIndex = 3)
            it.assertViewPager(count = 4, currentIndex = 3)
            it.assertScanButtonText(R.string.scan_label)
        }
    }

    @Test
    fun numberOfFingersIncreases_addsNewFingersCorrectly() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            state.value = startingState(TWO_FINGERS_IDS)
            it.assertViewPager(count = 2, currentIndex = 0)
            state.value = startingState(FOUR_FINGERS_IDS)
            it.assertViewPager(count = 4, currentIndex = 0)
        }
    }

    @Test
    fun numberOfFingersDecreases_removesFingersCorrectly() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        scenario.onActivity {
            state.value = startingState(FOUR_FINGERS_IDS)
            it.assertViewPager(count = 4, currentIndex = 0)
            state.value = startingState(TWO_FINGERS_IDS)
            it.assertViewPager(count = 2, currentIndex = 0)
        }
    }

    @Test
    fun isShowingSplashScreen_launchesSplashScreenActivity() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())
        Intents.init()

        Intents.intending(hasComponent(SplashScreenActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        state.postValue(startingState(TWO_FINGERS_IDS).copy(isShowingSplashScreen = true))

        Intents.intended(hasComponent(SplashScreenActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun receivesLaunchAlertEvent_launchesAlertActivity() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())
        Intents.init()

        Intents.intending(hasComponent("com.simprints.feature.alert.intent.AlertWrapperActivity"))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        launchAlert.postEvent(AlertError.UNEXPECTED_ERROR)

        Intents.intended(hasComponent("com.simprints.feature.alert.intent.AlertWrapperActivity"))

        Intents.release()
    }

    @Test
    fun receivesReconnectEvent_launchesConnectScannerActivity() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())
        Intents.init()

        Intents.intending(hasComponent(ConnectScannerActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        launchReconnect.postEvent()
        Intents.intended(hasComponent(ConnectScannerActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun finishWithFingerprintsEvent_finishesActivityWithCorrectResult() {
        scenario = ActivityScenario.launch(collectTaskRequest(TWO_FINGERS_IDS).toIntent())

        val fingerprints = FingerprintGenerator.generateRandomFingerprints(2)

        vm.finishWithFingerprints.postEvent(fingerprints)

        val result = scenario.result.resultData.run {
            setExtrasClassLoader(CollectFingerprintsTaskResult::class.java.classLoader)
            extras?.getParcelable<CollectFingerprintsTaskResult>(CollectFingerprintsTaskResult.BUNDLE_KEY)
        }

        assertThat(result).isNotNull()
        assertThat(result?.fingerprints?.map { it.fingerId }).containsExactlyElementsIn(fingerprints.map { it.fingerId })
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    private fun CollectFingerprintsState.updateCurrentFingerState(block: CaptureState.() -> CaptureState) =
        this.copy(
            fingerStates = fingerStates.updateOnIndex(currentFingerIndex) { fingerState ->
                fingerState.copy(
                    captures = fingerState.captures.updateOnIndex(
                        index = fingerState.currentCaptureIndex,
                        newItem = block
                    )
                )
            }
        )

    private fun CollectFingerprintsActivity.assertViewPager(count: Int, currentIndex: Int) {
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        assertThat(viewPager.adapter?.itemCount).isEqualTo(count)
        assertThat(viewPager.currentItem).isEqualTo(currentIndex)
    }

    private fun CollectFingerprintsActivity.assertScanButtonText(@StringRes stringRes: Int) {
        val scanButton = findViewById<Button>(R.id.scan_button)
        assertThat(scanButton.text.toString()).isEqualTo(getString(stringRes))
    }

    companion object {
        val TWO_FINGERS_IDS =
            listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER)
        val FOUR_FINGERS_IDS = listOf(
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER,
            FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.RIGHT_INDEX_FINGER
        )

        const val GOOD_QUALITY = 80
        const val BAD_QUALITY = 20

        val TEMPLATE = byteArrayOf(0x00, 0x01)

        private fun collectTaskRequest(fingersToCapture: List<FingerIdentifier>) =
            CollectFingerprintsTaskRequest(fingersToCapture)

        private fun CollectFingerprintsTaskRequest.toIntent() = Intent().also {
            it.setClassName(
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext,
                CollectFingerprintsActivity::class.qualifiedName!!
            )
            it.putExtra(CollectFingerprintsTaskRequest.BUNDLE_KEY, this)
        }
    }

    private fun <T> List<T>.updateOnIndex(index: Int, newItem: (T) -> T): List<T> =
        mapIndexed { i, item ->
            when (i) {
                index -> newItem(item)
                else -> item
            }
        }
}
