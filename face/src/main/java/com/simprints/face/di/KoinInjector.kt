package com.simprints.face.di

import com.simprints.face.activities.orchestrator.FaceOrchestratorViewModel
import com.simprints.id.Application
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

object KoinInjector {
    private var koinModule: Module? = null

    private fun Scope.appComponent() =
        (androidApplication() as Application).component

    fun acquireFaceKoinModules() {
        if (koinModule == null) {
            val module = buildKoinModule()
            loadKoinModules(module)
            koinModule = module
        }
    }

    fun releaseFaceKoinModules() {
        koinModule?.let {
            unloadKoinModules(it)
            koinModule = null
        }
    }

    private fun buildKoinModule() =
        module(override = true) {
            defineBuildersForFaceManagers()
            defineBuildersForDomainClasses()
            defineBuildersForViewModels()
        }

    private fun Module.defineBuildersForFaceManagers() {

    }

    private fun Module.defineBuildersForDomainClasses() {

    }

    private fun Module.defineBuildersForViewModels() {
        viewModel { FaceOrchestratorViewModel() }
    }
}
