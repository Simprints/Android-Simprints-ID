package com.simprints.id

import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.multidex.MultiDexApplication
import com.simprints.core.tools.LineNumberDebugTree
import com.simprints.id.di.*
import com.simprints.id.tools.logging.LoggingConfigHelper
import com.simprints.id.tools.logging.NoLoggingConfigHelper
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import timber.log.Timber

open class Application : MultiDexApplication(), CameraXConfig.Provider {

    lateinit var component: AppComponent
    lateinit var orchestratorComponent: OrchestratorComponent

    open var loggingConfigHelper: LoggingConfigHelper = NoLoggingConfigHelper()

    open fun createComponent() {
        component = DaggerAppComponent
            .builder()
            .application(this)
            .appModule(AppModule())
            .preferencesModule(PreferencesModule())
            .serializerModule(SerializerModule())
            .syncModule(SyncModule())
            .build()
    }

    open fun createOrchestratorComponent() {
        orchestratorComponent = component
            .getOrchestratorComponent().orchestratorModule(OrchestratorModule())
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        initApplication()
    }

    open fun initApplication() {
        createComponent()

        if (loggingConfigHelper.loggingNeedsSetUp())
            loggingConfigHelper.setUpLogging()

        handleUndeliverableExceptionInRxJava()
        initKoin()
    }

    fun setupTimber() {
        if (BuildConfig.DEBUG)
            Timber.plant(LineNumberDebugTree())
    }

    override fun getCameraXConfig(): CameraXConfig = Camera2Config.defaultConfig()

    // RxJava doesn't allow not handled exceptions, when that happens the app crashes.
    // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#reason-handling
    // It can happen when an observable throws an exception, but the
    // chain has already terminated. E.g. given `chain = zip(network_call1, network_call2)`, when
    // phone goes offline network_calls1 fails and it stops `chain`. But even network_call2 will throw a
    // network exception and it won't be handled because the chain has already stopped.
    open fun handleUndeliverableExceptionInRxJava() {
        RxJavaPlugins.setErrorHandler { e ->
            var exceptionToPrint = e
            if (e is UndeliverableException) {
                exceptionToPrint = e.cause
            }
            Timber.d("Undeliverable exception received", exceptionToPrint)

            exceptionToPrint.printStackTrace()
            component.getCrashReportManager().logException(e)
        }
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@Application)
            loadKoinModules(listOf(module(override = true) {
                this.defineBuildersForCoreManagers()
            }))
        }
    }

    private fun Module.defineBuildersForCoreManagers() {
        factory { component.getPreferencesManager() }
        factory { component.getAnalyticsManager() }
        factory { component.getSessionEventsManager() }
        factory { component.getCrashReportManager() }
        factory { component.getTimeHelper() }
        factory { component.getFingerprintRecordLocalDataSource() }
        factory { component.getImprovedSharedPreferences() }
        factory { component.getRemoteConfigWrapper() }
        factory { component.getAndroidResourcesHelper() }
        factory { orchestratorComponent.getFlowManager() }
        factory { component.getPersonRepository() }
        factory { component.getImageRepository() }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }

}
