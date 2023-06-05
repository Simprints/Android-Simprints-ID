package com.simprints.feature.login.tools.play

import android.app.Activity
import android.content.DialogInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.ConnectionResult.*
import com.google.android.gms.common.GoogleApiAvailability
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.tools.play.GooglePlayServicesAvailabilityChecker.Companion.GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE
import com.simprints.infra.logging.Simber
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GooglePlayServicesAvailabilityCheckerTest {

    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var googleApiAvailability: GoogleApiAvailability

    @MockK
    private lateinit var launchCallBack: (LoginError) -> Unit

    private lateinit var googlePlayServicesAvailabilityChecker: GooglePlayServicesAvailabilityChecker


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(Simber)

        every { launchCallBack.invoke(any()) } returns Unit
        googlePlayServicesAvailabilityChecker = GooglePlayServicesAvailabilityChecker(googleApiAvailability)
    }

    @Test
    fun `check does nothing if google play services is installed and updated`() {
        //Given
        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns SUCCESS
        //When
        googlePlayServicesAvailabilityChecker.check(activity, launchCallBack)
        //Then

        verify(exactly = 0) {
            googleApiAvailability.showErrorDialogFragment(
                activity, any(),
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify(exactly = 0) { launchCallBack(any()) }
    }

    @Test
    fun `check shows alert and logs MissingGooglePlayServices exception for missing google play services`() {
        //Given

        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns INTERNAL_ERROR
        every { googleApiAvailability.isUserResolvableError(INTERNAL_ERROR) } returns false

        //When
        googlePlayServicesAvailabilityChecker.check(activity, launchCallBack)

        //Then
        verify(exactly = 0) {
            googleApiAvailability.showErrorDialogFragment(
                activity, any(),
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify { launchCallBack(LoginError.MissingPlayServices) }
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
        googlePlayServicesAvailabilityChecker.check(activity, launchCallBack)

        //Then
        verify(exactly = 1) {
            googleApiAvailability.showErrorDialogFragment(
                activity, SERVICE_VERSION_UPDATE_REQUIRED,
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify(exactly = 0) { launchCallBack(any()) }
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
        googlePlayServicesAvailabilityChecker.check(activity, launchCallBack)

        //Then
        verify(exactly = 1) {
            googleApiAvailability.showErrorDialogFragment(
                activity, SERVICE_VERSION_UPDATE_REQUIRED,
                GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE, any()
            )
        }
        verify { launchCallBack(LoginError.OutdatedPlayServices) }
        verify { Simber.e(ofType<OutdatedGooglePlayServices>()) }
    }

}
