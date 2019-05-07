package com.simprints.clientapi.activities.libsimprints.di

import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [LibSimprintsActivityModule::class])
interface LibSimprintsActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(orchestratorActivityModule: LibSimprintsActivityModule,
                   @BindsInstance activity: LibSimprintsActivity): LibSimprintsActivityComponent
    }

    fun inject(orchestratorActivity: LibSimprintsActivity)
    fun inject(orchestratorActivity: LibSimprintsPresenter)
}
