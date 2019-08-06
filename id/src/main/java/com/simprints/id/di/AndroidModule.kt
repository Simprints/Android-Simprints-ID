package com.simprints.id.di

import com.simprints.id.activities.orchestrator.OrchestratorViewModelFactory
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides

@Module
class AndroidModule {

    @Provides
    fun provideOrchestratorViewModelFactory(orchestratorManager: OrchestratorManager,
                                            sessionEventsManager: SessionEventsManager,
                                            timeHelper: TimeHelper) =
        OrchestratorViewModelFactory(orchestratorManager, sessionEventsManager, timeHelper)
}
