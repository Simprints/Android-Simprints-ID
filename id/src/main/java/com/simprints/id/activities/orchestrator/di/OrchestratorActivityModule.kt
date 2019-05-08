package com.simprints.id.activities.orchestrator.di

import com.simprints.core.di.FeatureScope
import com.simprints.id.activities.orchestrator.OrchestratorActivity
import com.simprints.id.activities.orchestrator.OrchestratorContract
import com.simprints.id.activities.orchestrator.OrchestratorPresenter
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
open class OrchestratorActivityModule {

    @Provides
    fun provideOrchestratorView(activity: OrchestratorActivity): OrchestratorContract.View = activity

    @Provides
    fun provideOrchestratorPresenter(): OrchestratorContract.Presenter = OrchestratorPresenter()
}
