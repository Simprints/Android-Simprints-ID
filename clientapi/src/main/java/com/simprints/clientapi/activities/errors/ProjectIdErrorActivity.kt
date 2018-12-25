package com.simprints.clientapi.activities.errors

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.simprints.clientapi.R

import kotlinx.android.synthetic.main.activity_project_id_error.*

class ProjectIdErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_id_error)
    }

}
