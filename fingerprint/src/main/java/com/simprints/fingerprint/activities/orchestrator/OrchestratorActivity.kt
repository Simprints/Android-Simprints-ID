package com.simprints.fingerprint.activities.orchestrator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.simprints.fingerprint.di.koinModule
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class OrchestratorActivity : AppCompatActivity() {

    private val viewModel: OrchestratorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(koinModule)

        viewModel.finishedResult.observe(this, Observer {
            setResult(it.resultCode, it.resultData)
            finish()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(koinModule)
    }
}
