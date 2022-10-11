package com.simprints.fingerprint

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.fingerprint.di.KoinInjector
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module

class KoinTestRule(
    private val modules: List<Module>
) : TestWatcher() {
    override fun starting(description: Description) {
        LanguageHelper.init(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)
        startKoin {
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)
        }
        KoinInjector.acquireFingerprintKoinModules()
        loadKoinModules(modules)
    }

    override fun finished(description: Description) {
        stopKoin()
    }
}
