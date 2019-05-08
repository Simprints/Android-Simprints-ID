package com.simprints.fingerprint.di

import com.simprints.id.Application
import com.simprints.id.di.AppComponent

class FingerprintComponentBuilder {

    companion object {

        private var component: FingerprintComponent? = null

        //StopShip
        @JvmStatic
        fun getComponent(app: Application): FingerprintComponent =
            component?.let {
                it
            } ?: buildComponent(app.component).also { component = it }

        private fun buildComponent(appComponent: AppComponent): FingerprintComponent =
            DaggerFingerprintComponent
                .builder()
                .appComponent(appComponent)
                .build()

        fun setComponent(testAppComponent: FingerprintComponent) {
            component = testAppComponent
        }

    }
}

