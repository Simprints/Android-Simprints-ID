package com.simprints.id.activities.main

import android.content.Context
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

        val builder = AlertDialog.Builder(ctx)
            .setTitle(R.string.add_finger_dialog_title)
            .setMultiChoiceItems(
                optionNames,
                optionChecks,
                { dialogInterface, i, isChecked ->

                    val fingerOption = options[i]
                    fingerOption.active = fingerOption.required || isChecked

                    setCheckStateForDialogRow((dialogInterface as AlertDialog), i, fingerOption.active)
                })
            .setPositiveButton(R.string.ok) { _, _ ->
                val persistentOption = options.last()
                options.remove(persistentOption)
                onPositiveButton(persistentOption.active, options)
            }
        return builder.create()
    }

    private fun setCheckStateForDialogRow(dialogInterface: AlertDialog, i: Int, active: Boolean) {
        dialogInterface.listView.setItemChecked(i, active)
    }
}
