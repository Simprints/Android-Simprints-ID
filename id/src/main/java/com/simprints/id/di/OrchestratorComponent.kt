package com.simprints.id.di

import com.simprints.id.activities.orchestrator.OrchestratorActivity
import com.simprints.id.orchestrator.FlowProvider
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class OrchestratorScope

@Subcomponent(modules = [OrchestratorModule::class])
@OrchestratorScope
interface OrchestratorComponent {

    @Subcomponent.Builder
    interface Builder {
        fun orchestratorModule(orchestratorModule: OrchestratorModule): Builder
        fun build(): OrchestratorComponent
    }

    fun inject(orchestratorActivity: OrchestratorActivity)

    fun getFlowManager(): FlowProvider
}
