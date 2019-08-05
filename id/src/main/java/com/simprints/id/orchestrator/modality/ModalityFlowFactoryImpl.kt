package com.simprints.id.orchestrator.modality

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.modality.flows.FaceModalityFlow
import com.simprints.id.orchestrator.modality.flows.FingerprintModalityFlow
import com.simprints.id.orchestrator.modality.flows.MultiModalitiesFlowBase
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

class ModalityFlowFactoryImpl(private val prefs: PreferencesManager,
                              private val packageName: String) : ModalityFlowFactory {


    override fun buildModalityFlow(appRequest: AppRequest,
                                   modality: Modality): ModalityFlow =
        when (modality) {
            FACE -> buildModalityFlow(listOf(
                buildFaceModality(appRequest, packageName)))

            FINGER -> buildModalityFlow(listOf(
                buildFingerprintModality(appRequest, packageName)))

            FINGER_FACE -> buildModalityFlow(listOf(
                buildFingerprintModality(appRequest, packageName),
                buildFaceModality(appRequest, packageName)))

            FACE_FINGER -> buildModalityFlow(listOf(
                buildFaceModality(appRequest, packageName),
                buildFingerprintModality(appRequest, packageName)))
        }

    private fun buildModalityFlow(steps: List<ModalityFlow>): ModalityFlow =
        MultiModalitiesFlowBase(steps)

    internal fun buildFingerprintModality(appRequest: AppRequest, packageName: String) =
        FingerprintModalityFlow(appRequest, packageName, prefs)

    internal fun buildFaceModality(appRequest: AppRequest, packageName: String) =
        FaceModalityFlow(appRequest, packageName)
}
