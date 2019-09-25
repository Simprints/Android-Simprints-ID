package com.simprints.id.exitformhandler

import android.content.Intent
import com.simprints.id.activities.coreexitform.result.CoreExitFormActivityResult
import com.simprints.id.activities.faceexitform.result.FaceExitFormActivityResult
import com.simprints.id.activities.fingerprintexitform.result.FingerprintExitFormActivityResult
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFaceExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.exitformhandler.ExitFormResult.Companion.EXIT_FORM_BUNDLE_KEY
import com.simprints.id.exitformhandler.ExitFormResult.ExitFormType.*

class ExitFormHandlerImpl : ExitFormHandler {

    override fun buildExitFormResponseForCore(data: Intent?): CoreResponse? =
        data?.getParcelableExtra<ExitFormResult>(EXIT_FORM_BUNDLE_KEY)?.let {
            when (it.type) {
                CORE_EXIT_FORM -> {
                    buildCoreExitFormResponseFromCoreExitForm(data.getParcelableExtra(EXIT_FORM_BUNDLE_KEY))
                }
                CORE_FINGERPRINT_EXIT_FROM -> {
                    buildCoreExitFormResponseFromCoreFingerprintExitForm(data.getParcelableExtra(EXIT_FORM_BUNDLE_KEY))
                }
                CORE_FACE_EXIT_FORM -> {
                    buildCoreExitFormResponseFromCoreFaceExitForm(data.getParcelableExtra(EXIT_FORM_BUNDLE_KEY))
                }
            }
        }

    private fun buildCoreExitFormResponseFromCoreExitForm(result: CoreExitFormActivityResult) =
        if (result.action == CoreExitFormActivityResult.Action.SUBMIT) {
            CoreExitFormResponse(result.answer.reason, result.answer.optionalText)
        } else {
            null
        }

    private fun buildCoreExitFormResponseFromCoreFingerprintExitForm(result: FingerprintExitFormActivityResult) =
        if (result.action == FingerprintExitFormActivityResult.Action.SUBMIT) {
            CoreFingerprintExitFormResponse(result.answer.reason, result.answer.optionalText)
        } else {
            null
        }

    private fun buildCoreExitFormResponseFromCoreFaceExitForm(result: FaceExitFormActivityResult) =
        if (result.action == FaceExitFormActivityResult.Action.SUBMIT) {
            CoreFaceExitFormResponse(result.answer.reason, result.answer.optionalText)
        } else {
            null
        }
}
