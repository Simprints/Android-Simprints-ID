package com.simprints.clientapi.di

import com.simprints.clientapi.activities.errors.ErrorContract
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.activities.odk.OdkContract
import com.simprints.clientapi.activities.odk.OdkPresenter
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManagerImpl
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import com.simprints.clientapi.tools.json.GsonBuilder
import com.simprints.clientapi.tools.json.GsonBuilderImpl
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.tools.TimeHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


val koinModule = module {

    // Core Builders
    factory { SessionEventsManager.build(androidApplication() as Application) }
    factory { CoreCrashReportManager.build(androidApplication() as Application) }
    factory { TimeHelper.build(androidApplication() as Application) }

    // Domain
    factory<ClientApiSessionEventsManager> { ClientApiSessionEventsManagerImpl(get()) }
    factory<ClientApiCrashReportManager> { ClientApiCrashReportManagerImpl(get()) }
    factory<ClientApiTimeHelper> { ClientApiTimeHelperImpl(get()) }
    factory<GsonBuilder> { GsonBuilderImpl() }

    // Presenters
    factory<ErrorContract.Presenter> { (view: ErrorContract.View) ->
        ErrorPresenter(view, get(), get())
    }
    factory<LibSimprintsContract.Presenter> { (view: LibSimprintsContract.View, action: String?) ->
        LibSimprintsPresenter(view, action, get(), get(), get(), get())
    }
    factory<OdkContract.Presenter> { (view: OdkContract.View, action: String?) ->
        OdkPresenter(view, action, get(), get(), get(), get())
    }
}
