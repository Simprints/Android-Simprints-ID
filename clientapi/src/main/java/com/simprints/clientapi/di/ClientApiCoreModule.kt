package com.simprints.clientapi.di

import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.core.di.FeatureScope
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import dagger.Module
import dagger.Provides

@Module
open class ClientApiCoreModule {

    @Provides
    @FeatureScope
    open fun provideClientApiSessionEventsManager(coreSessionManager: SessionEventsManager): ClientApiSessionEventsManager =
        ClientApiSessionEventsManagerImpl(coreSessionManager)
}
