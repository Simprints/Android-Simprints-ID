package com.simprints.id.di

import android.content.Context
import com.simprints.id.activities.orchestrator.OrchestratorViewModelFactory
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.orchestrator.ModalityFlowFactory
import com.simprints.id.orchestrator.ModalityFlowFactoryImpl
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.OrchestratorManagerImpl
import com.simprints.id.orchestrator.builders.AppResponseFactory
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImpl
import com.simprints.id.orchestrator.modality.ModalityFlowIdentifyImpl
import com.simprints.id.orchestrator.modality.ModalityFlowVerifyImpl
import com.simprints.id.orchestrator.steps.face.*
import com.simprints.id.orchestrator.steps.fingerprint.*
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class AndroidModule {

    @Provides
    fun provideFaceRequestFactory(): FaceRequestFactory = FaceRequestFactoryImpl()

    @Provides
    fun provideFingerprintRequestFactory(): FingerprintRequestFactory = FingerprintRequestFactoryImpl()


    // Face Step Processors [Enrol, Identify, Verify]
    @Provides
    fun provideFaceEnrolStepProcessor(faceRequestFactory: FaceRequestFactory,
                                      ctx: Context): FaceEnrolStepProcessor =
        FaceEnrolStepProcessorImpl(faceRequestFactory, ctx.packageName)

    @Provides
    fun provideFaceVerifyStepProcessor(faceRequestFactory: FaceRequestFactory,
                                       ctx: Context): FaceVerifyStepProcessor =
        FaceVerifyStepProcessorImpl(faceRequestFactory, ctx.packageName)

    @Provides
    fun provideFaceIdentifyStepProcessor(faceRequestFactory: FaceRequestFactory,
                                         ctx: Context): FaceIdentifyStepProcessor =
        FaceIdentifyStepProcessorImpl(faceRequestFactory, ctx.packageName)

    // Fingerprint Step Processors [Enrol, Identify, Verify]
    @Provides
    fun provideFingerprintEnrolStepProcessor(fingerprintRequestFactory: FingerprintRequestFactory,
                                             preferenceManager: PreferencesManager,
                                             ctx: Context): FingerprintEnrolStepProcessor =
        FingerprintEnrolStepProcessorImpl(fingerprintRequestFactory, preferenceManager, ctx.packageName)

    @Provides
    fun provideFingerprintVerifyStepProcessor(fingerprintRequestFactory: FingerprintRequestFactory,
                                              preferenceManager: PreferencesManager,
                                              ctx: Context): FingerprintVerifyStepProcessor =
        FingerprintVerifyStepProcessorImpl(fingerprintRequestFactory, preferenceManager, ctx.packageName)

    @Provides
    fun provideFingerprintIdentifyStepProcessor(fingerprintRequestFactory: FingerprintRequestFactory,
                                                preferenceManager: PreferencesManager,
                                                ctx: Context): FingerprintIdentifyStepProcessor =
        FingerprintIdentifyStepProcessorImpl(fingerprintRequestFactory, preferenceManager, ctx.packageName)


    // ModalFlow [Enrol, Identify, Verify]
    @Provides
    @Named("ModalityFlowEnrol")
    fun provideModalityFlow(fingerprintEnrolStepProcessor: FingerprintEnrolStepProcessor,
                            faceEnrolStepProcessorImpl: FaceEnrolStepProcessor): ModalityFlow =
        ModalityFlowEnrolImpl(fingerprintEnrolStepProcessor, faceEnrolStepProcessorImpl)

    @Provides
    @Named("ModalityFlowVerify")
    fun provideModalityFlowVerify(fingerprintEnrolStepProcessor: FingerprintVerifyStepProcessor,
                                  faceEnrolStepProcessorImpl: FaceVerifyStepProcessor): ModalityFlow =
        ModalityFlowVerifyImpl(fingerprintEnrolStepProcessor, faceEnrolStepProcessorImpl)

    @Provides
    @Named("ModalityFlowIdentify")
    fun provideModalityFlowIdentify(fingerprintIdentifyStepProcessor: FingerprintIdentifyStepProcessor,
                                    faceIdentifyStepProcessorImpl: FaceIdentifyStepProcessor): ModalityFlow =
        ModalityFlowIdentifyImpl(fingerprintIdentifyStepProcessor, faceIdentifyStepProcessorImpl)

    // Orchestration
    @Provides
    fun provideModalityFlowFactory(@Named("ModalityFlowEnrol") enrolFlow: ModalityFlow,
                                        @Named("ModalityFlowVerify") verifyFlow: ModalityFlow,
                                        @Named("ModalityFlowIdentify") identifyFlow: ModalityFlow): ModalityFlowFactory =
        ModalityFlowFactoryImpl(enrolFlow, verifyFlow, identifyFlow)

    @Provides
    @Singleton
    fun provideOrchestratorManager(modalityFlowFactory: ModalityFlowFactory,
                                        appResponseFactory: AppResponseFactory): OrchestratorManager =
        OrchestratorManagerImpl(modalityFlowFactory, appResponseFactory)

    @Provides
    fun provideOrchestratorViewModelFactory(orchestratorManager: OrchestratorManager,
                                            preferenceManager: PreferencesManager,
                                            sessionEventsManager: SessionEventsManager,
                                            timeHelper: TimeHelper) =
        OrchestratorViewModelFactory(orchestratorManager, preferenceManager, sessionEventsManager, timeHelper)

}
