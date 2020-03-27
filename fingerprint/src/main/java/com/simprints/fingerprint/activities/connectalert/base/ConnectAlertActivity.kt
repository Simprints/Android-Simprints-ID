package com.simprints.fingerprint.activities.connectalert.base

import android.content.Intent
import android.os.Bundle
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode

abstract class ConnectAlertActivity : FingerprintActivity() {

    override fun onBackPressed() {
        startRefusalActivity()
    }

    private fun startRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java),
            RequestCode.REFUSAL.value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> {
                }
            }
        }
    }

    private fun setResultAndFinish(resultCode: ResultCode, data: Intent?) {
        setResult(resultCode.value, data)
        finish()
    }
}
