package com.simprints.id.activities.orchestrator.di

import com.simprints.id.activities.orchestrator.OrchestratorActivity
import com.simprints.id.activities.orchestrator.OrchestratorViewModelFactory
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [OrchestratorActivityModule::class])
interface OrchestratorActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(orchestratorActivityModule: OrchestratorActivityModule,
                   @BindsInstance activity: OrchestratorActivity): OrchestratorActivityComponent
    }

    fun inject(orchestratorActivity: OrchestratorActivity)
    fun inject(orchestratorViewModelFactory: OrchestratorViewModelFactory)
}
