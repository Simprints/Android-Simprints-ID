package com.simprints.face.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.simprints.face.R
import com.simprints.face.data.moduleapi.face.DomainToFaceResponse.fromDomainToFaceResponse
import com.simprints.face.data.moduleapi.face.FaceToDomainRequest.fromFaceToDomainRequest
import com.simprints.face.data.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.face.data.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.requests.FaceVerifyRequest
import com.simprints.face.data.moduleapi.face.responses.FaceEnrolResponse
import com.simprints.face.data.moduleapi.face.responses.FaceIdentifyResponse
import com.simprints.face.data.moduleapi.face.responses.FaceResponse
import com.simprints.face.data.moduleapi.face.responses.FaceVerifyResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchingResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceTier
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import kotlinx.android.synthetic.main.activity_face_capture.*
import java.util.*

class FaceCaptureActivity : AppCompatActivity() {

    lateinit var faceRequest: FaceRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_capture)

        val iFaceRequest: IFaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException()
        faceRequest = fromFaceToDomainRequest(iFaceRequest)

        val handler = Handler()
        var nSeconds = 0
        Timer().scheduleAtFixedRate(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if(nSeconds == 5) {
                    setResult(Activity.RESULT_OK, Intent().apply { putExtra(IFaceResponse.BUNDLE_KEY, fromDomainToFaceResponse(getFakeResponse())) })
                    finish()
                }

                nSeconds++
                handler.post {
                    count.text = "$nSeconds seconds"
                }
            }
        }, 0, 1000)
    }

    fun getFakeResponse(): FaceResponse =
        when(faceRequest) {
            is FaceEnrolRequest -> FaceEnrolResponse("some_guid")
            is FaceVerifyRequest-> FaceVerifyResponse(FaceMatchingResult("some_guid", 0, FaceTier.TIER_1))
            is FaceIdentifyRequest -> FaceIdentifyResponse(listOf(FaceMatchingResult("some_guid", 0, FaceTier.TIER_1)))
            else -> throw IllegalArgumentException("Invalid Request")
        }

}
