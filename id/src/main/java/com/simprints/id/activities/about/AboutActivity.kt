package com.simprints.id.activities.about

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_about.*
import javax.inject.Inject

class AboutActivity : AppCompatActivity(), AboutContract.View {

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var preferencesManager: PreferencesManager

    override lateinit var viewPresenter: AboutContract.Presenter

    private lateinit var recoveryDialog: SimProgressDialog
    private lateinit var errorDialog: AlertDialog
    private lateinit var successDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)
        LanguageHelper.setLanguage(this, preferencesManager.language)

        setContentView(R.layout.activity_about)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setSupportActionBar(toolbar_about)
        supportActionBar?.let {
            it.show()
            it.setDisplayHomeAsUpEnabled(true)
        }

        initUi()

        viewPresenter = AboutPresenter(this, component)
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

    override fun setUserCount(userCount: String) {
        tv_userDbCount.text = userCount
    }

    override fun setProjectCount(projectCount: String) {
        tv_globalDbCount.text = projectCount
    }

    override fun setModuleCount(moduleCount: String) {
        tv_moduleDbCount.text = moduleCount
    }

    override fun setRecoveryInProgress() {
        recoveryDialog.show()
        bt_recoverDb.isEnabled = false
    }

    override fun setSuccessRecovering() =
        runOnUiThreadIfStillRunning(
            then = {
                recoveryDialog.dismiss()
                successDialog.show()
                bt_recoverDb.isEnabled = true
            }, otherwise = { showToast(R.string.success_recovery_message) })

    override fun setRecoveringFailed() =
        runOnUiThreadIfStillRunning(
            then = {
                recoveryDialog.dismiss()
                errorDialog.show()
                bt_recoverDb.isEnabled = true
            }, otherwise = { showToast(R.string.error_recovery_message) })
}
