package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.domain.workflow.WorkflowCacheClearer
import com.simprints.id.activities.alert.AlertContract
import com.simprints.id.activities.alert.AlertPresenter
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentContract
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentPresenter
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherContract
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherPresenter
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSourceImpl
import com.simprints.id.activities.dashboard.cards.daily_activity.displayer.DashboardDailyActivityCardDisplayer
import com.simprints.id.activities.dashboard.cards.daily_activity.displayer.DashboardDailyActivityCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepositoryImpl
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayer
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepositoryImpl
import com.simprints.id.activities.fetchguid.FetchGuidHelper
import com.simprints.id.activities.fetchguid.FetchGuidHelperImpl
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.tools.LoginActivityHelperImpl
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelper
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelperImpl
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.consent.longconsent.LongConsentRepositoryImpl
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSourceImpl
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSourceImpl
import com.simprints.id.data.db.subject.domain.SubjectFactory
import com.simprints.id.data.db.subject.domain.SubjectFactoryImpl
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory
import com.simprints.id.domain.moduleapi.face.FaceRequestFactoryImpl
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactory
import com.simprints.id.domain.moduleapi.fingerprint.FingerprintRequestFactoryImpl
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.exitformhandler.ExitFormHelperImpl
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.ModuleRepositoryImpl
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
import com.simprints.id.secure.*
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.SecurityStateProcessorImpl
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSource
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSourceImpl
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSourceImpl
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.secure.securitystate.repository.SecurityStateRepositoryImpl
import com.simprints.id.services.guidselection.GuidSelectionManager
import com.simprints.id.services.guidselection.GuidSelectionManagerImpl
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.securitystate.SecurityStateSchedulerImpl
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.SyncSchedulerImpl
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.EventDownSyncHelperImpl
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilderImpl
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderTask
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderTaskImpl
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.EventSyncManagerImpl
import com.simprints.id.services.sync.events.master.EventSyncStateProcessor
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImpl
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.internal.EventSyncCacheImpl
import com.simprints.id.services.sync.events.master.internal.SyncWorkersLiveDataProvider
import com.simprints.id.services.sync.events.master.internal.SyncWorkersLiveDataProviderImpl
import com.simprints.id.services.sync.events.master.workers.EventSyncSubMasterWorkersBuilder
import com.simprints.id.services.sync.events.master.workers.EventSyncSubMasterWorkersBuilderImpl
import com.simprints.id.services.sync.events.up.EventUpSyncHelper
import com.simprints.id.services.sync.events.up.EventUpSyncHelperImpl
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilder
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilderImpl
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.id.services.sync.images.up.ImageUpSyncSchedulerImpl
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.LocationManagerImpl
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.ConnectivityHelperImpl
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.device.DeviceManagerImpl
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.SecurityManager.Companion.GLOBAL_SHARED_PREFS_FILENAME
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AbsolutePath

@Module
@InstallIn(SingletonComponent::class)
abstract class IdAppModule {

    @AssistedFactory
    interface AlertPresenterFactory {
        fun create(
            view: AlertContract.View,
            alertType: AlertType,
        ): AlertPresenter
    }

    @AssistedFactory
    interface CheckLoginFromIntentPresenterFactory {
        fun create(
            view: CheckLoginFromIntentContract.View,
        ): CheckLoginFromIntentPresenter
    }

    @AssistedFactory
    interface CheckLoginFromMainLauncherPresenterFactory {
        fun create(
            view: CheckLoginFromMainLauncherContract.View,
        ): CheckLoginFromMainLauncherPresenter
    }

    @Binds
    abstract fun provideGuidSelectionManager(impl: GuidSelectionManagerImpl): GuidSelectionManager

    @Binds
    abstract fun provideModuleRepository(impl: ModuleRepositoryImpl): ModuleRepository

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
    abstract fun provideQrCodeDetector(impl: QrCodeDetectorImpl): QrCodeDetector

    @Binds
    abstract fun provideQrCodeProducer(impl: QrCodeProducerImpl): QrCodeProducer

    @Binds
    abstract fun provideCameraHelper(impl: CameraHelperImpl): CameraHelper

    @Binds
    abstract fun provideQrPreviewBuilder(impl: QrPreviewBuilderImpl): QrPreviewBuilder

    @Binds
    abstract fun provideCameraFocusManager(impl: CameraFocusManagerImpl): CameraFocusManager

    @Binds
    abstract fun provideExitFormHelper(impl: ExitFormHelperImpl): ExitFormHelper

    @Binds
    abstract fun provideEnrolResponseAdjudicationHelper(impl: EnrolResponseAdjudicationHelperImpl): EnrolResponseAdjudicationHelper

    @Binds
    abstract fun provideLocationManager(impl: LocationManagerImpl): LocationManager
}

@Module
@InstallIn(SingletonComponent::class)
abstract class IdDashboardModule {

    @Binds
    abstract fun provideDashboardDailyActivityRepository(impl: DashboardDailyActivityRepositoryImpl): DashboardDailyActivityRepository

    @Binds
    abstract fun provideDailyActivityLocalDataSource(impl: DailyActivityLocalDataSourceImpl): DailyActivityLocalDataSource

    @Binds
    abstract fun provideDashboardSyncCardStateRepository(impl: DashboardSyncCardStateRepositoryImpl): DashboardSyncCardStateRepository

    @Binds
    abstract fun provideDashboardDailyActivityCardDisplayer(impl: DashboardDailyActivityCardDisplayerImpl): DashboardDailyActivityCardDisplayer

    @Binds
    abstract fun provideDashboardProjectDetailsCardDisplayer(impl: DashboardProjectDetailsCardDisplayerImpl): DashboardProjectDetailsCardDisplayer

    @Binds
    abstract fun provideDashboardSyncCardDisplayer(impl: DashboardSyncCardDisplayerImpl): DashboardSyncCardDisplayer
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

    @Binds
    abstract fun provideLoginActivityHelper(impl: LoginActivityHelperImpl): LoginActivityHelper

    @Binds
    abstract fun provideFetchGuidHelper(impl: FetchGuidHelperImpl): FetchGuidHelper

    @Binds
    abstract fun provideDeviceManager(impl: DeviceManagerImpl): DeviceManager

    @Binds
    abstract fun provideConnectivityHelper(impl: ConnectivityHelperImpl): ConnectivityHelper

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

    @Binds
    abstract fun provideImageUpSyncScheduler(impl: ImageUpSyncSchedulerImpl): ImageUpSyncScheduler

    @Binds
    abstract fun provideEventSyncManager(impl: EventSyncManagerImpl): EventSyncManager

    @Binds
    abstract fun provideEventSyncStateProcessor(impl: EventSyncStateProcessorImpl): EventSyncStateProcessor

    @Binds
    abstract fun provideEventSyncCache(impl: EventSyncCacheImpl): EventSyncCache

    @Binds
    abstract fun provideSyncWorkersLiveDataProvider(impl: SyncWorkersLiveDataProviderImpl): SyncWorkersLiveDataProvider

    @Binds
    abstract fun provideEventUpSyncHelper(impl: EventUpSyncHelperImpl): EventUpSyncHelper

    @Binds
    abstract fun provideEventUpSyncWorkersBuilder(impl: EventUpSyncWorkersBuilderImpl): EventUpSyncWorkersBuilder

    @Binds
    abstract fun provideEventSyncSubMasterWorkersBuilder(impl: EventSyncSubMasterWorkersBuilderImpl): EventSyncSubMasterWorkersBuilder

    @Binds
    abstract fun provideEventDownSyncWorkersBuilder(impl: EventDownSyncWorkersBuilderImpl): EventDownSyncWorkersBuilder

    @Binds
    abstract fun provideEventDownSyncHelper(impl: EventDownSyncHelperImpl): EventDownSyncHelper

    @Binds
    abstract fun provideEventDownSyncDownloaderTask(impl: EventDownSyncDownloaderTaskImpl): EventDownSyncDownloaderTask

    @Binds
    abstract fun provideSubjectFactory(impl: SubjectFactoryImpl): SubjectFactory
}

@Module
@InstallIn(SingletonComponent::class)
abstract class IdSecurityModule {

    @Binds
    abstract fun provideSecurityStateRepository(impl: SecurityStateRepositoryImpl): SecurityStateRepository

    @Binds
    abstract fun provideSecurityStateRemoteDataSource(impl: SecurityStateRemoteDataSourceImpl): SecurityStateRemoteDataSource

    @Binds
    abstract fun provideSecurityStateLocalDataSource(impl: SecurityStateLocalDataSourceImpl): SecurityStateLocalDataSource

    @Binds
    abstract fun provideSecurityStateProcessor(impl: SecurityStateProcessorImpl): SecurityStateProcessor

    @Binds
    abstract fun provideSignerManager(impl: SignerManagerImpl): SignerManager

    @Binds
    abstract fun provideSecurityStateScheduler(impl: SecurityStateSchedulerImpl): SecurityStateScheduler

    @Binds
    abstract fun provideProjectAuthenticator(impl: ProjectAuthenticatorImpl): ProjectAuthenticator

    @Binds
    abstract fun provideAuthenticationHelper(impl: AuthenticationHelperImpl): AuthenticationHelper
}

@Module
@InstallIn(SingletonComponent::class)
abstract class IdDataModule {

    @Binds
    abstract fun provideLongConsentRepository(impl: LongConsentRepositoryImpl): LongConsentRepository

    @Binds
    abstract fun provideLongConsentLocalDataSource(impl: LongConsentLocalDataSourceImpl): LongConsentLocalDataSource

    @Binds
    abstract fun provideLongConsentRemoteDataSource(impl: LongConsentRemoteDataSourceImpl): LongConsentRemoteDataSource

}

@Module
@InstallIn(SingletonComponent::class)
object IdDependenciesModule {

    @AbsolutePath
    @Provides
    @Singleton
    fun provideAbsolutePath(@ApplicationContext context: Context): String =
        context.filesDir.absolutePath

    @EncryptedSharedPreferences
    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(securityManager: SecurityManager): SharedPreferences =
        securityManager.buildEncryptedSharedPreferences(GLOBAL_SHARED_PREFS_FILENAME)

    @Provides
    @Singleton
    fun provideDomainToModuleApiAppResponse(): DomainToModuleApiAppResponse =
        DomainToModuleApiAppResponse

}

// TODO remove when the event sync manager has been moved into its one module
@Module
@InstallIn(SingletonComponent::class)
abstract class TemporaryFeatureDashboardModule {
    @Binds
    abstract fun provideEventSyncManager(impl: EventSyncManagerImpl): com.simprints.feature.dashboard.sync.EventSyncManager

    @Binds
    abstract fun provideEventSyncCache(impl: EventSyncCacheImpl): com.simprints.feature.dashboard.sync.EventSyncCache

    @Binds
    abstract fun provideDeviceManager(impl: DeviceManagerImpl): com.simprints.feature.dashboard.sync.DeviceManager
}
