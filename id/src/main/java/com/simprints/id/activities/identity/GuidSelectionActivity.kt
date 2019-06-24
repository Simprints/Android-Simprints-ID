package com.simprints.id.activities.identity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GuidSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Toast.makeText(this, "Alan was here", Toast.LENGTH_SHORT).show()
        routeService(intent)
        finish()
    }

    private fun Activity.routeService(intent: Intent) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            this.startForegroundService(intent)
        else
            this.startService(intent)

}
