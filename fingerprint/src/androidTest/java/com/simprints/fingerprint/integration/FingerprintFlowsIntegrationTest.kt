package com.simprints.fingerprint.integration

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.activities.collect.pressScanUntilDialogIsDisplayedAndClickConfirm
import com.simprints.fingerprint.activities.collect.waitUntilCollectFingerprintsIsDisplayed
import com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.factory.ScannerFactoryImpl
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.SimulationMode
import com.simprints.fingerprintscannermock.simulated.component.SimulatedBluetoothAdapter
import com.simprints.moduleapi.fingerprint.responses.*
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.anyOrNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenThis
import io.reactivex.Single
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare

@RunWith(AndroidJUnit4::class)
@LargeTest
class FingerprintFlowsIntegrationTest : KoinTest {

    private val dbManagerMock: FingerprintDbManager = mock()

    @get:Rule var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var scenario: ActivityScenario<OrchestratorActivity>

    @Before
    fun setUp() {
        acquireFingerprintKoinModules()
        setupDbManagerMock()
    }

    private fun setupSimulatedBluetoothAdapterAndKoinModules(simulationMode: SimulationMode) {
        val simulatedBluetoothAdapter = SimulatedBluetoothAdapter(SimulatedScannerManager(simulationMode))
        declare {
            single<ScannerFactory> {
                spy(ScannerFactoryImpl(simulatedBluetoothAdapter, get())).apply {
                    whenThis { create(anyNotNull()) } then {
                        val macAddress = it.arguments[0] as String
                        when (simulationMode) {
                            SimulationMode.V1 -> createScannerV1(macAddress)
                            SimulationMode.V2 -> createScannerV2(macAddress)
                        }
                    }
                }
            }
            single<ScannerManager> { ScannerManagerImpl(simulatedBluetoothAdapter, get()) }
            factory { dbManagerMock }
        }
    }

    private fun setupDbManagerMock() {
        with(dbManagerMock) {
            whenThis { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.just(PersonFetchResult(
                PeopleGeneratorUtils.getRandomPerson(), false
            ))
            whenThis { loadPeople(anyNotNull(), anyOrNull(), anyOrNull()) } thenReturn Single.just(
                PeopleGeneratorUtils.getRandomPeople(NUMBER_OF_PEOPLE_IN_DB)
            )
        }
    }

    @Test
    fun enrolFlow_withScannerV1_finishesSuccessfully() {
        setupSimulatedBluetoothAdapterAndKoinModules(SimulationMode.V1)
        assertEnrolFlowFinishesSuccessfully()
    }

    @Test
    fun enrolFlow_withScannerV2_finishesSuccessfully() {
        setupSimulatedBluetoothAdapterAndKoinModules(SimulationMode.V2)
        assertEnrolFlowFinishesSuccessfully()
    }

    @Test
    fun identifyFlow_withScannerV1_finishesSuccessfully() {
        setupSimulatedBluetoothAdapterAndKoinModules(SimulationMode.V1)
        assertIdentifyFlowFinishesSuccessfully()
    }

    @Test
    fun identifyFlow_withScannerV2_finishesSuccessfully() {
        setupSimulatedBluetoothAdapterAndKoinModules(SimulationMode.V2)
        assertIdentifyFlowFinishesSuccessfully()
    }

    @Test
    fun verifyFlow_withScannerV1_finishesSuccessfully() {
        setupSimulatedBluetoothAdapterAndKoinModules(SimulationMode.V1)
        assertVerifyFlowFinishesSuccessfully()
    }

    @Test
    fun verifyFlow_withScannerV2_finishesSuccessfully() {
        setupSimulatedBluetoothAdapterAndKoinModules(SimulationMode.V2)
        assertVerifyFlowFinishesSuccessfully()
    }

    private fun assertIdentifyFlowFinishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.IDENTIFY))

        waitUntilCollectFingerprintsIsDisplayed()
        pressScanUntilDialogIsDisplayedAndClickConfirm()

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintIdentifyResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintIdentifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.IDENTIFY, type)
            })
        }
    }

    private fun assertVerifyFlowFinishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.VERIFY))

        waitUntilCollectFingerprintsIsDisplayed()
        pressScanUntilDialogIsDisplayedAndClickConfirm()

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintVerifyResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintVerifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.VERIFY, type)
            })
        }
    }

    private fun assertEnrolFlowFinishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.ENROL))

        waitUntilCollectFingerprintsIsDisplayed()
        pressScanUntilDialogIsDisplayedAndClickConfirm()

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintEnrolResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintEnrolResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.ENROL, type)
            })
        }

        verifyOnce(dbManagerMock) { savePerson(anyNotNull()) }
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        releaseFingerprintKoinModules()
    }

    companion object {
        private const val NUMBER_OF_PEOPLE_IN_DB = 120
    }
}
