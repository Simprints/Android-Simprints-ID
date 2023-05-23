package com.simprints.feature.login.tools.play

import android.app.Activity
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import com.simprints.feature.login.LoginError
import com.simprints.infra.logging.Simber
import javax.inject.Inject

// TODO make internal when id.LoginActivity is deleted
class GooglePlayServicesAvailabilityChecker @Inject constructor(
    private val googleApiAvailability: GoogleApiAvailability
) {

    /**
     * Check the availability of the google play services.
     *
     * @param activity
     */
    fun check(
        activity: Activity,
        errorCallback: (LoginError) -> Unit,
    ) {
        val statusCode = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        when {
            statusCode == SUCCESS -> { // On SUCCESS then does nothing
                return
            }

            googleApiAvailability.isUserResolvableError(statusCode) -> {
                showErrorDialog(activity, statusCode, errorCallback)
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
        crossinline errorCallback: (LoginError) -> Unit,
    ) {
        googleApiAvailability.showErrorDialogFragment(
            activity,
            statusCode,
            GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE
        ) {
            // Throw exception when user cancels the dialog
            handleCancellation(statusCode, errorCallback)
        }
    }

    /**
     * Handle non resolvable errors, then throw exception
     *
     * @param activity
     * @param statusCode
     */
    private inline fun handleMissingGooglePlayServices(
        statusCode: Int,
        crossinline errorCallback: (LoginError) -> Unit,
    ) {
        errorCallback(LoginError.MissingPlayServices)
        Simber.e(MissingGooglePlayServices(
            "Error with GooglePlayServices version. Error code=$statusCode"
        ))
    }

    private inline fun handleCancellation(
        statusCode: Int,
        crossinline errorCallback: (LoginError) -> Unit,
    ) {
        errorCallback(LoginError.OutdatedPlayServices)
        Simber.e(OutdatedGooglePlayServices(
            "Error with GooglePlayServices version. Error code=$statusCode"
        ))
    }

    // Should be any number we user to check if the user updated
    companion object {
        const val GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE: Int = 123
    }
}
