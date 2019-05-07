package com.simprints.clientapi.activities.odk.di

import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.activities.odk.OdkPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [OdkActivityModule::class])
interface OdkActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(orchestratorActivityModule: OdkActivityModule,
                   @BindsInstance activity: OdkActivity): OdkActivityComponent
    }

    fun inject(orchestratorActivity: OdkActivity)
    fun inject(orchestratorActivity: OdkPresenter)
}
