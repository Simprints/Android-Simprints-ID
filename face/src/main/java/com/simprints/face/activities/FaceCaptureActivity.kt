package com.simprints.face.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.simprints.face.R
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.moduleapi.face.responses.IFaceResponse
import kotlinx.android.synthetic.main.activity_face_capture.*
import java.util.*

class FaceCaptureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_capture)


        val handler = Handler()
        var nSeconds = 0
        Timer().scheduleAtFixedRate(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if (nSeconds == 2) {
                    val intent = Intent().apply {
                        putExtra(IFaceResponse.BUNDLE_KEY, generateFakeCaptureResponse())
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }

                nSeconds++
                handler.post {
                    count.text = "$nSeconds seconds"
                }
            }
        }, 0, 1000)
    }

    companion object {
        // this will be removed later, created like this to be used in tests as well
        fun generateFakeCaptureResponse(): FaceCaptureResponse {
            val securedImageRef = SecuredImageRef(
                path = Path(arrayOf("file://someFile"))
            )
            val sample = FaceSample(UUID.randomUUID().toString(), ByteArray(0), securedImageRef)
            val result = FaceCaptureResult(0, sample)
            val captureResults = listOf(result)
            return FaceCaptureResponse(captureResults)
        }
    }
}
