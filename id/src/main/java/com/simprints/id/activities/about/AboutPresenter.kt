package com.simprints.id.activities.about

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.safe.SimprintsException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AboutPresenter(private val view: AboutContract.View,
                     component: AppComponent) : AboutContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var analyticsManager: AnalyticsManager

    init {
        component.inject(this)
    }

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
        dbManager.getPeopleCount(Constants.GROUP.GLOBAL).subscribe({ count ->
            view.setProjectCount(count.toString())
        }, { analyticsManager.logSafeException(SimprintsException(it)) })

        dbManager.getPeopleCount(Constants.GROUP.MODULE).subscribe({ count ->
            view.setModuleCount(count.toString())
        }, { analyticsManager.logSafeException(SimprintsException(it)) })

        dbManager.getPeopleCount(Constants.GROUP.USER).subscribe({ count ->
            view.setUserCount(count.toString())
        }, { analyticsManager.logSafeException(SimprintsException(it)) })
    }

    override fun recoverDb() {
        recoveryRunning = true
        view.setRecoveryInProgress()
        dbManager.recoverLocalDb(Constants.GROUP.GLOBAL)
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
