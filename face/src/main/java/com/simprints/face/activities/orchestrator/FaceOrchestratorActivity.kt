package com.simprints.face.activities.orchestrator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.face.di.KoinInjector
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import org.koin.android.viewmodel.ext.android.viewModel

class FaceOrchestratorActivity : AppCompatActivity() {
    private val viewModel: FaceOrchestratorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KoinInjector.acquireFaceKoinModules()

        val iFaceRequest: IFaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException("No IFaceCaptureRequest found for FaceOrchestratorActivity")

        viewModel.start(iFaceRequest)
    }

    override fun onDestroy() {
        KoinInjector.releaseFaceKoinModules()
        super.onDestroy()
    }
}
