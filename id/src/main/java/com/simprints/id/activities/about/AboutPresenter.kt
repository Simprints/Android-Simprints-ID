package com.simprints.id.activities.about


import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class AboutPresenter(private val view: AboutContract.View,
                              private val dataManager: DataManager) : AboutContract.Presenter {

    override fun start() {
        initVersions()
        initCounts()
        view.setRecoveryAvailability(recoveryRunning)
    }

    private fun initVersions() {
        view.setVersionData(
            dataManager.appVersionName,
            dataManager.libVersionName,
            dataManager.hardwareVersionString)
    }

    private fun initCounts() {
        view.setDbCountData(
            dataManager.getPeopleCount(Constants.GROUP.USER).toLong().toString(),
            dataManager.getPeopleCount(Constants.GROUP.MODULE).toLong().toString(),
            dataManager.getPeopleCount(Constants.GROUP.GLOBAL).toLong().toString())
    }

    override fun recoverDb() {
        recoveryRunning = true
        view.setStartRecovering()
        dataManager.recoverRealmDb(Constants.GROUP.GLOBAL)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = { handleRecoverySuccess() },
                onError = { throwable -> handleRecoveryError(throwable) })
    }

    private fun handleRecoverySuccess() {
        recoveryRunning = false
        view.setSuccessRecovering()
    }

    private fun handleRecoveryError(throwable: Throwable) {
        recoveryRunning = false
        dataManager.logThrowable(throwable)
        view.setRecoveringFailed()
    }

    companion object {
        private var recoveryRunning = false
    }
}
