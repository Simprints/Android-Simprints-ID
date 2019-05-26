package com.simprints.clientapi.di

import com.simprints.clientapi.activities.errors.ErrorContract
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManagerImpl
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.tools.TimeHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


val koinModule = module {

    // Core Builders
    // TODO: I don't know what casting to id application will do
    factory { SessionEventsManager.build(androidApplication() as Application) }
    factory { CoreCrashReportManager.build(androidApplication() as Application) }
    factory { TimeHelper.build(androidApplication() as Application) }

    // Domain
    factory<ClientApiSessionEventsManager> { ClientApiSessionEventsManagerImpl(get()) }
    factory<ClientApiCrashReportManager> { ClientApiCrashReportManagerImpl(get()) }
    factory<ClientApiTimeHelper> { ClientApiTimeHelperImpl(get()) }

    // Presenters
    factory<ErrorContract.Presenter> { (view: ErrorContract.View) -> ErrorPresenter(view, get(), get()) }

}
