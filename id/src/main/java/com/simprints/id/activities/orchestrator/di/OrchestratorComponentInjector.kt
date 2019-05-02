package com.simprints.id.activities.orchestrator.di

import com.simprints.id.Application
import com.simprints.id.activities.orchestrator.OrchestratorActivity
import com.simprints.id.activities.orchestrator.OrchestratorPresenter
import com.simprints.id.di.AppComponent
import kotlinx.android.synthetic.main.activity_front.view.*
import java.lang.IllegalArgumentException

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

        fun inject(orchestratorPresenter: OrchestratorPresenter) {
            component?.inject(orchestratorPresenter)
        }
    }
}

