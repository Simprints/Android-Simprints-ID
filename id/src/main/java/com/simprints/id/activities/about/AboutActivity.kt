package com.simprints.id.activities.about

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AppCompatActivity(), AboutContract.View {

    override lateinit var viewPresenter: AboutContract.Presenter

    private lateinit var recoveryDialog: SimProgressDialog
    private lateinit var errorDialog: AlertDialog
    private lateinit var successDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as Application
        val dataManager = app.dataManager
        LanguageHelper.setLanguage(this, dataManager.language)

        setContentView(R.layout.activity_about)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setSupportActionBar(toolbar_about)
        supportActionBar?.let {
            it.show()
            it.setDisplayHomeAsUpEnabled(true)
        }

        initUi()

        viewPresenter = AboutPresenter(this, dataManager)
    }

    private fun initUi() {
        setRecoverButtonListener()
        recoveryDialog = SimProgressDialog(this)
        errorDialog = createRecoveryErrorDialog()
        successDialog = createRecoverySuccessDialog()
    }

    private fun setRecoverButtonListener() =
        bt_recoverDb.setOnClickListener {
            viewPresenter.recoverDb()
        }

    private fun createRecoveryErrorDialog(): AlertDialog =
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.error_recovery_message))
            .setNegativeButton(R.string.dismiss) { dialog, _ -> dialog.dismiss() }.create()

    private fun createRecoverySuccessDialog(): AlertDialog =
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.success_recovery_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }.create()

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun setVersionData(appVersion: String, libsimprintsVersion: String, scannerVersion: String) {
        tv_appVersion.text = appVersion
        tv_libsimprintsVersion.text = libsimprintsVersion
        tv_scannerVersion.text = scannerVersion
    }

    override fun setDbCountData(userCount: String, moduleCount: String, globalCount: String) {
        tv_userDbCount.text = userCount
        tv_moduleDbCount.text = moduleCount
        tv_globalDbCount.text = globalCount
    }

    override fun setStartRecovering() {
        recoveryDialog.show()
        bt_recoverDb.isEnabled = false
    }

    override fun setSuccessRecovering() =
        runOnUiThreadIfStillRunning {
            recoveryDialog.dismiss()
            successDialog.show()
            bt_recoverDb.isEnabled = true
        }

    override fun setRecoveringFailed() =
        runOnUiThreadIfStillRunning {
            recoveryDialog.dismiss()
            errorDialog.show()
            bt_recoverDb.isEnabled = true
        }

    override fun setRecoveryAvailability(recoveryRunning: Boolean) =
        runOnUiThreadIfStillRunning {
            bt_recoverDb.isEnabled = !recoveryRunning
        }
}
