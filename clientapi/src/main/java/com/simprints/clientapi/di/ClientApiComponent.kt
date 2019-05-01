package com.simprints.clientapi.di

import com.simprints.core.di.FeatureScope
import com.simprints.id.di.AppComponent
import dagger.Component

@Component(modules =  [ClientApiCoreModule::class, ClientApiModule::class],
        dependencies = [AppComponent::class])
@FeatureScope
interface ClientApiComponent {
    @Component.Builder interface Builder {
        fun appComponent(component: AppComponent): Builder
        fun build(): ClientApiComponent
    }
}
