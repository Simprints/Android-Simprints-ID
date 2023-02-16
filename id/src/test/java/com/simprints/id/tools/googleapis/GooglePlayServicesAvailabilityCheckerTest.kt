package com.simprints.id.tools.googleapis

import android.app.Activity
import android.content.DialogInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.ConnectionResult.*
import com.google.android.gms.common.GoogleApiAvailability
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exceptions.unexpected.MissingGooglePlayServices
import com.simprints.id.exceptions.unexpected.OutdatedGooglePlayServices
import com.simprints.id.tools.googleapis.GooglePlayServicesAvailabilityCheckerImpl.Companion.GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE
import com.simprints.infra.logging.Simber
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GooglePlayServicesAvailabilityCheckerTest {

    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var googleApiAvailability: GoogleApiAvailability

    private lateinit var googlePlayServicesAvailabilityChecker: GooglePlayServicesAvailabilityChecker


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(AlertActivityHelper)
        mockkObject(Simber)
        googlePlayServicesAvailabilityChecker =
            GooglePlayServicesAvailabilityCheckerImpl(googleApiAvailability)
    }

    @Test
    fun `check does nothing if google play services is installed and updated`() {
        //Given
        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns SUCCESS
        //When
        googlePlayServicesAvailabilityChecker.check(activity)
        //Then

        verify(exactly = 0) {
            googleApiAvailability.showErrorDialogFragment(
                activity, any(),
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify(exactly = 0) {
            AlertActivityHelper.launchAlert(activity, any())
        }
    }

    @Test
    fun `check shows alert and logs MissingGooglePlayServices exception for missing google play services`() {
        //Given

        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns INTERNAL_ERROR
        every { googleApiAvailability.isUserResolvableError(INTERNAL_ERROR) } returns false

        //When
        googlePlayServicesAvailabilityChecker.check(activity)

        //Then
        verify(exactly = 0) {
            googleApiAvailability.showErrorDialogFragment(
                activity, any(),
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify {
            AlertActivityHelper.launchAlert(activity, AlertType.MISSING_GOOGLE_PLAY_SERVICES)
        }
        verify { Simber.e(ofType<MissingGooglePlayServices>()) }
    }

    @Test
    fun `check shows errorDialog for outdated google play services`() {
        //Given
        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns SERVICE_VERSION_UPDATE_REQUIRED
        every { googleApiAvailability.isUserResolvableError(SERVICE_VERSION_UPDATE_REQUIRED) } returns true
        every {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                SERVICE_VERSION_UPDATE_REQUIRED, GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        } returns true

        //When
        googlePlayServicesAvailabilityChecker.check(activity)

        //Then
        verify(exactly = 1) {
            googleApiAvailability.showErrorDialogFragment(
                activity, SERVICE_VERSION_UPDATE_REQUIRED,
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify(exactly = 0) {
            AlertActivityHelper.launchAlert(activity, AlertType.GOOGLE_PLAY_SERVICES_OUTDATED)
        }
        verify(exactly = 0) { Simber.e(ofType<OutdatedGooglePlayServices>()) }
    }


    @Test
    fun `check shows alert and logs OutdatedGooglePlayServices exception for outdated google play services and the user cancels the alert dialog`() {
        //Given
        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns SERVICE_VERSION_UPDATE_REQUIRED
        every { googleApiAvailability.isUserResolvableError(SERVICE_VERSION_UPDATE_REQUIRED) } returns true
        val cancellationListenerSlot = slot<DialogInterface.OnCancelListener>()
        every {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                SERVICE_VERSION_UPDATE_REQUIRED,
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE,
                capture(cancellationListenerSlot)
            )
        } answers {
            cancellationListenerSlot.captured.onCancel(mockk())
            true
        }
        //When
        googlePlayServicesAvailabilityChecker.check(activity)

        //Then
        verify(exactly = 1) {
            googleApiAvailability.showErrorDialogFragment(
                activity, SERVICE_VERSION_UPDATE_REQUIRED,
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify {
            AlertActivityHelper.launchAlert(activity, AlertType.GOOGLE_PLAY_SERVICES_OUTDATED)
        }
        verify { Simber.e(ofType<OutdatedGooglePlayServices>()) }
    }

}
