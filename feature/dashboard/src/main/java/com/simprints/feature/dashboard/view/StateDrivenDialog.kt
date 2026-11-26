package com.simprints.feature.dashboard.view

import androidx.appcompat.app.AlertDialog
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

/**
 * Shows an [AlertDialog] while the screen state says it is visible.
 *
 * The visibility lives in the ViewModel state, so after view re-creation (rotation) the next render shows the dialog again.
 * [release] closes the window with the view without reporting anything, so the state keeps the dialog visible.
 * [onCancelledByUser] is called when the user cancels the dialog: back, a touch outside, or a cancel button that calls
 * `DialogInterface.cancel()`. A choice (a confirm button or a list pick) is reported by its own action, which also hides
 * the dialog in the state, so it is not reported a second time here.
 *
 * The reference is kept until the state says hidden, so a render between a button click and the matching action
 * never shows the dialog a second time.
 *
 * Contract: every way the user can close the dialog must either cancel it or send an action that hides it in the state.
 * A close that does neither leaves the state visible: the dialog cannot be opened again, and it reappears on the next
 * view re-creation.
 */
@ExcludedFromGeneratedTestCoverageReports("UI class")
internal class StateDrivenDialog(
    private val onCancelledByUser: () -> Unit,
) {
    private var dialog: AlertDialog? = null

    fun render(
        isVisible: Boolean,
        create: () -> AlertDialog,
    ) {
        when {
            isVisible && dialog == null -> dialog = create().apply {
                setOnCancelListener { onCancelledByUser() }
                show()
            }

            !isVisible && dialog != null -> release()
        }
    }

    // Call from onDestroyView.
    fun release() {
        // dismiss() does not call the cancel listener
        dialog?.dismiss()
        dialog = null
    }
}
