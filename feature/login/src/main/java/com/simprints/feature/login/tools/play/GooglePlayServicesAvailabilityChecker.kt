package com.simprints.feature.login.tools.play

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import com.simprints.feature.login.LoginError
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class GooglePlayServicesAvailabilityChecker @Inject constructor(
    private val googleApiAvailability: GoogleApiAvailability,
) {
    /**
     * Check the availability of the google play services.
     *
     * @param activity
     */
    fun check(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        errorCallback: (LoginError) -> Unit,
    ) {
        val statusCode = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        when {
            statusCode == SUCCESS -> { // On SUCCESS then does nothing
                return
            }

            googleApiAvailability.isUserResolvableError(statusCode) -> {
                showErrorDialog(activity, statusCode, activityResultLauncher, errorCallback)
            }

            else -> {
                handleMissingGooglePlayServices(statusCode, errorCallback)
            }
        }
    }

    /**
     * Show error dialog that describes all resolvable cases and have a button that does the appropriate action
     * SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED,  SERVICE_INVALID
     *
     * @param activity
     * @param statusCode
     */
    private inline fun showErrorDialog(
        activity: Activity,
        statusCode: Int,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        crossinline errorCallback: (LoginError) -> Unit,
    ) {
        googleApiAvailability.showErrorDialogFragment(
            activity,
            statusCode,
            activityResultLauncher,
        ) {
            // Throw exception when user cancels the dialog
            handleCancellation(statusCode, errorCallback)
        }
    }

    /**
     * Handle non resolvable errors, then throw exception
     *
     * @param statusCode
     */
    private inline fun handleMissingGooglePlayServices(
        statusCode: Int,
        crossinline errorCallback: (LoginError) -> Unit,
    ) {
        errorCallback(LoginError.MissingPlayServices)
        Simber.e(
            "Missing GooglePlay services",
            MissingGooglePlayServices("Error with GooglePlayServices version. Error code=$statusCode"),
            tag = LOGIN,
        )
    }

    private inline fun handleCancellation(
        statusCode: Int,
        crossinline errorCallback: (LoginError) -> Unit,
    ) {
        errorCallback(LoginError.OutdatedPlayServices)
        Simber.e(
            "Outdated GooglePlay services",
            OutdatedGooglePlayServices("Error with GooglePlayServices version. Error code=$statusCode"),
            tag = LOGIN,
        )
    }
}
