package com.simprints.fingerprint.integration

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.activities.collect.pressScanUntilDialogIsDisplayedAndClickConfirm
import com.simprints.fingerprint.activities.collect.waitUntilCollectFingerprintsIsDisplayed
import com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.factory.ScannerFactoryImpl
import com.simprints.fingerprint.testtools.FingerprintGenerator
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.SimulationMode
import com.simprints.fingerprintscannermock.simulated.component.SimulatedBluetoothAdapter
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.moduleapi.fingerprint.responses.IFingerprintCaptureResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintMatchResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponseType
import com.simprints.testtools.common.syntax.anyNotNull
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.asFlow
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
@Ignore("Test fails due to no local DB key")
class FingerprintFlowsIntegrationTest : KoinTest {

    private val dbManagerMock: FingerprintDbManager = mockk(relaxed = true)
    private val masterFlowManager: MasterFlowManager = mockk(relaxed = true)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var scenario: ActivityScenario<OrchestratorActivity>

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
    }

    private fun setupMocksAndKoinModules(simulationMode: SimulationMode, action: Action) {
        val simulatedBluetoothAdapter = SimulatedBluetoothAdapter(SimulatedScannerManager(simulationMode))
        loadKoinModules(module {
            single<ScannerFactory> {
                spyk(ScannerFactoryImpl(
                    bluetoothAdapter = simulatedBluetoothAdapter,
                    configManager = mockk(relaxed = true),
                    scannerUiHelper = mockk(relaxed = true),
                    serialNumberConverter = mockk(relaxed = true),
                    scannerGenerationDeterminer = mockk(relaxed = true),
                    scannerInitialSetupHelper = mockk(relaxed = true),
                    connectionHelper = mockk(relaxed = true),
                    cypressOtaHelper = mockk(relaxed = true),
                    stmOtaHelper = mockk(relaxed = true),
                    un20OtaHelper = mockk(relaxed = true)
                )).apply {
                    coEvery { create(any()) } answers {
                        val macAddress = args[0] as String
                        when (simulationMode) {
                            SimulationMode.V1 -> createScannerV1(macAddress)
                            SimulationMode.V2 -> createScannerV2(macAddress)
                        }
                    }
                }
            }
            single<ScannerManager> { ScannerManagerImpl(simulatedBluetoothAdapter, get(), get(), get()) }
            factory { masterFlowManager }
            factory { dbManagerMock }
        })
        setupMasterFlowManager(action)
        setupDbManagerMock()
    }

    private fun setupMasterFlowManager(action: Action) {
        every { masterFlowManager.getCurrentAction() } returns action
    }

    private fun setupDbManagerMock() {
        with(dbManagerMock) {
            coEvery { loadPeople(anyNotNull()) } answers  {
                val query = args[0] as SubjectQuery
                val numberOfPeopleToLoad = if (query.subjectId == null) NUMBER_OF_PEOPLE_IN_DB else 1
                FingerprintGenerator.generateRandomFingerprintRecords(numberOfPeopleToLoad).asFlow()
            }
        }
    }

    @Test
    fun captureFlow_withScannerV1_finishesSuccessfully() {
        setupMocksAndKoinModules(SimulationMode.V1, Action.ENROL)
        assertCaptureFlowFinishesSuccessfully()
    }

    @Test
    fun captureFlow_withScannerV2_finishesSuccessfully() {
        setupMocksAndKoinModules(SimulationMode.V2, Action.ENROL)
        assertCaptureFlowFinishesSuccessfully()
    }

    @Test
    fun matchFlow_identify_finishesSuccessfully() {
        setupMocksAndKoinModules(SimulationMode.V2, Action.IDENTIFY)
        assertIdentifyFlowFinishesSuccessfully()
    }

    @Test
    fun matchFlow_verify_finishesSuccessfully() {
        setupMocksAndKoinModules(SimulationMode.V2, Action.VERIFY)
        assertVerifyFlowFinishesSuccessfully()
    }

    private fun assertCaptureFlowFinishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintCaptureRequestIntent())

        waitUntilCollectFingerprintsIsDisplayed()
        pressScanUntilDialogIsDisplayedAndClickConfirm()

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintCaptureResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            val captureResponse = resultData?.extras?.getParcelable<IFingerprintCaptureResponse>(IFingerprintResponse.BUNDLE_KEY)
            with(captureResponse) {
                assertNotNull(captureResponse)
                assertEquals(IFingerprintResponseType.CAPTURE, this?.type)
            }
        }
    }

    private fun assertIdentifyFlowFinishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintMatchRequestIntent(
            FingerprintGenerator.generateRandomFingerprints(2),
            SubjectQuery(projectId = DEFAULT_PROJECT_ID)
        ))

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintMatchResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintMatchResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.MATCH, type)
            })
        }
    }

    private fun assertVerifyFlowFinishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintMatchRequestIntent(
            FingerprintGenerator.generateRandomFingerprints(2),
            SubjectQuery(subjectId = UUID.randomUUID().toString())
        ))

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintMatchResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintMatchResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.MATCH, type)
            })
        }
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        releaseFingerprintKoinModules()
    }

    companion object {
        private const val NUMBER_OF_PEOPLE_IN_DB = 120

        private const val DEFAULT_PROJECT_ID = "TESTzbq8ZBOs1LLOOH6p"
    }
}
