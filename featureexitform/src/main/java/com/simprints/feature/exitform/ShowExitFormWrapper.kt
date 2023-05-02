package com.simprints.feature.exitform

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.simprints.feature.exitform.screen.ExitFormWrapperActivity

/**
 * Activity result contract for modules that do not implement fragment
 * based navigation and cannot use fragment result API.
 *
 * Usage in activity:
 * ```
 * private val showExitForm = registerForActivityResult(ShowExitFormWrapper()) { d ->
 *   val isSubmitted = ExitFormContract.isFormSubmitted(d)
 * }
 *
 * showExitForm.launch(exitFormConfiguration { /* configuration */ }.toArgs())
 * ```
 *
 * Input is the same configuration args bundle that is used in fragment navigation.
 * Output is the same response bundle that is returned in fragment result listener.
 */
class ShowExitFormWrapper : ActivityResultContract<Bundle, Bundle>() {
    override fun createIntent(context: Context, input: Bundle): Intent =
        Intent(context, ExitFormWrapperActivity::class.java)
            .also { it.putExtra(ExitFormWrapperActivity.EXIT_FORM_ARGS_EXTRA, input) }

    override fun parseResult(resultCode: Int, intent: Intent?): Bundle = intent?.extras ?: Bundle()
}
