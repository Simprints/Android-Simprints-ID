package com.simprints.fingerprint.integration

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.activities.collectfingerprint.pressScanUntilDialogIsDisplayedAndClickConfirm
import com.simprints.fingerprint.activities.launch.setupActivityAndContinue
import com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity
import com.simprints.fingerprint.commontesttools.generators.PeopleGeneratorUtils
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.di.KoinInjector.loadFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.unloadFingerprintKoinModules
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscannermock.simulated.SimulatedBluetoothAdapter
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.moduleapi.fingerprint.responses.*
import com.simprints.testtools.common.syntax.*
import io.reactivex.Completable
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
class FingerprintFlowsIntegrationTest: KoinTest {

    private val dbManagerMock: FingerprintDbManager = mock()

    @get:Rule var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var scenario: ActivityScenario<OrchestratorActivity>

    @Before
    fun setUp() {
        loadFingerprintKoinModules()
        declare {
            factory<BluetoothComponentAdapter> { SimulatedBluetoothAdapter(SimulatedScannerManager()) }
            factory { dbManagerMock }
        }
        setupDbManagerMock()
    }

    private fun setupDbManagerMock() {
        with(dbManagerMock) {
            whenThis { loadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.just(PersonFetchResult(
                PeopleGeneratorUtils.getRandomPerson(), false
            ))
            whenThis { loadPeople(anyNotNull(), anyOrNull(), anyOrNull()) } thenReturn Single.just(
                PeopleGeneratorUtils.getRandomPeople(NUMBER_OF_PEOPLE_IN_DB)
            )
            whenThis { savePerson(anyNotNull()) } thenReturn Completable.complete()
        }
    }

    @Test
    fun enrolFlow_finishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.ENROL))

        setupActivityAndContinue()
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

    @Test
    fun identifyFlow_finishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.IDENTIFY))

        setupActivityAndContinue()
        pressScanUntilDialogIsDisplayedAndClickConfirm()

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintIdentifyResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintIdentifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.IDENTIFY, type)
            })
        }
    }

    @Test
    fun verifyFlow_finishesSuccessfully() {
        scenario = ActivityScenario.launch(createFingerprintRequestIntent(Action.VERIFY))

        setupActivityAndContinue()
        pressScanUntilDialogIsDisplayedAndClickConfirm()

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintVerifyResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintVerifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.VERIFY, type)
            })
        }
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        unloadFingerprintKoinModules()
    }

    companion object {
        private const val NUMBER_OF_PEOPLE_IN_DB = 120
    }
}
