package com.simprints.fingerprint.connect.screens

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.simprints.fingerprint.connect.FingerprintConnectContract
import com.simprints.fingerprint.connect.FingerprintConnectParams
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.fingerprint.connect.screens.controller.ConnectScannerControllerFragmentArgs
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

// TODO remove and use only as include into the capture flow
@ExcludedFromGeneratedTestCoverageReports("UI code")
class ShowConnectWrapper : ActivityResultContract<Boolean, Parcelable>() {
    override fun createIntent(context: Context, input: Boolean): Intent =
        Intent(context, ConnectScannerWrapperActivity::class.java).putExtra(
            ConnectScannerWrapperActivity.SCANNER_CONNECT_ARGS_EXTRA,
            FingerprintConnectContract.getArgs(input)
        )

    override fun parseResult(resultCode: Int, intent: Intent?): Parcelable = intent
        ?.getParcelableExtra(FingerprintConnectContract.RESULT)
        ?: FingerprintConnectResult(false)
}
