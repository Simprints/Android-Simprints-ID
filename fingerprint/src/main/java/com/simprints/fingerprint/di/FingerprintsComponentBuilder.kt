package com.simprints.fingerprint.di

import com.simprints.id.Application
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule

class FingerprintsComponentBuilder {

    companion object {

        private var component: FingerprintsComponent? = null

        //StopShip
        @JvmStatic
        fun getComponent(app: Application): FingerprintsComponent =
            component?.let {
                it
            } ?: buildComponent(app).also { component = it }

        private fun buildComponent(app: Application): FingerprintsComponent =
            DaggerFingerprintsComponent
                .builder()
                .appModule(AppModule(app))
                .fingerprintModule(FingerprintModule())
                .preferencesModule(PreferencesModule())
                .serializerModule(SerializerModule())
                .build()

    }
}
