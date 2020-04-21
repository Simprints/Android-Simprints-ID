package com.simprints.face.di

import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.capture.livefeedback.LiveFeedbackFragmentViewModel
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelper
import com.simprints.face.controllers.core.androidResources.FaceAndroidResourcesHelperImpl
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.controllers.core.image.FaceImageManagerImpl
import com.simprints.face.controllers.core.preferencesManager.FacePreferencesManager
import com.simprints.face.controllers.core.preferencesManager.FacePreferencesManagerImpl
import com.simprints.face.detection.FaceDetector
import com.simprints.face.detection.mock.MockFaceDetector
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
        factory<FaceAndroidResourcesHelper> { FaceAndroidResourcesHelperImpl(get()) }
        factory<FacePreferencesManager> { FacePreferencesManagerImpl(get()) }
        factory<FaceImageManager> { FaceImageManagerImpl(get(), get()) }
    }

    private fun Module.defineBuildersForDomainClasses() {
        factory<FaceDetector> { MockFaceDetector() }
        factory { FrameProcessor(get()) }
        factory { LibYuvJni() }
    }

    private fun Module.defineBuildersForViewModels() {
        viewModel { FaceOrchestratorViewModel() }
        viewModel { FaceCaptureViewModel(get<FacePreferencesManager>().maxRetries, get()) }
        viewModel { (mainVM: FaceCaptureViewModel) -> LiveFeedbackFragmentViewModel(mainVM, get(), get(), get<FacePreferencesManager>().qualityThreshold) }
    }
}
