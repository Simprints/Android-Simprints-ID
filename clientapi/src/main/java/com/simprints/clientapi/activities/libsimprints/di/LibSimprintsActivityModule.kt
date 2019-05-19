package com.simprints.clientapi.activities.libsimprints.di

import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.json.GsonBuilder
import dagger.Module
import dagger.Provides

@Module
open class LibSimprintsActivityModule {

    @Provides
    fun provideLibSimprintsActivity(activity: LibSimprintsActivity): LibSimprintsContract.View = activity

    @Provides
    fun provideLibSimprintsPresenter(view: LibSimprintsContract.View,
                                     timeHelper: ClientApiTimeHelper,
                                     clientApiSessionEventsManager: ClientApiSessionEventsManager,
                                     clientApiCrashReportManager: ClientApiCrashReportManager,
                                     gsonBuilder: GsonBuilder): LibSimprintsContract.Presenter =
        LibSimprintsPresenter(
            view,
            view.action,
            clientApiSessionEventsManager,
            clientApiCrashReportManager,
            timeHelper,
            gsonBuilder,
            view.integrationInfo)
}
