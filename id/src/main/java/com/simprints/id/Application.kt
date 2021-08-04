package com.simprints.id

import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import com.simprints.core.CoreApplication
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.di.*
import com.simprints.infra.logging.Simber
import com.simprints.infra.logging.SimberBuilder
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

open class Application : CoreApplication() {

    lateinit var component: AppComponent
    lateinit var orchestratorComponent: OrchestratorComponent
    private lateinit var applicationScope: CoroutineScope

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

    open fun initApplication() {
        createComponent()
        handleUndeliverableExceptionInRxJava()
        createApplicationCoroutineScope()
        initKoin()
        SimberBuilder.initialize(this)
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

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@Application)
            loadKoinModules(listOf(module(override = true) {
                this.defineBuildersForCoreManagers()
                single(qualifier = named(APPLICATION_COROUTINE_SCOPE)) {
                    applicationScope
                }
            }))
        }
    }

    private fun Module.defineBuildersForCoreManagers() {
        factory { component.getPreferencesManager() }
        factory { component.getSessionEventsManager() }
        factory { component.getTimeHelper() }
        factory { component.getFingerprintRecordLocalDataSource() }
        factory { component.getFaceIdentityLocalDataSource() }
        factory { component.getImprovedSharedPreferences() }
        factory { component.getRemoteConfigWrapper() }
        factory { orchestratorComponent.getFlowManager() }
        factory { component.getPersonRepository() }
        factory { component.getImageRepository() }
        factory { component.getLoginManager() }
        factory { component.getLicenseRepository() }
        factory { component.getIdPreferencesManager() }
        factory { component.getSecurityManager() }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }

    companion object {
        const val APPLICATION_COROUTINE_SCOPE = "application_coroutine_scope"
    }
}
