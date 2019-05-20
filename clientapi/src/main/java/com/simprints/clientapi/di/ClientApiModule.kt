package com.simprints.clientapi.di

import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import com.simprints.clientapi.tools.json.GsonBuilder
import com.simprints.clientapi.tools.json.GsonBuilderImpl
import com.simprints.core.di.FeatureScope
import dagger.Module
import dagger.Provides

@Module
open class ClientApiModule {

    @Provides
    @FeatureScope
    open fun provideGsonBuilder(): GsonBuilder = GsonBuilderImpl()
}
