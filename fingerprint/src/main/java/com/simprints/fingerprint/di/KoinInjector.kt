package com.simprints.fingerprint.di

import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
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
            viewModel { OrchestratorViewModel() }
        }
}
