package com.simprints.fingerprint.orchestrator.models

import android.content.Intent
import com.simprints.fingerprint.orchestrator.domain.ResultCode

/**
 * This class represents the result of a completed fingerprint flow
 *
 * @property resultCode  the integer value of the final [ResultCode] of the fingerprint's flow processed
 * @property resultData  the resulting returned data, wrapped in an intent
 */
data class FinalResult(val resultCode: Int, val resultData: Intent?)
