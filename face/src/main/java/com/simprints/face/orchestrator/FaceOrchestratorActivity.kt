package com.simprints.face.orchestrator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.showToast
import com.simprints.core.tools.whenNonNull
import com.simprints.core.tools.whenNull
import com.simprints.face.capture.FaceCaptureActivity
import com.simprints.face.di.KoinInjector
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.face.match.FaceMatchActivity
import com.simprints.face.models.RankOneInitializer
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import org.koin.android.viewmodel.ext.android.viewModel

class FaceOrchestratorActivity : AppCompatActivity() {
    private val viewModel: FaceOrchestratorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KoinInjector.acquireFaceKoinModules()

        val iFaceRequest: IFaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException("No IFaceCaptureRequest found for FaceOrchestratorActivity")

        observeViewModel()

        getRankOneLicense()
            .whenNull { viewModel.missingLicense() }
            .whenNonNull { tryInitWithLicense(this, iFaceRequest) }
    }

    private fun tryInitWithLicense(rankOneLicense: String, iFaceRequest: IFaceRequest) {
        if (RankOneInitializer.tryInitWithLicense(this, rankOneLicense)) {
            viewModel.start(iFaceRequest)
        } else {
            viewModel.invalidLicense()
        }
    }

    private fun getRankOneLicense(): String? = try {
        String(assets.open("ROC.lic").readBytes())
    } catch (t: Throwable) {
        null
    }

    override fun onDestroy() {
        KoinInjector.releaseFaceKoinModules()
        super.onDestroy()
    }

    private fun observeViewModel() {
        viewModel.startCapture.observe(this, LiveDataEventWithContentObserver {
            startActivityForResult(FaceCaptureActivity.getStartingIntent(this, it), CAPTURE_REQUEST)
        })
        viewModel.flowFinished.observe(this, LiveDataEventWithContentObserver {
            val intent = Intent().putExtra(IFaceResponse.BUNDLE_KEY, it)
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
        viewModel.startMatching.observe(this, LiveDataEventWithContentObserver {
            startActivityForResult(FaceMatchActivity.getStartingIntent(this, it), MATCH_REQUEST)
        })
        viewModel.missingLicenseEvent.observe(this, LiveDataEventObserver {
            // TODO: this is temporary, should route user the an error screen
            showToast("RankOne license is missing")
            setResult(Activity.RESULT_CANCELED)
            finish()
        })
        viewModel.invalidLicenseEvent.observe(this, LiveDataEventObserver {
            // TODO: this is temporary, should route user the an error screen
            showToast("RankOne license is invalid")
            setResult(Activity.RESULT_CANCELED)
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_REQUEST) {
                viewModel.captureFinished(data?.getParcelableExtra(IFaceResponse.BUNDLE_KEY))
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        const val CAPTURE_REQUEST = 100
        const val MATCH_REQUEST = 101
    }
}
