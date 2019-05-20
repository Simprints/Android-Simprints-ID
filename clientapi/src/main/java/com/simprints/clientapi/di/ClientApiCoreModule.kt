package com.simprints.clientapi.di

import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManagerImpl
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import com.simprints.core.di.FeatureScope
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides

@Module
open class ClientApiCoreModule {

    @Provides
    @FeatureScope
    open fun provideClientApiSessionEventsManager(coreSessionManager: SessionEventsManager): ClientApiSessionEventsManager =
        ClientApiSessionEventsManagerImpl(coreSessionManager)

    @Provides
    @FeatureScope
    open fun provideClientApiCrashReportManager(coreCrashReportManager: CoreCrashReportManager): ClientApiCrashReportManager =
        ClientApiCrashReportManagerImpl(coreCrashReportManager)

    @Provides
    @FeatureScope
    open fun provideClientTimeHelper(coreTimeHelper: TimeHelper): ClientApiTimeHelper = ClientApiTimeHelperImpl(coreTimeHelper)
}
