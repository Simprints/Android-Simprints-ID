package com.simprints.feature.datagenerator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simprints.feature.datagenerator.databinding.ActivityDataGeneratorBinding
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DataGeneratorActivity : AppCompatActivity() {
    private val viewModel: DataGeneratorViewModel by viewModels()
    private lateinit var binding: ActivityDataGeneratorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!BuildConfig.DEBUG_MODE) {
            Simber.i("DataGenerator is only for debug builds.")
            throw IllegalStateException("DataGenerator is only for debug builds.")
        }
        setContentView(R.layout.activity_data_generator)
        binding = ActivityDataGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.statusMessage.observe(this) {
            binding.statusText.text = it
        }

        lifecycleScope.launch {
            try {
                viewModel.handleIntent(intent)
                // add a delay to let the user see the status message
                delay(SMALL_DELAY_MS)
                // Set result to indicate success
                setResult(RESULT_OK)
            } catch (e: Exception) {
                Simber.e("Error handling intent: ${e.message}", e)
                // Set result to indicate failure
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }

    companion object {
        private const val SMALL_DELAY_MS = 1000L // 1 second
    }
}
