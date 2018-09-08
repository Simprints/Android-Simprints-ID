package com.simprints.id.services.scheduledSync.peopleSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.sync.SyncExecutor
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.json.JsonHelper
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

class ScheduledPeopleSync : Worker() {

    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager

    override fun doWork(): Result {
        val result = LinkedBlockingQueue<Result>()

        if (applicationContext is Application) {
            (applicationContext as Application).component.inject(this)

            Timber.d("ScheduledPeopleSync - doWork")

            SyncExecutor(dbManager, JsonHelper.gson)
                .sync(syncParams = SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager))
                .subscribeBy(onError = {
                    Timber.d("ScheduledPeopleSync - onError")
                    Timber.e(it)
                    result.put(Result.FAILURE)
                }, onComplete = {
                    Timber.d("ScheduledPeopleSync - onComplete")
                    result.put(Result.SUCCESS)
                }, onNext = {
                    Timber.d("ScheduledPeopleSync - onProgress: ${it.currentValue}/${it.maxValue}")
                })
        }
        return result.take()
    }
}
