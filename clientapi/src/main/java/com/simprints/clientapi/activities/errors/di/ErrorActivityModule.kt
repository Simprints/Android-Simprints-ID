package com.simprints.clientapi.activities.errors.di

import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.ErrorContract
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import dagger.Module
import dagger.Provides

@Module
open class ErrorActivityModule {

    @Provides
    fun provideErrorActivity(activity: ErrorActivity): ErrorContract.View = activity

    @Provides
    fun providesErrorPresenter(view: ErrorContract.View,
                               clientApiSessionEventsManager: ClientApiSessionEventsManager,
                               clientApiCrashReportManager: ClientApiCrashReportManager): ErrorContract.Presenter =
        ErrorPresenter(view, clientApiSessionEventsManager, clientApiCrashReportManager)
}
