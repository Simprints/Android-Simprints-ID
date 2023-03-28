package com.simprints.feature.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.simprints.feature.alert.intent.AlertWrapperActivity

class ShowAlertWrapper : ActivityResultContract<Bundle, Bundle>() {
    override fun createIntent(context: Context, input: Bundle): Intent =
        Intent(context, AlertWrapperActivity::class.java)
            .also { it.putExtra(AlertWrapperActivity.ALERT_ARGS_EXTRA, input) }

    override fun parseResult(resultCode: Int, intent: Intent?): Bundle = intent?.extras ?: Bundle()
}
