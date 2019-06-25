package com.simprints.fingerprint.di

import com.simprints.fingerprint.activities.orchestrator.OrchestratorViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {

    viewModel { OrchestratorViewModel() }
}
