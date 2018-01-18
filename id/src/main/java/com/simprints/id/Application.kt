package com.simprints.id

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.DaggerAppComponent
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import javax.inject.Inject
import android.app.Application as AndroidApplication


class Application : MultiDexApplication() {

    companion object {
        lateinit var component: AppComponent
    }

    fun createComponent() {
        Application.component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
    }

    @Inject
    lateinit var fabric: Fabric

    override fun onCreate() {
        super.onCreate()
        createComponent()
        Application.component.inject(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val releaseBuild = BuildConfig.DEBUG == false
        if (releaseBuild) {
            val fabric = Fabric.Builder(this).kits(Crashlytics()).debuggable(BuildConfig.DEBUG).build()
            Fabric.with(fabric)
        }
    }
}
