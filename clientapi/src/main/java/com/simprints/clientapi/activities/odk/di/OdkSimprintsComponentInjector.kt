package com.simprints.clientapi.activities.odk.di

import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.activities.odk.OdkPresenter
import com.simprints.clientapi.di.ClientApiComponentBuilder
import com.simprints.id.Application

class OdkComponentInjector {

    companion object {

        private var component: OdkActivityComponent? = null

        @JvmStatic
        private fun getComponent(app: Application, activity: OdkActivity): OdkActivityComponent =
            component?.let {
                it
            } ?: buildComponent(app, activity).also { component = it }

        private fun buildComponent(app: Application, activity: OdkActivity): OdkActivityComponent =
            ClientApiComponentBuilder.getComponent(app)
                .odkActivityComponentFactory.create(OdkActivityModule(), activity)

        fun setComponent(component: OdkActivityComponent?) {
            this.component = component
        }

        fun inject(activity: OdkActivity) {
            val app = activity.application as Application
            getComponent(app, activity).inject(activity)
        }

        fun inject(OdkPresenter: OdkPresenter) {
            component?.inject(OdkPresenter)
        }
    }
}
