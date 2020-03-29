package com.simprints.fingerprint.activities.connect.issues.scanneroff

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import kotlinx.android.synthetic.main.fragment_scanner_off.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ScannerOffFragment : Fragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_scanner_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tryAgainButton.setOnClickListener {
            replaceTryAgainButtonWithProgressBar()
        }

        connectScannerViewModel.retryConnect()
    }

    override fun onResume() {
        super.onResume()
        connectScannerViewModel.scannerConnected.observe(this, Observer { success: Boolean? ->
            when (success) {
                true -> handleScannerConnected()
                false -> connectScannerViewModel.retryConnect()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        connectScannerViewModel.scannerConnected.removeObservers(this)
    }

    // The tryAgainButton doesn't actually do anything - we're already retrying in the background
    // Show a progress bar to make it known that something is happening
    private fun replaceTryAgainButtonWithProgressBar() {
        turnOnScannerProgressBar.visibility = View.VISIBLE
        tryAgainButton.visibility = View.INVISIBLE
        tryAgainButton.isEnabled = false
    }

    private fun handleScannerConnected() {
        turnOnScannerProgressBar.visibility = View.INVISIBLE
        tryAgainButton.visibility = View.VISIBLE
        tryAgainButton.isEnabled = false
        tryAgainButton.setText(R.string.scanner_on)
        tryAgainButton.setBackgroundColor(resources.getColor(R.color.simprints_green, null))
        Handler().postDelayed({ finishConnectActivity() }, FINISHED_TIME_DELAY_MS)
    }

    private fun finishConnectActivity() {
        connectScannerViewModel.finish.postValue(Unit)
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
