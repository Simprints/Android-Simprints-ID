package com.simprints.fingerprints.di

import com.simprints.id.Application
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule

class FingerprintsComponentBuilder {

    companion object {
        @JvmStatic fun getComponent(app: Application): FingerprintsComponent =
            DaggerFingerprintsComponent
                .builder()
                .appModule(AppModule(app))
                .preferencesModule(PreferencesModule())
                .serializerModule(SerializerModule())
                .build()
    }
}
