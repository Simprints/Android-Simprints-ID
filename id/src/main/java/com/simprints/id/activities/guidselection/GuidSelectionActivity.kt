package com.simprints.id.activities.guidselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.id.guidselection.GuidSelectionWorker
import com.simprints.id.tools.extensions.parseAppConfirmation

class GuidSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleGuidSelection()
        sendOkResult()
    }

    private fun scheduleGuidSelection() {
        val guidSelectionWork = buildGuidSelectionWork()
        WorkManager.getInstance().enqueue(guidSelectionWork)
    }

    private fun sendOkResult() {
        val data = Intent().putExtra(IDENTIFICATION_OUTCOME_KEY, true)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun buildGuidSelectionWork() = OneTimeWorkRequestBuilder<GuidSelectionWorker>()
        .setInputData(prepareInputData())
        .build()

    private fun prepareInputData() = intent.parseAppConfirmation().toMap().let {
        Data.Builder().putAll(it).build()
    }

    companion object {
        private const val IDENTIFICATION_OUTCOME_KEY = "identification_outcome"
    }

}
