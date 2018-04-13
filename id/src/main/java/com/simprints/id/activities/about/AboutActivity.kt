package com.simprints.id.activities.about

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.SimProgressDialog
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.id.tools.extensions.showToast
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import io.reactivex.rxkotlin.subscribeBy
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

        //STOPSHIP: Delete bt_deleteSyncInfo, bt_deletePeopleFromRealm, bt_addPatient, bt_enrollPeople before release
        bt_deleteSyncInfo.setOnClickListener { dataManager.deleteSyncInfoFromLocal(SyncTaskParameters.build(app.dataManager.syncGroup, app.dataManager)) }
        bt_deletePeopleFromRealm.setOnClickListener { dataManager.deletePeopleFromLocal(SyncTaskParameters.build(app.dataManager.syncGroup, app.dataManager)) }
        bt_addPatient.setOnClickListener {
            (1..10).forEach {
                dataManager.insertOrUpdatePersonInLocal(
                    PeopleGeneratorUtils.getRandomPeople(1,
                        projectId = dataManager.getSignedInProjectIdOrEmpty(),
                        userId = dataManager.getSignedInUserIdOrEmpty(),
                        toSync = true).first()
                ).subscribeBy ( onComplete = {}, onError = { it.printStackTrace() })
            }
        }

        bt_enrollPeople.setOnClickListener {
            dataManager.savePerson(
                fb_Person(PeopleGeneratorUtils.getRandomPeople(1,
                    projectId = dataManager.getSignedInProjectIdOrEmpty(),
                    userId = dataManager.getSignedInUserIdOrEmpty()).first())
            ).subscribeBy(
                onComplete = {},
                onError = { it.printStackTrace() }
            )
        }
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
