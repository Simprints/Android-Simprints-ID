package com.simprints.id.di

import android.content.SharedPreferences
import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.domain.workflow.WorkflowCacheClearer
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentContract
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentPresenter
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelper
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelperImpl
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.exitformhandler.ExitFormHelperImpl
import com.simprints.id.orchestrator.*
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.StepEncoder
import com.simprints.id.orchestrator.cache.StepEncoderImpl
import com.simprints.id.orchestrator.modality.*
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactoryImpl
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolResponseAdjudicationHelper
import com.simprints.id.orchestrator.responsebuilders.adjudication.EnrolResponseAdjudicationHelperImpl
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.SyncSchedulerImpl
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.SecurityManager.Companion.GLOBAL_SHARED_PREFS_FILENAME
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ModalityFlowConfirmation

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ModalityFlowEnrolmentLastBiometrics

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ModalityFlowEnrolment

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ModalityFlowIdentification

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ModalityFlowVerification

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EncryptedSharedPreferences

@Module
@InstallIn(SingletonComponent::class)
abstract class IdAppModule {

    @AssistedFactory
    interface CheckLoginFromIntentPresenterFactory {
        fun create(
            view: CheckLoginFromIntentContract.View,
        ): CheckLoginFromIntentPresenter
    }

    @Binds
    abstract fun providePersonCreationEventHelper(impl: PersonCreationEventHelperImpl): PersonCreationEventHelper

    @Binds
    abstract fun provideEnrolmentHelper(impl: EnrolmentHelperImpl): EnrolmentHelper

    @Binds
    abstract fun provideStepEncoder(impl: StepEncoderImpl): StepEncoder

    @Binds
    abstract fun provideHotCache(impl: HotCacheImpl): HotCache

    @Binds
    abstract fun provideWorkflowCacheClearer(impl: HotCacheImpl): WorkflowCacheClearer

    @Binds
    abstract fun provideExitFormHelper(impl: ExitFormHelperImpl): ExitFormHelper

    @Binds
    abstract fun provideEnrolResponseAdjudicationHelper(impl: EnrolResponseAdjudicationHelperImpl): EnrolResponseAdjudicationHelper
}

@Module
@InstallIn(SingletonComponent::class)
abstract class IdOrchestratorModule {

    @Binds
    abstract fun provideFaceRequestFactory(impl: FaceRequestFactoryImpl): FaceRequestFactory

    @Binds
    abstract fun provideFaceStepProcessor(impl: FaceStepProcessorImpl): FaceStepProcessor

    @Binds
    abstract fun provideFingerprintRequestFactory(impl: FingerprintRequestFactoryImpl): FingerprintRequestFactory

    @Binds
    abstract fun provideFingerprintStepProcessor(impl: FingerprintStepProcessorImpl): FingerprintStepProcessor

    @Binds
    abstract fun provideCoreStepProcessor(impl: CoreStepProcessorImpl): CoreStepProcessor

    @Binds
    abstract fun provideModalityFlowFactory(impl: ModalityFlowFactoryImpl): ModalityFlowFactory

    @Binds
    abstract fun provideOrchestratorManager(impl: OrchestratorManagerImpl): OrchestratorManager

    @Binds
    abstract fun provideFlowProvider(impl: OrchestratorManagerImpl): FlowProvider

    @Binds
    abstract fun provideAppResponseFactory(impl: AppResponseFactoryImpl): AppResponseFactory

    @Binds
    abstract fun provideOrchestratorEventsHelper(impl: OrchestratorEventsHelperImpl): OrchestratorEventsHelper

    @ModalityFlowEnrolment
    @Binds
    abstract fun provideFlowModalityEnrolment(impl: ModalityFlowEnrol): ModalityFlow

    @ModalityFlowConfirmation
    @Binds
    abstract fun provideModalityFlowConfirmation(impl: ModalityFlowConfirmIdentity): ModalityFlow

    @ModalityFlowEnrolmentLastBiometrics
    @Binds
    abstract fun provideModalityFlowEnrolmentLastBiometrics(impl: ModalityFlowEnrolLastBiometrics): ModalityFlow

    @ModalityFlowIdentification
    @Binds
    abstract fun provideFlowModalityIdentification(impl: ModalityFlowIdentify): ModalityFlow

    @ModalityFlowVerification
    @Binds
    abstract fun provideFlowModalityVerification(impl: ModalityFlowVerify): ModalityFlow

}

@Module
@InstallIn(SingletonComponent::class)
abstract class IdSyncModule {

    @Binds
    abstract fun provideSyncScheduler(impl: SyncSchedulerImpl): SyncManager

}

@Module
@InstallIn(SingletonComponent::class)
object IdDependenciesModule {

    @EncryptedSharedPreferences
    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(securityManager: SecurityManager): SharedPreferences =
        securityManager.buildEncryptedSharedPreferences(GLOBAL_SHARED_PREFS_FILENAME)

    @Provides
    @Singleton
    fun provideDomainToModuleApiAppResponse(): DomainToModuleApiAppResponse = DomainToModuleApiAppResponse

}
