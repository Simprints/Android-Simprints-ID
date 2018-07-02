package com.simprints.id.activities.collectFingerprints.fingers

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import com.simprints.id.R

class AddFingerDialog(private val ctx: Context,
                      fingerOptions: ArrayList<FingerDialogOption>,
                      persistFingerState: Boolean,
                      private val onPositiveButton: (persistFingerState: Boolean, ArrayList<FingerDialogOption>) -> Unit) {

    private val options = ArrayList<FingerDialogOption>()

    init {
        val persist = FingerDialogOption(ctx.getString(R.string.persistence_label), null, false, persistFingerState)
        options.addAll(fingerOptions)
        options.add(persist)
    }

    fun create(): AlertDialog {
        val optionChecks = options.map { it.active }.toBooleanArray()
        val optionNames = options.map { it.name }.toTypedArray()

        return AlertDialog.Builder(ctx)
            .setTitle(R.string.add_finger_dialog_title)
            .setMultiChoiceItems(optionNames, optionChecks, multiChoiceClickListener())
            .setPositiveButton(R.string.ok, handlePositiveButtonClick()).create()
    }

    private fun multiChoiceClickListener(): (DialogInterface, Int, Boolean) -> Unit =
        { dialogInterface, i, isChecked ->
            val fingerOption = options[i]
            fingerOption.active = fingerOption.required || isChecked
            setCheckStateForDialogRow((dialogInterface as AlertDialog), i, fingerOption.active)
        }

    private fun handlePositiveButtonClick(): (DialogInterface, Int) -> Unit = { _, _ ->
        val persistentOption = options.last()
        options.remove(persistentOption)
        onPositiveButton(persistentOption.active, options)
    }

    private fun setCheckStateForDialogRow(dialogInterface: AlertDialog, i: Int, active: Boolean) {
        dialogInterface.listView.setItemChecked(i, active)
    }
}
