package com.simprints.feature.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.simprints.feature.alert.intent.AlertWrapperActivity

/**
 * Activity result contract for modules that do not implement fragment
 * based navigation and cannot use fragment result API.
 *
 * Usage in activity:
 * ```
 * private val showAlert = registerForActivityResult(ShowAlertWrapper()) { d ->
 *   val key = AlertContract.getResponseKey(d)
 *   val payload = AlertContract.getResponsePayload(d)
 *   val hasExactKey = AlertContract.hasResponseKey(d, "action_key")
 * }
 *
 * showAlert.launch(alertConfiguration { /* configuration */ }.toArgs())
 * ```
 *
 * Input is the same alert configuration args bundle that is used in fragment navigation.
 * Output is the same response bundle that is returned in fragment result listener.
 *
 * Note that since this wrapper relies on activity result API all action buttons should
 * set `closeOnClick = true` otherwise those clicks will not be delivered.
 */
class ShowAlertWrapper : ActivityResultContract<Bundle, Bundle>() {
    override fun createIntent(context: Context, input: Bundle): Intent =
        Intent(context, AlertWrapperActivity::class.java)
            .also { it.putExtra(AlertWrapperActivity.ALERT_ARGS_EXTRA, input) }

    override fun parseResult(resultCode: Int, intent: Intent?): Bundle = intent?.extras ?: Bundle()
}
