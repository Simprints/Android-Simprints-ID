package com.simprints.face.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.simprints.face.R
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
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                nSeconds++
                handler.post {
                    count.text = "$nSeconds seconds"
                }
            }
        }, 0, 1000)
    }

}
