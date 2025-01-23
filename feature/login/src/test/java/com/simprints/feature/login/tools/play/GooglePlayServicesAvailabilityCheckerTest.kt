package com.simprints.feature.login.tools.play

import android.app.Activity
import android.content.DialogInterface
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.ConnectionResult.INTERNAL_ERROR
import com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import com.simprints.feature.login.LoginError
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GooglePlayServicesAvailabilityCheckerTest {
    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var checkForPlayServicesResultLauncher: ActivityResultLauncher<IntentSenderRequest>

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

    @After
    fun cleanUp() {
        unmockkObject(Simber)
    }

    @Test
    fun `check does nothing if google play services is installed and updated`() {
        // Given
        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns SUCCESS
        // When
        googlePlayServicesAvailabilityChecker.check(activity, checkForPlayServicesResultLauncher, launchCallBack)
        // Then

        verify(exactly = 0) {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                any(),
                checkForPlayServicesResultLauncher,
                any(),
            )
        }
        verify(exactly = 0) { launchCallBack(any()) }
    }

    @Test
    fun `check shows alert and logs exception for missing google play services`() {
        // Given

        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns INTERNAL_ERROR
        every { googleApiAvailability.isUserResolvableError(INTERNAL_ERROR) } returns false

        // When
        googlePlayServicesAvailabilityChecker.check(activity, checkForPlayServicesResultLauncher, launchCallBack)

        // Then
        verify(exactly = 0) {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                any(),
                checkForPlayServicesResultLauncher,
                any(),
            )
        }
        verify { launchCallBack(LoginError.MissingPlayServices) }
        verify { Simber.e(any(), ofType<MissingGooglePlayServices>(), any<CrashReportTag>()) }
    }

    @Test
    fun `check shows errorDialog for outdated google play services`() {
        // Given
        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns SERVICE_VERSION_UPDATE_REQUIRED
        every { googleApiAvailability.isUserResolvableError(SERVICE_VERSION_UPDATE_REQUIRED) } returns true
        every {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                SERVICE_VERSION_UPDATE_REQUIRED,
                checkForPlayServicesResultLauncher,
                any(),
            )
        } returns true

        // When
        googlePlayServicesAvailabilityChecker.check(activity, checkForPlayServicesResultLauncher, launchCallBack)

        // Then
        verify(exactly = 1) {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                SERVICE_VERSION_UPDATE_REQUIRED,
                checkForPlayServicesResultLauncher,
                any(),
            )
        }
        verify(exactly = 0) { launchCallBack(any()) }
        verify(exactly = 0) { Simber.e(any(), ofType<OutdatedGooglePlayServices>(), any<CrashReportTag>()) }
    }

    @Test
    fun `check shows alert and logs exception for outdated google play services and the user cancels the alert dialog`() {
        // Given
        every { googleApiAvailability.isGooglePlayServicesAvailable(activity) } returns SERVICE_VERSION_UPDATE_REQUIRED
        every { googleApiAvailability.isUserResolvableError(SERVICE_VERSION_UPDATE_REQUIRED) } returns true
        val cancellationListenerSlot = slot<DialogInterface.OnCancelListener>()
        every {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                SERVICE_VERSION_UPDATE_REQUIRED,
                checkForPlayServicesResultLauncher,
                capture(cancellationListenerSlot),
            )
        } answers {
            cancellationListenerSlot.captured.onCancel(mockk())
            true
        }
        // When
        googlePlayServicesAvailabilityChecker.check(activity, checkForPlayServicesResultLauncher, launchCallBack)

        // Then
        verify(exactly = 1) {
            googleApiAvailability.showErrorDialogFragment(
                activity,
                SERVICE_VERSION_UPDATE_REQUIRED,
                checkForPlayServicesResultLauncher,
                any(),
            )
        }
        verify { launchCallBack(LoginError.OutdatedPlayServices) }
        verify { Simber.e(any(), ofType<OutdatedGooglePlayServices>(), any<CrashReportTag>()) }
    }
}
