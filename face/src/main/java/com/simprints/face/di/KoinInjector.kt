package com.simprints.face.di

import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.capture.livefeedback.LiveFeedbackFragmentViewModel
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.configuration.ConfigurationViewModel
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
import com.simprints.face.license.data.local.LicenseLocalDataSource
import com.simprints.face.license.data.local.LicenseLocalDataSourceImpl
import com.simprints.face.license.data.remote.BasicAuthInterceptor
import com.simprints.face.license.data.remote.LicenseRemoteDataSource
import com.simprints.face.license.data.remote.LicenseRemoteDataSourceImpl
import com.simprints.face.license.data.remote.SimprintsLicenseServer
import com.simprints.face.license.data.repository.LicenseRepository
import com.simprints.face.license.data.repository.LicenseRepositoryImpl
import com.simprints.face.match.FaceMatchViewModel
import com.simprints.face.match.FaceMatcher
import com.simprints.face.match.rankone.RankOneFaceMatcher
import com.simprints.face.orchestrator.FaceOrchestratorViewModel
import com.simprints.id.Application
import com.simprints.id.network.TimberLogger
import com.simprints.uicomponents.imageTools.LibYuvJni
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Consider this flow:
 * - consumers starts at 0
 * - Setup callout: OrchestratorActivity.onCreate() , Koin modules created, consumers + 1 → 1 (The Koin modules are now loaded)
 * - Then, Normal callout (e.g. match) begins before previous on destroy: OrchestratorActivity.onCreate(), consumers + 1 → 2
 * - Setup callout now destroys shortly after like you described: OrchestratorActivity.onDestroy(), consumers - 1 → 1 (Koin modules still loaded)
 * - Normal callout finishes as usual after the work is done: OrchestratorActivity.onDestroy(), consumers - 1 → 0 (the Koin modules are now unloaded)
 *
 * This means we don’t need to worry about this mis-ordering during the flow - the unload only happens when the counter reaches 0.
 * If you call `acquire…` and `release…` every activity, this is additionally helpful for situations where the Orchestrator
 * activity is destroyed while it’s in the background because of low memory.
 */
object KoinInjector {
    private val consumers = AtomicInteger(0)
    private var koinModule: Module? = null

    private fun Scope.appComponent() =
        (androidApplication() as Application).component

    /**
     * Call this on the first point of contact of your modality (usually onCreate() of OrchestratorActivity)
     */
    fun acquireFaceKoinModules() {
        consumers.incrementAndGet()
        if (koinModule == null) {
            val module = buildKoinModule()
            loadKoinModules(module)
            koinModule = module
        }
    }

    /**
     * Call this on the last point of contact of your modality, usually onDestroy()
     */
    fun releaseFaceKoinModules() {
        if (consumers.decrementAndGet() == 0) {
            koinModule?.let {
                unloadKoinModules(it)
                koinModule = null
            }
        }
    }

    private fun buildKoinModule() =
        module(override = true) {
            defineBuildersForFaceManagers()
            defineBuildersForDomainClasses()
            defineBuildersForViewModels()
            defineBuildersForRemote()
        }

    private fun Module.defineBuildersForFaceManagers() {
        factory<FacePreferencesManager> { FacePreferencesManagerImpl(get()) }
        factory<FaceImageManager> { FaceImageManagerImpl(get(), get()) }
        factory<MasterFlowManager> { MasterFlowManagerImpl(get()) }
        factory<FaceDbManager> { FaceDbManagerImpl(get()) }
        factory<FaceCrashReportManager> { FaceCrashReportManagerImpl(get()) }
        factory<FaceTimeHelper> { FaceTimeHelperImpl(get()) }
        factory<FaceSessionEventsManager> { FaceSessionEventsManagerImpl(get()) }
        factory<DispatcherProvider> { DefaultDispatcherProvider() }
    }

    private fun Module.defineBuildersForDomainClasses() {
        factory<FaceDetector> { RankOneFaceDetector() }
        factory { FrameProcessor(get()) }
        factory { LibYuvJni() }
        factory<FaceMatcher> { RankOneFaceMatcher() }
        factory<LicenseLocalDataSource> { LicenseLocalDataSourceImpl(androidContext()) }
        factory<LicenseRemoteDataSource> { LicenseRemoteDataSourceImpl(get()) }
        factory<LicenseRepository> { LicenseRepositoryImpl(get(), get(), get()) }
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

        viewModel { ConfigurationViewModel(get()) }
    }

    private fun Module.defineBuildersForRemote() {
        factory {
            val loggingInterceptor = HttpLoggingInterceptor(TimberLogger()).apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            OkHttpClient.Builder()
                .addInterceptor(BasicAuthInterceptor("VYHffwmvMxiaoxzm", "3fM01e10sn5Vq6FV2EVd"))
                .addInterceptor(loggingInterceptor)
                .build()
        }

        factory {
            Retrofit.Builder()
                .baseUrl(SimprintsLicenseServer.BASE_URL)
                .client(get())
                .build()
                .create(SimprintsLicenseServer::class.java)
        }
    }
}
