package com.simprints.face.base

import android.os.Bundle
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.face.di.KoinInjector

open class FaceActivity : BaseSplitActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KoinInjector.acquireFaceKoinModules()
    }

    override fun onDestroy() {
        KoinInjector.releaseFaceKoinModules()
        super.onDestroy()
    }
}
