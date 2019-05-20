package com.simprints.clientapi.activities.errors.di

import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.ErrorPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [ErrorActivityModule::class])
interface ErrorActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(errorActivityModule: ErrorActivityModule,
                   @BindsInstance activity: ErrorActivity): ErrorActivityComponent
    }

    fun inject(orchestratorActivity: ErrorActivity)
    fun inject(orchestratorActivity: ErrorPresenter)
}
