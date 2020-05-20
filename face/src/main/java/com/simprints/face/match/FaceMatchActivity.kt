package com.simprints.face.match

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.simprints.face.R
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceRequest
import kotlinx.android.synthetic.main.activity_face_match.*
import org.koin.android.viewmodel.ext.android.viewModel

class FaceMatchActivity : AppCompatActivity() {
    private val vm: FaceMatchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_match)
    }
}
