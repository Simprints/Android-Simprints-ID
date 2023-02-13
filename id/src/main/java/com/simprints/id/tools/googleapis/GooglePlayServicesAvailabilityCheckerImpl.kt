package com.simprints.id.tools.googleapis

import android.app.Activity
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.domain.alert.AlertType.GOOGLE_PLAY_SERVICES_OUTDATED
import com.simprints.id.domain.alert.AlertType.MISSING_GOOGLE_PLAY_SERVICES
import com.simprints.id.exceptions.unexpected.MissingGooglePlayServices
import com.simprints.id.exceptions.unexpected.OutdatedGooglePlayServices
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class GooglePlayServicesAvailabilityCheckerImpl @Inject constructor(
    private val googleApiAvailability: GoogleApiAvailability
) : GooglePlayServicesAvailabilityChecker {

    /**
     * Check the availability of the google play services.
     *
     * @param activity
     */
    override fun check(activity: Activity) {
        val statusCode = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        when {
            statusCode == SUCCESS -> { // On SUCCESS then does nothing
                return
            }
            googleApiAvailability.isUserResolvableError(statusCode) -> {
                showErrorDialog(activity, statusCode)
            }
            else -> {
                handelMissingGooglePlayServices(activity, statusCode)
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
    private fun showErrorDialog(
        activity: Activity,
        statusCode: Int
    ) {
        googleApiAvailability.showErrorDialogFragment(
            activity,
            statusCode,
            GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE
        ) {
            // Throw exception when user cancels the dialog
            handleCancellation(activity, statusCode)
        }
    }

    /**
     * Handel non resolvable errors, then throw exception
     *
     * @param activity
     * @param statusCode
     */
    private fun handelMissingGooglePlayServices(
        activity: Activity,
        statusCode: Int
    ) {
        launchAlert(activity, MISSING_GOOGLE_PLAY_SERVICES)
        Simber.e(
            MissingGooglePlayServices(
                "Error with GooglePlayServices version. Error code=$statusCode"
            )
        )
    }

    private fun handleCancellation(activity: Activity, statusCode: Int) {
        launchAlert(activity, GOOGLE_PLAY_SERVICES_OUTDATED)
        Simber.e(
            OutdatedGooglePlayServices(
                "Error with GooglePlayServices version. Error code=$statusCode"
            )
        )
    }

    // Should be any number we user to check if the user updated
    companion object {
        const val GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE: Int = 123
    }
}
