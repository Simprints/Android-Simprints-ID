package com.simprints.id.testtools.di

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.Application
import com.simprints.id.activities.qrcapture.tools.*
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.device.ConnectivityHelper
import com.simprints.id.tools.device.DeviceManager
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule
import io.mockk.every
import io.mockk.mockk

class TestAppModule(
    val app: Application,
    private val eventRepositoryRule: DependencyRule = RealRule,
    private val sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
    private val deviceManagerRule: DependencyRule = RealRule,
    private val locationManagerRule: DependencyRule = RealRule,
    private val cameraHelperRule: DependencyRule = RealRule,
    private val qrPreviewBuilderRule: DependencyRule = RealRule,
    private val qrCodeDetectorRule: DependencyRule = RealRule,
    private val qrCodeProducerRule: DependencyRule = RealRule
) : AppModule() {

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
