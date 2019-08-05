package com.simprints.id.activities.orchestrator.di

import com.simprints.id.Application
import com.simprints.id.activities.orchestrator.OrchestratorActivity

class OrchestratorComponentInjector {

    companion object {

        internal var component: OrchestratorActivityComponent? = null

        @JvmStatic
        fun getComponent(activity: OrchestratorActivity): OrchestratorActivityComponent =
            component?.let {
                it
            } ?: buildComponent(activity).also { component = it }

        private fun buildComponent(activity: OrchestratorActivity): OrchestratorActivityComponent =
            (activity.application as Application).component
                .orchestratorActivityComponentFactory.create(OrchestratorActivityModule(), activity)

        fun inject(activity: OrchestratorActivity) {
            val orchestratorActivityComponent = getComponent(activity)
            orchestratorActivityComponent.inject(activity)
        }
    }
}

