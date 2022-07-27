package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.lyft.kronos.AndroidClockFactory
import com.simprints.core.domain.modality.toMode
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.RecentEventsPreferencesManager
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtilsImpl
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.EventRepositoryImpl
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.eventsystem.event.local.*
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.id.Application
import com.simprints.id.BuildConfig.VERSION_NAME
import com.simprints.id.activities.fetchguid.FetchGuidHelper
import com.simprints.id.activities.fetchguid.FetchGuidHelperImpl
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManagerImpl
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.exitformhandler.ExitFormHelperImpl
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.ModuleRepositoryImpl
import com.simprints.id.network.ImageUrlProvider
import com.simprints.id.network.ImageUrlProviderImpl
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.EnrolmentHelperImpl
import com.simprints.id.orchestrator.PersonCreationEventHelper
import com.simprints.id.orchestrator.PersonCreationEventHelperImpl
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.StepEncoder
import com.simprints.id.orchestrator.cache.StepEncoderImpl
import com.simprints.id.services.guidselection.GuidSelectionManager
import com.simprints.id.services.guidselection.GuidSelectionManagerImpl
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.LocationManagerImpl
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.ConnectivityHelperImpl
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.device.DeviceManagerImpl
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.time.KronosTimeHelperImpl
import com.simprints.infra.login.db.FirebaseManagerImpl
import com.simprints.infra.login.db.RemoteDbManager
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.LoginInfoManagerImpl
import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.login.network.SimApiClientFactoryImpl
import com.simprints.infra.network.url.BaseUrlProvider
import com.simprints.infra.network.url.BaseUrlProviderImpl
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilder
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilderImpl
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider.Companion.FILENAME_FOR_REALM_KEY_SHARED_PREFS
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProviderImpl
import com.simprints.infra.security.random.RandomGenerator
import com.simprints.infra.security.random.RandomGeneratorImpl
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
open class AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    @Singleton
    open fun provideRemoteDbManager(
        loginInfoManager: LoginInfoManager,
        context: Context,
        dispatcher: DispatcherProvider
    ): RemoteDbManager = FirebaseManagerImpl(loginInfoManager, context)

    @Provides
    @Singleton
    open fun provideLoginInfoManager(ctx: Context): LoginInfoManager = LoginInfoManagerImpl(ctx)

    @Provides
    @Singleton
    open fun provideRecentEventsPreferencesManager(prefs: ImprovedSharedPreferences): RecentEventsPreferencesManager =
        RecentEventsPreferencesManagerImpl(prefs)

    @Provides
    open fun provideSessionDataCache(app: EventSystemApplication): SessionDataCache =
        SessionDataCacheImpl(app)

    @Provides
    open fun provideEventSystemApplication(): EventSystemApplication = EventSystemApplication()

    @Provides
    @Singleton
    open fun provideRandomGenerator(): RandomGenerator = RandomGeneratorImpl()

    @Provides
    @Singleton
    open fun provideSecureLocalDbKeyProvider(
        builder: EncryptedSharedPreferencesBuilder,
        randomGenerator: RandomGenerator
    ): SecureLocalDbKeyProvider =
        SecureLocalDbKeyProviderImpl(
            builder.buildEncryptedSharedPreferences(FILENAME_FOR_REALM_KEY_SHARED_PREFS),
            randomGenerator
        )

    @Provides
    @Singleton
    open fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    open fun provideImageUrlProvider(
        projectLocalDataSource: ProjectLocalDataSource,
        loginInfoManager: LoginInfoManager
    ): ImageUrlProvider = ImageUrlProviderImpl(
        projectLocalDataSource,
        loginInfoManager
    )

    @Provides
    open fun provideBaseUrlProvider(ctx: Context): BaseUrlProvider =
        BaseUrlProviderImpl(ctx)

    @Provides
    open fun provideSimApiClientFactory(
        ctx: Context,
        remoteDbManager: RemoteDbManager,
        baseUrlProvider: BaseUrlProvider
    ): SimApiClientFactory = SimApiClientFactoryImpl(
        baseUrlProvider,
        ctx.deviceId,
        ctx,
        ctx.packageVersionName,
        remoteDbManager,
    )

    @Provides
    @Singleton
    // https://github.com/lyft/Kronos-Android
    fun provideTimeHelper(app: Application): TimeHelper = KronosTimeHelperImpl(
        AndroidClockFactory.createKronosClock(
            app,
            requestTimeoutMs = TimeUnit.SECONDS.toMillis(60),
            minWaitTimeBetweenSyncMs = TimeUnit.MINUTES.toMillis(30),
            cacheExpirationMs = TimeUnit.MINUTES.toMillis(30)
        )
    )


    @Provides
    open fun provideSessionEventValidatorsBuilder(): SessionEventValidatorsFactory =
        SessionEventValidatorsFactoryImpl()

    @Provides
    open fun provideDbEventDatabaseFactory(
        ctx: Context,
        secureDataManager: SecureLocalDbKeyProvider,
    ): EventDatabaseFactory =
        DbEventDatabaseFactoryImpl(ctx, secureDataManager)

    @Provides
    @Singleton
    open fun provideSessionEventsLocalDbManager(
        factory: EventDatabaseFactory
    ): EventLocalDataSource =
        EventLocalDataSourceImpl(factory)

    @Provides
    @Singleton
    open fun provideEventRepository(
        ctx: Context,
        eventLocalDataSource: EventLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        idPreferencesManager: IdPreferencesManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        validatorFactory: SessionEventValidatorsFactory,
        sessionDataCache: SessionDataCache
    ): EventRepository =
        EventRepositoryImpl(
            ctx.deviceId,
            ctx.packageVersionName,
            loginInfoManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            timeHelper,
            validatorFactory,
            VERSION_NAME,
            sessionDataCache,
            idPreferencesManager.language,
            idPreferencesManager.modalities.map { it.toMode() }
        )

    @Provides
    fun provideModuleRepository(
        preferencesManager: IdPreferencesManager,
        subjectRepository: SubjectRepository
    ): ModuleRepository = ModuleRepositoryImpl(
        preferencesManager,
        subjectRepository
    )

    @Provides
    open fun provideGuidSelectionManager(
        context: Context,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        eventRepository: EventRepository
    ): GuidSelectionManager =
        GuidSelectionManagerImpl(
            context.deviceId,
            loginInfoManager,
            timeHelper,
            eventRepository
        )

    @Provides
    open fun provideExitFormHandler(): ExitFormHelper = ExitFormHelperImpl()

    @Provides
    open fun provideGuidFetchGuidHelper(
        downSyncHelper: EventDownSyncHelper,
        subjectRepository: SubjectRepository,
        preferencesManager: IdPreferencesManager
    ): FetchGuidHelper =
        FetchGuidHelperImpl(
            downSyncHelper,
            subjectRepository,
            preferencesManager
        )

    @Provides
    open fun provideEncryptedSharedPreferencesBuilder(app: Application): EncryptedSharedPreferencesBuilder =
        EncryptedSharedPreferencesBuilderImpl(app)

    @Provides
    @Named("EncryptedSharedPreferences")
    open fun provideEncryptedSharedPreferences(builder: EncryptedSharedPreferencesBuilder): SharedPreferences =
        builder.buildEncryptedSharedPreferences()

    @Provides
    open fun provideDeviceManager(connectivityHelper: ConnectivityHelper): DeviceManager =
        DeviceManagerImpl(connectivityHelper)

    @Provides
    open fun provideConnectivityHelper(ctx: Context): ConnectivityHelper =
        ConnectivityHelperImpl(ctx)

    @Provides
    open fun provideLocationManager(ctx: Context): LocationManager = LocationManagerImpl(ctx)

    @Provides
    open fun provideCameraHelper(
        context: Context,
        previewBuilder: QrPreviewBuilder,
        cameraFocusManager: CameraFocusManager
    ): CameraHelper = CameraHelperImpl(context, previewBuilder, cameraFocusManager)

    @Provides
    open fun provideCameraFocusManager(): CameraFocusManager = CameraFocusManagerImpl()

    @Provides
    open fun provideQrPreviewBuilder(): QrPreviewBuilder = QrPreviewBuilderImpl()

    @Provides
    @ExperimentalCoroutinesApi
    open fun provideQrCodeProducer(
        qrCodeDetector: QrCodeDetector,
    ): QrCodeProducer = QrCodeProducerImpl(qrCodeDetector)

    @Provides
    open fun provideQrCodeDetector(): QrCodeDetector = QrCodeDetectorImpl()

    @Provides
    fun provideHotCache(
        @Named("EncryptedSharedPreferences") sharedPrefs: SharedPreferences,
        stepEncoder: StepEncoder
    ): HotCache = HotCacheImpl(sharedPrefs, stepEncoder)

    @Provides
    fun provideStepEncoder(): StepEncoder = StepEncoderImpl()

    @Provides
    fun provideEnrolmentHelper(
        subjectRepository: SubjectRepository,
        eventRepository: EventRepository,
        timeHelper: TimeHelper
    ): EnrolmentHelper = EnrolmentHelperImpl(subjectRepository, eventRepository, timeHelper)

    @Provides
    fun providePersonCreationEventHelper(
        eventRepository: EventRepository,
        timeHelper: TimeHelper,
        encodingUtils: EncodingUtils
    ): PersonCreationEventHelper =
        PersonCreationEventHelperImpl(eventRepository, timeHelper, encodingUtils)

    @Provides
    open fun provideDispatcher(): DispatcherProvider = DefaultDispatcherProvider()

}

