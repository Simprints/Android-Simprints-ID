package com.simprints.face.match

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.face.R
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceRequest
import org.koin.android.viewmodel.ext.android.viewModel

class FaceMatchActivity : AppCompatActivity() {
    private val vm: FaceMatchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_match)

        val faceRequest: FaceRequest = this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
            ?: throw InvalidFaceRequestException("No IFaceRequest found for FaceMatchActivity")

        vm.setupMatch(faceRequest)
    }

    override fun onBackPressed() {}

    companion object {
        fun getStartingIntent(context: Context, faceMatchRequest: FaceMatchRequest): Intent =
            Intent(context, FaceMatchActivity::class.java).apply {
                putExtra(IFaceRequest.BUNDLE_KEY, faceMatchRequest)
            }
    }
}
