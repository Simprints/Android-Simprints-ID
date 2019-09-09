package com.simprints.fingerprint.activities.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed

abstract class FingerprintActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logActivityCreated()
        acquireFingerprintKoinModules()
    }

    override fun onDestroy() {
        super.onDestroy()
        logActivityDestroyed()
        releaseFingerprintKoinModules()
    }
}
