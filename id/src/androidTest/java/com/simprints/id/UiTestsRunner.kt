package com.simprints.id

import android.app.Instrumentation
import android.content.Context
import androidx.annotation.NonNull
import androidx.test.runner.AndroidJUnitRunner
import com.simprints.id.commontesttools.TestApplication

// Not used - useful if a custom Application is required
class UiTestsRunner : AndroidJUnitRunner() {

    @NonNull
    @Throws(InstantiationException::class, IllegalAccessException::class, ClassNotFoundException::class)
    override fun newApplication(@NonNull cl: ClassLoader, @NonNull className: String, @NonNull context: Context): Application {
        return Instrumentation.newApplication(TestApplication::class.java, context) as Application
    }
}
