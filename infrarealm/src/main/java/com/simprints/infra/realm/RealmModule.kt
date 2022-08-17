package com.simprints.infra.realm

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class RealmModule {

    @Binds
    internal abstract fun bindRealmWrapper(impl: RealmWrapperImpl): RealmWrapper
}
