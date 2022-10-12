@file:Suppress("DEPRECATION")

package com.simprints.id.testtools.di

import android.content.Context
import com.simprints.core.tools.time.TimeHelper
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
import com.simprints.id.di.AppModule
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.DeviceManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestAppModule(
    app: Application,
    private val eventRepositoryRule: DependencyRule = RealRule,
    private val sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
    private val deviceManagerRule: DependencyRule = RealRule,
    private val locationManagerRule: DependencyRule = RealRule,
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
        configManager: ConfigManager,
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
                configManager,
                loginManager,
                timeHelper,
                validatorFactory,
                sessionDataCache,
            )
        }

    override fun provideSessionEventsLocalDbManager(
        factory: EventDatabaseFactory
    ): EventLocalDataSource =
        sessionEventsLocalDbManagerRule.resolveDependency {
            super.provideSessionEventsLocalDbManager(
                factory
            )
        }

    override fun provideLocationManager(ctx: Context): LocationManager =
        locationManagerRule.resolveDependency {
            super.provideLocationManager(ctx)
        }

    override fun provideDeviceManager(
        connectivityHelper: ConnectivityHelper
    ): DeviceManager = deviceManagerRule.resolveDependency {
        super.provideDeviceManager(connectivityHelper)
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
