package com.simprints.id.domain.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.orchestrator.steps.Step.Request

abstract class FingerprintRequest: Parcelable, Request {

    override fun toJson(): String = JsonHelper.toJson(this)

}
