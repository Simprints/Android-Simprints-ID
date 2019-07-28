package com.simprints.fingerprint.di

import com.simprints.fingerprint.activities.matching.MatchingViewModel
import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.orchestrator.Orchestrator
import com.simprints.fingerprint.tasks.RunnableTaskDispatcher
import com.simprints.id.Application
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

object KoinInjector {

    private var koinModule: Module? = null

    fun loadFingerprintKoinModules() {
        if (koinModule == null) {
            val module = buildKoinModule()
            loadKoinModules(module)
            koinModule = module
        }
    }

    fun unloadFingerprintKoinModules() {
        koinModule?.let {
            unloadKoinModules(it)
            koinModule = null
        }
    }

    private fun buildKoinModule() =
        module(override = true) {
            defineBuildersForDomainObjects()
            defineBuildersForViewModels()
        }


    private fun Module.defineBuildersForDomainObjects() {
        factory { FinalResultBuilder() }
        factory { RunnableTaskDispatcher.build(androidApplication() as Application) }
        factory { Orchestrator(get()) }
    }

    private fun Module.defineBuildersForViewModels() {
        viewModel { OrchestratorViewModel(get(), get()) }
        viewModel { MatchingViewModel.build(androidApplication() as Application) }
    }
}
