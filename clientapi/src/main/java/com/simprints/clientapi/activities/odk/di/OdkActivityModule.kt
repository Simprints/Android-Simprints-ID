package com.simprints.clientapi.activities.odk.di

import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.activities.odk.OdkContract
import com.simprints.clientapi.activities.odk.OdkPresenter
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.json.GsonBuilder
import dagger.Module
import dagger.Provides

@Module
open class OdkActivityModule {

    @Provides
    fun provideLibSimprintsActivity(activity: OdkActivity): OdkContract.View = activity

    @Provides
    fun provideLibSimprintsPresenter(view: OdkContract.View,
                                     clientApiSessionEventsManager: ClientApiSessionEventsManager,
                                     gsonBuilder: GsonBuilder): OdkContract.Presenter =
        OdkPresenter(view, clientApiSessionEventsManager, gsonBuilder, view.action)
}
