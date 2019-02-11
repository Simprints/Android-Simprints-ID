package com.simprints.id.testtools

import androidx.test.runner.AndroidJUnitRunner

// Used as the default instrumentation test runner to be able to instantiate the TestApplication
class TestApplicationJUnitRunner : AndroidJUnitRunner() {
//
//    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
//        return super.newApplication(cl, TestApplication::class.java.name, context) as Application
//    }
}
