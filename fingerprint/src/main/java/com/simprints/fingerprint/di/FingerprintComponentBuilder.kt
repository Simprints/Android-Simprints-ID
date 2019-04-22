package com.simprints.fingerprint.di

import com.simprints.id.Application

class FingerprintComponentBuilder {

    companion object {

        private var component: FingerprintComponent? = null

        //StopShip
        @JvmStatic
        fun getComponent(app: Application): FingerprintComponent =
            component?.let {
                it
            } ?: buildComponent(app).also { component = it }

        private fun buildComponent(app: Application): FingerprintComponent =
            DaggerFingerprintComponent
                .builder()
                .appComponent(app.component)
                .build()

    }
}
