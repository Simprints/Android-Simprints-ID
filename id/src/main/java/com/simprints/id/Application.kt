package com.simprints.id

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.DaggerAppComponent
import io.fabric.sdk.android.Fabric
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject

open class Application : MultiDexApplication() {

    companion object {
        lateinit var component: AppComponent
    }

    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager


    fun createComponent() {
        Application.component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        createComponent()
        Application.component.inject(this)

        initModules()
    }

    open fun initModules() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
            .build()
        Fabric.Builder(this).kits(crashlyticsKit).debuggable(BuildConfig.DEBUG).build()

        dbManager.initialiseDb()

        handleUndeliverableExceptionInRxJava()
    }

    // RxJava doesn't allow not handled exceptions, when that happens the app crashes.
    // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
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
        }
    }
}
