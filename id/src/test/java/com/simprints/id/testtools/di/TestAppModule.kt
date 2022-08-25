package com.simprints.id.testtools.di

import android.content.Context
import android.content.SharedPreferences
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.RecentEventsPreferencesManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.local.EventDatabaseFactory
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.event.local.SessionDataCache
import com.simprints.eventsystem.event.local.SessionDataCacheImpl
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.id.Application
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.di.AppModule
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.DeviceManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule
import io.mockk.every
import io.mockk.mockk

class TestAppModule(
    val app: Application,
    private val remoteDbManagerRule: DependencyRule = RealRule,
    private val dbManagerRule: DependencyRule = RealRule,
    private val secureDataManagerRule: DependencyRule = RealRule,
    private val loginInfoManagerRule: DependencyRule = RealRule,
    private val randomGeneratorRule: DependencyRule = RealRule,
    private val keystoreManagerRule: DependencyRule = RealRule,
    private val eventRepositoryRule: DependencyRule = RealRule,
    private val sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
    private val sessionEventsRemoteDbManagerRule: DependencyRule = RealRule,
    private val simNetworkUtilsRule: DependencyRule = RealRule,
    private val longConsentManagerRule: DependencyRule = RealRule,
    private val secureApiInterfaceRule: DependencyRule = RealRule,
    private val syncStatusDatabaseRule: DependencyRule = RealRule,
    private val deviceManagerRule: DependencyRule = RealRule,
    private val recentEventsPreferencesManagerRule: DependencyRule = RealRule,
    private val remoteProjectInfoProviderRule: DependencyRule = RealRule,
    private val locationManagerRule: DependencyRule = RealRule,
    private val baseUrlProviderRule: DependencyRule = RealRule,
    private val encryptedSharedPreferencesRule: DependencyRule = DependencyRule.ReplaceRule {
        app.getSharedPreferences("test", 0)
    },
    private val cameraHelperRule: DependencyRule = RealRule,
    private val qrPreviewBuilderRule: DependencyRule = RealRule,
    private val qrCodeDetectorRule: DependencyRule = RealRule,
    private val qrCodeProducerRule: DependencyRule = RealRule
) : AppModule() {


    override fun provideSessionDataCache(app: EventSystemApplication): SessionDataCache =
        SessionDataCacheImpl(app)

    override fun provideEventRepository(
        ctx: Context,
        eventLocalDataSource: EventLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        idPreferencesManager: IdPreferencesManager,
        loginManager: LoginManager,
        timeHelper: TimeHelper,
        validatorFactory: SessionEventValidatorsFactory,
        sessionDataCache: SessionDataCache
    ): EventRepository =
        eventRepositoryRule.resolveDependency {
            super.provideEventRepository(
                ctx,
                eventLocalDataSource,
                eventRemoteDataSource,
                idPreferencesManager,
                loginManager,
                timeHelper,
                validatorFactory,
                sessionDataCache
            )
        }

    // Android keystore is not available in unit tests - so it returns a mock that builds the standard shared prefs.
    override fun provideEncryptedSharedPreferences(
        builder: SecurityManager
    ): SharedPreferences = mockk<SharedPreferences>().apply {
        every { builder.buildEncryptedSharedPreferences(any()) } answers {
            app.getSharedPreferences(
                args[0] as String,
                Context.MODE_PRIVATE
            )
        }
    }

    override fun provideSessionEventsLocalDbManager(
        factory: EventDatabaseFactory
    ): EventLocalDataSource =
        sessionEventsLocalDbManagerRule.resolveDependency {
            super.provideSessionEventsLocalDbManager(
                factory
            )
        }

    override fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils =
        simNetworkUtilsRule.resolveDependency { super.provideSimNetworkUtils(ctx) }

    override fun provideLocationManager(ctx: Context): LocationManager =
        locationManagerRule.resolveDependency {
            super.provideLocationManager(ctx)
        }

    override fun provideDeviceManager(
        connectivityHelper: ConnectivityHelper
    ): DeviceManager = deviceManagerRule.resolveDependency {
        super.provideDeviceManager(connectivityHelper)
    }

    override fun provideRecentEventsPreferencesManager(
        prefs: ImprovedSharedPreferences
    ): RecentEventsPreferencesManager = recentEventsPreferencesManagerRule.resolveDependency {
        super.provideRecentEventsPreferencesManager(prefs)
    }

    override fun provideCameraHelper(
        context: Context,
        previewBuilder: QrPreviewBuilder,
        cameraFocusManager: CameraFocusManager
    ): CameraHelper = cameraHelperRule.resolveDependency {
        super.provideCameraHelper(context, previewBuilder, cameraFocusManager)
    }

    override fun provideQrPreviewBuilder(): QrPreviewBuilder {
        return qrPreviewBuilderRule.resolveDependency {
            super.provideQrPreviewBuilder()
        }
    }

    override fun provideQrCodeProducer(
        qrCodeDetector: QrCodeDetector,
    ): QrCodeProducer = qrCodeProducerRule.resolveDependency {
        super.provideQrCodeProducer(qrCodeDetector)
    }

    override fun provideQrCodeDetector(
    ): QrCodeDetector = qrCodeDetectorRule.resolveDependency {
        super.provideQrCodeDetector()
    }
}
