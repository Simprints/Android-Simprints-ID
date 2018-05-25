package com.simprints.id.activities.about


import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.safe.SimprintsException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class AboutPresenter(private val view: AboutContract.View,
                              private val dbManager: DbManager,
                              private val loginInfoManager: LoginInfoManager,
                              private val preferencesManager: PreferencesManager,
                              private val analyticsManager: AnalyticsManager) : AboutContract.Presenter {

    override fun start() {
        initVersions()
        initCounts()
        if (recoveryRunning) view.setRecoveryInProgress()
    }

    private fun initVersions() {
        view.setVersionData(
            preferencesManager.appVersionName,
            preferencesManager.libVersionName,
            preferencesManager.hardwareVersionString)
    }

    private fun initCounts() {
        dbManager.getPeopleCount().subscribe({ count ->
            view.setProjectCount(count.toString())
        }, { analyticsManager.logSafeException(SimprintsException(it)) })
        dbManager.getPeopleCount(moduleId = preferencesManager.moduleId).subscribe({ count ->
            view.setModuleCount(count.toString())
        }, { analyticsManager.logSafeException(SimprintsException(it)) })

        dbManager.getPeopleCount(userId = loginInfoManager.getSignedInUserIdOrEmpty()).subscribe({ count ->
            view.setUserCount(count.toString())
        }, { analyticsManager.logSafeException(SimprintsException(it)) })
    }

    override fun recoverDb() {
        recoveryRunning = true
        view.setRecoveryInProgress()
        dbManager.recoverLocalDb(
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.deviceId,
            preferencesManager.moduleId,
            Constants.GROUP.GLOBAL)
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
        analyticsManager.logThrowable(throwable)
        view.setRecoveringFailed()
    }

    companion object {
        private var recoveryRunning = false
    }
}
