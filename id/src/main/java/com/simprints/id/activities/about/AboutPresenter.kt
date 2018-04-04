package com.simprints.id.activities.about


import android.view.WindowManager
import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.safe.SimprintsException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class AboutPresenter(private val aboutView: AboutContract.View,
                              private val dataManager: DataManager) : AboutContract.Presenter {

    override fun start() {
        initVersions()
        initCounts()
        initRecoveryAvailability()
    }

    private fun initVersions() {
        aboutView.setVersionData(
            dataManager.appVersionName,
            dataManager.libVersionName,
            dataManager.hardwareVersionString)
    }

    private fun initCounts() {
        aboutView.setDbCountData(
            dataManager.getPeopleCount(Constants.GROUP.USER).toLong().toString(),
            dataManager.getPeopleCount(Constants.GROUP.MODULE).toLong().toString(),
            dataManager.getPeopleCount(Constants.GROUP.GLOBAL).toLong().toString())
    }

    private fun initRecoveryAvailability() =
        if (recoveryRunning) {
            aboutView.setRecoverDbUnavailable()
        } else {
            aboutView.setRecoverDbAvailable()
        }

    override fun recoverDb() {
        recoveryRunning = true
        dataManager.recoverRealmDb(Constants.GROUP.GLOBAL)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = { handleRecoverySuccess() },
                onError = { throwable -> handleRecoveryError(throwable) })
    }

    private fun handleRecoverySuccess() {
        recoveryRunning = false
        try {
            aboutView.setSuccessRecovering()
            aboutView.setRecoverDbAvailable()
        } catch (e: WindowManager.BadTokenException) {
            dataManager.logSafeException(SimprintsException(e))
            e.printStackTrace()
        }
    }

    private fun handleRecoveryError(throwable: Throwable) {
        recoveryRunning = false
        dataManager.logThrowable(throwable)
        try {
            aboutView.setRecoveringFailed(throwable.message)
            aboutView.setRecoverDbAvailable()
        } catch (e: WindowManager.BadTokenException) {
            dataManager.logSafeException(SimprintsException(e))
            e.printStackTrace()
        }
    }

    companion object {
        private var recoveryRunning = false
    }
}
