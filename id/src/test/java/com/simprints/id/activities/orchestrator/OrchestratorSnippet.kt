package com.simprints.id.activities.orchestrator

import com.simprints.id.activities.orchestrator.di.OrchestratorActivityComponent
import com.simprints.id.activities.orchestrator.di.OrchestratorComponentInjector
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.syntax.mock
import dagger.Component
import dagger.Module
import dagger.Provides

fun mockOrchestratorDI(){
    OrchestratorComponentInjector.component = DaggerTestOrchestratorActivityComponent.create()
}

@Component(modules = [TestOrchestratorActivityModule::class])
interface TestOrchestratorActivityComponent : OrchestratorActivityComponent

@Module
open class TestOrchestratorActivityModule {
    @Provides
    fun provideOrchestratorPresenter(): OrchestratorContract.Presenter = mock()
    @Provides
    fun provideTimeHelper(): TimeHelper = mock()
    @Provides
    fun provideOrchestratorView(): OrchestratorContract.View = mock()
    @Provides
    fun getOrchestratorManager(): OrchestratorManager = mock()
    @Provides
    fun getSessionEventsManager(): SessionEventsManager = mock()
    @Provides
    fun getSyncSchedulerHelper(): SyncSchedulerHelper = mock()
    @Provides
    fun provideAndroidResourcesHelper(): AndroidResourcesHelper = mock()

}
