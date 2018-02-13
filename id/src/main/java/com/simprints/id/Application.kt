package com.simprints.id

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.DaggerAppComponent
import com.simprints.id.domain.sessionParameters.extractors.Extractor
import com.simprints.id.domain.sessionParameters.extractors.ParameterExtractor
import com.simprints.id.domain.sessionParameters.readers.OptionalParameterReader
import com.simprints.id.domain.sessionParameters.readers.Reader
import com.simprints.id.domain.sessionParameters.validators.NoOpValidator
import com.simprints.id.domain.sessionParameters.validators.Validator
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants.SIMPRINTS_PROJECT_ID
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

    private val invalidProjectIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_PROJECT_ID)
    }

    private val projectIdReader: Reader<String> by lazy {
        OptionalParameterReader(SIMPRINTS_PROJECT_ID, "", invalidProjectIdError)
    }

    private val projectIdValidator: Validator<String> by lazy {
        NoOpValidator<String>()
    }

    private val projectIdExtractor: Extractor<String> by lazy {
        ParameterExtractor(projectIdReader, projectIdValidator)
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
