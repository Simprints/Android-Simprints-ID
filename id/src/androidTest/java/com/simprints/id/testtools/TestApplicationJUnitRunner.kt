package com.simprints.id.testtools

import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.simprints.id.Application
import com.simprints.id.commontesttools.TestApplication

// Used as the default instrumentation test runner to be able to instantiate the TestApplication
class TestApplicationJUnitRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context) as Application
    }
}
