package com.simprints.clientapi.activities.libsimprints.di

import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.di.ClientApiComponentBuilder
import com.simprints.id.Application

class LibSimprintsComponentInjector {

    companion object {

        private var component: LibSimprintsActivityComponent? = null

        @JvmStatic
        private fun getComponent(app: Application, activity: LibSimprintsActivity): LibSimprintsActivityComponent =
            component?.let {
                it
            } ?: buildComponent(app, activity).also { component = it }

        private fun buildComponent(app:Application, activity: LibSimprintsActivity): LibSimprintsActivityComponent =
            ClientApiComponentBuilder.getComponent(app)
                .libsimprintsActivityComponentFactory.create(LibSimprintsActivityModule(), activity)

        fun setComponent(component: LibSimprintsActivityComponent?) {
            this.component = component
        }

        fun inject(activity: LibSimprintsActivity) {
            val app = activity.application as Application
            getComponent(app, activity).inject(activity)
        }

        fun inject(LibSimprintsPresenter: LibSimprintsPresenter) {
            component?.inject(LibSimprintsPresenter)
        }
    }
}
