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
        if (recoveryRunning) view.setRecoveryInProgress()
    }

    private fun initVersions() {
        view.setVersionData(
            dataManager.appVersionName,
            dataManager.libVersionName,
            dataManager.hardwareVersionString)
    }

    private fun initCounts() {
        dataManager.getPeopleCount(Constants.GROUP.GLOBAL).subscribe({
            view.setProjectCount(it.toString())
        }, {
            view.setProjectCount(0.toString())
        })

        dataManager.getPeopleCount(Constants.GROUP.MODULE).subscribe({
            view.setModuleCount(it.toString())
        }, {
            view.setModuleCount(0.toString())
        })

        dataManager.getPeopleCount(Constants.GROUP.USER).subscribe({
            view.setUserCount(it.toString())
        }, {
            view.setUserCount(0.toString())
        })
    }

    override fun recoverDb() {
        recoveryRunning = true
        view.setRecoveryInProgress()
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
