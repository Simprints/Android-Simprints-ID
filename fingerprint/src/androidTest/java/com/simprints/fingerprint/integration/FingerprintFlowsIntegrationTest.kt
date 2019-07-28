package com.simprints.fingerprint.integration

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.simprints.fingerprint.activities.collectfingerprint.takeScansAndConfirm
import com.simprints.fingerprint.activities.launch.setupActivityAndContinue
import com.simprints.fingerprint.activities.orchestrator.OrchestratorActivity
import com.simprints.fingerprint.commontesttools.di.TestFingerprintCoreModule
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.commontesttools.generators.PeopleGeneratorUtils
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.data.domain.Action
import com.simprints.fingerprint.testtools.AndroidTestConfig
import com.simprints.fingerprintscannermock.simulated.SimulatedBluetoothAdapter
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.moduleapi.fingerprint.responses.*
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class FingerprintFlowsIntegrationTest {

    @Inject lateinit var dbManagerMock: FingerprintDbManager

    @get:Rule var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val fingerprintModule by lazy {
        TestFingerprintModule(
            bluetoothComponentAdapter = DependencyRule.ReplaceRule { SimulatedBluetoothAdapter(SimulatedScannerManager()) }
        )
    }

    private val fingerprintCoreModule by lazy {
        TestFingerprintCoreModule(
            fingerprintDbManagerRule = DependencyRule.MockRule)
    }

    @Before
    fun setUp() {
        AndroidTestConfig(this, fingerprintModule, fingerprintCoreModule).fullSetup()
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
        val scenario = ActivityScenario.launch<OrchestratorActivity>(createFingerprintRequestIntent(Action.ENROL))

        setupActivityAndContinue()
        takeScansAndConfirm()

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
        val scenario = ActivityScenario.launch<OrchestratorActivity>(createFingerprintRequestIntent(Action.IDENTIFY))

        setupActivityAndContinue()
        takeScansAndConfirm()

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
        val scenario = ActivityScenario.launch<OrchestratorActivity>(createFingerprintRequestIntent(Action.VERIFY))

        setupActivityAndContinue()
        takeScansAndConfirm()

        with(scenario.result) {
            resultData.setExtrasClassLoader(IFingerprintVerifyResponse::class.java.classLoader)
            assertEquals(Activity.RESULT_OK, resultCode)
            assertNotNull(resultData?.extras?.getParcelable<IFingerprintVerifyResponse>(IFingerprintResponse.BUNDLE_KEY)?.apply {
                assertEquals(IFingerprintResponseType.VERIFY, type)
            })
        }
    }

    companion object {
        private const val NUMBER_OF_PEOPLE_IN_DB = 120
    }
}
