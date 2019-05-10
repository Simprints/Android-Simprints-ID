package com.simprints.clientapi.activities.errors.di

import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.di.ClientApiComponentBuilder
import com.simprints.id.Application

class ErrorActivityComponentInjector {

    companion object {

        private var component: ErrorActivityComponent? = null

        @JvmStatic
        private fun getComponent(app: Application, activity: ErrorActivity): ErrorActivityComponent =
            component?.let {
                it
            } ?: buildComponent(app, activity).also { component = it }

        private fun buildComponent(app: Application, activity: ErrorActivity): ErrorActivityComponent =
            ClientApiComponentBuilder.getComponent(app)
                .errorActivityComponentFactory.create(ErrorActivityModule(), activity)

        fun setComponent(component: ErrorActivityComponent?) {
            this.component = component
        }

        fun inject(activity: ErrorActivity) {
            val app = activity.application as Application
            getComponent(app, activity).inject(activity)
        }

        fun inject(errorPresenter: ErrorPresenter) {
            component?.inject(errorPresenter)
        }
    }
}
