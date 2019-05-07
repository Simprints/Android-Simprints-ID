package com.simprints.clientapi.di

import com.simprints.id.Application
import com.simprints.id.di.AppComponent

class ClientApiComponentBuilder {

    companion object {
        private var component: ClientApiComponent? = null

        @JvmStatic
        fun getComponent(app: Application): ClientApiComponent =
            component?.let {
                it
            } ?: buildComponent(app.component).also { component = it }

        private fun buildComponent(appComponent: AppComponent): ClientApiComponent =
            DaggerClientApiComponent
                .builder()
                .appComponent(appComponent)
                .build()

        fun setComponent(testAppComponent: ClientApiComponent) {
            component = testAppComponent
        }
    }
}
