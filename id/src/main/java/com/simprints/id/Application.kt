package com.simprints.id

import android.content.Context
import com.simprints.core.CoreApplication
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.di.*
import com.simprints.id.tools.extensions.deviceId
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.DEVICE_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.logging.SimberBuilder
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
open class Application : CoreApplication() {

//    @Inject
//    lateinit var workerFactory: HiltWorkerFactory

    lateinit var component: AppComponent
    lateinit var orchestratorComponent: OrchestratorComponent
    private lateinit var applicationScope: CoroutineScope

    override fun attachBaseContext(base: Context) {
        LanguageHelper.init(base)
        val ctx = LanguageHelper.getLanguageConfigurationContext(base)
        super.attachBaseContext(ctx)
    }

    open fun createComponent() {
        component = DaggerAppComponent
            .builder()
            .application(this)
            .appModule(AppModule())
            .preferencesModule(PreferencesModule())
            .serializerModule(SerializerModule())
            .syncModule(SyncModule())
            .viewModelModule(ViewModelModule())
            .build()

    }

    open fun createOrchestratorComponent() {
        orchestratorComponent = component
            .getOrchestratorComponent().orchestratorModule(OrchestratorModule())
            .build()
    }

    open fun createApplicationCoroutineScope() {
        // For operations that shouldn’t be cancelled,
        // call them from a coroutine created by an
        // application CoroutineScope
        applicationScope = CoroutineScope(SupervisorJob())
    }

    override fun onCreate() {
        super.onCreate()
        initApplication()
    }

//    override fun getWorkManagerConfiguration() =
//        Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()


    open fun initApplication() {
        createComponent()
        handleUndeliverableExceptionInRxJava()
        createApplicationCoroutineScope()
        SimberBuilder.initialize(this)
        Simber.tag(DEVICE_ID, true).i(deviceId)
    }

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
            Simber.e(e)
            Simber.d("Undeliverable exception received", exceptionToPrint)
            exceptionToPrint.printStackTrace()
        }
    }
}
