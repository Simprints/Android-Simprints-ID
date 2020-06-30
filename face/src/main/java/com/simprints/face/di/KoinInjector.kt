package com.simprints.face.di

import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.capture.livefeedback.LiveFeedbackFragmentViewModel
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManagerImpl
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.FaceSessionEventsManagerImpl
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.flow.MasterFlowManagerImpl
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.controllers.core.image.FaceImageManagerImpl
import com.simprints.face.controllers.core.preferencesManager.FacePreferencesManager
import com.simprints.face.controllers.core.preferencesManager.FacePreferencesManagerImpl
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.controllers.core.repository.FaceDbManagerImpl
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.controllers.core.timehelper.FaceTimeHelperImpl
import com.simprints.face.detection.FaceDetector
import com.simprints.face.detection.rankone.RankOneFaceDetector
import com.simprints.face.exitform.ExitFormViewModel
import com.simprints.face.match.FaceMatchViewModel
import com.simprints.face.match.FaceMatcher
import com.simprints.face.match.rankone.RankOneFaceMatcher
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import com.simprints.id.Application
import com.simprints.uicomponents.imageTools.LibYuvJni
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
        factory<FacePreferencesManager> { FacePreferencesManagerImpl(get()) }
        factory<FaceImageManager> { FaceImageManagerImpl(get(), get()) }
        factory<MasterFlowManager> { MasterFlowManagerImpl(get()) }
        factory<FaceDbManager> { FaceDbManagerImpl(get()) }
        factory<FaceCrashReportManager> { FaceCrashReportManagerImpl(get()) }
        factory<FaceTimeHelper> { FaceTimeHelperImpl(get()) }
        factory<FaceSessionEventsManager> { FaceSessionEventsManagerImpl(get()) }
    }

    private fun Module.defineBuildersForDomainClasses() {
        factory<FaceDetector> { RankOneFaceDetector() }
        factory { FrameProcessor(get()) }
        factory { LibYuvJni() }
        factory<FaceMatcher> { RankOneFaceMatcher() }
    }

    private fun Module.defineBuildersForViewModels() {
        viewModel { FaceOrchestratorViewModel(get()) }
        viewModel { FaceCaptureViewModel(get<FacePreferencesManager>().maxRetries, get(), get()) }
        viewModel {
            FaceMatchViewModel(
                get(),
                get(),
                get(),
                get<FacePreferencesManager>().faceMatchThreshold,
                get(),
                get(),
                get()
            )
        }

        viewModel { (mainVM: FaceCaptureViewModel) ->
            LiveFeedbackFragmentViewModel(
                mainVM,
                get(),
                get(),
                get<FacePreferencesManager>().qualityThreshold,
                get(),
                get()
            )
        }
        viewModel { (mainVM: FaceCaptureViewModel) -> ExitFormViewModel(mainVM, get()) }
    }
}
