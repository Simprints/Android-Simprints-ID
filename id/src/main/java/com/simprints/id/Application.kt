package com.simprints.id

import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import com.simprints.core.CoreApplication
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.di.*
import com.simprints.logging.Simber
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

open class Application : CoreApplication() {

    lateinit var component: AppComponent
    lateinit var orchestratorComponent: OrchestratorComponent

    override fun attachBaseContext(base: Context) {
        LanguageHelper.init(base)
        val ctx = LanguageHelper.getLanguageConfigurationContext(base)
        super.attachBaseContext(ctx)
        SplitCompat.install(this)
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

        // Create events for the subjects that are stored in the subjects (old architecture)
        // and they still need to be uploaded. The new architecture uploads only events.
        // The operation is not trivial for SID usage, so it can be performed in background.
        inBackground {
            component.getSubjectToEventMigrationManager().migrateSubjectToSyncToEventsDb()
        }
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
        handleUndeliverableExceptionInRxJava()
        initKoin()
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
            Simber.d("Undeliverable exception received", exceptionToPrint)

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
        factory { component.getFaceIdentityLocalDataSource() }
        factory { component.getImprovedSharedPreferences() }
        factory { component.getRemoteConfigWrapper() }
        factory { orchestratorComponent.getFlowManager() }
        factory { component.getPersonRepository() }
        factory { component.getImageRepository() }
        factory { component.getSimClientFactory() }
        factory { component.getLicenseRepository() }
        factory { component.getIdPreferencesManager() }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }

}
