package com.simprints.id.activities.about


import com.simprints.id.data.DataManager
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.safe.SimprintsException
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
        dataManager.db.getPeopleCount(Constants.GROUP.GLOBAL).subscribe({ count ->
            view.setProjectCount(count.toString())
        }, { dataManager.analytics.logSafeException(SimprintsException(it)) })

        dataManager.db.getPeopleCount(Constants.GROUP.MODULE).subscribe({ count ->
            view.setModuleCount(count.toString())
        }, { dataManager.analytics.logSafeException(SimprintsException(it)) })

        dataManager.db.getPeopleCount(Constants.GROUP.USER).subscribe({ count ->
            view.setUserCount(count.toString())
        }, { dataManager.analytics.logSafeException(SimprintsException(it)) })
    }

    override fun recoverDb() {
        recoveryRunning = true
        view.setRecoveryInProgress()
        dataManager.db.recoverLocalDb(Constants.GROUP.GLOBAL)
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
        dataManager.analytics.logThrowable(throwable)
        view.setRecoveringFailed()
    }

    companion object {
        private var recoveryRunning = false
    }
}
