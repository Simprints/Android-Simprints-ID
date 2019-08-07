package com.simprints.id.orchestrator.modality

import com.simprints.id.domain.modality.Modality.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.ModalityFlowFactoryImpl
import com.simprints.testtools.common.syntax.*
import org.junit.Test

class ModalityFlowBuilderImplTest {

    companion object {
        const val packageName = "com.simprints.id"
    }

    @Test
    fun givenFaceModality_buildAFlowModality_shouldContainAFingerprintModality() {
        val app = mock<AppRequest>()
        val modalityFlowFactorySpy = spy(ModalityFlowFactoryImpl(mock(), packageName))

        modalityFlowFactorySpy.startModalityFlow(app, FACE)

        verifyOnce(modalityFlowFactorySpy) { buildFaceModality(anyNotNull(), anyNotNull()) }
        verifyNever(modalityFlowFactorySpy) { buildFingerprintModality(anyNotNull(), anyNotNull()) }
    }


    @Test
    fun givenFingerprintModality_buildAFlowModality_shouldContainAFingerprintModality() {
        val app = mock<AppRequest>()
        val modalityFlowFactorySpy = spy(ModalityFlowFactoryImpl(mock(), packageName))

         modalityFlowFactorySpy.startModalityFlow(app, FINGER)

        verifyOnce(modalityFlowFactorySpy) { buildFingerprintModality(anyNotNull(), anyNotNull()) }
        verifyNever(modalityFlowFactorySpy) { buildFaceModality(anyNotNull(), anyNotNull()) }
    }

    @Test
    fun givenFaceFingerprintModality_buildAFlowModality_shouldContainAFaceAndFingerprintModals() {
        val app = mock<AppRequest>()
        val modalityFlowFactorySpy = spy(ModalityFlowFactoryImpl(mock(), packageName))

         modalityFlowFactorySpy.startModalityFlow(app, FACE_FINGER)

        verifyOnce(modalityFlowFactorySpy) { buildFaceModality(anyNotNull(), anyNotNull()) }
        verifyOnce(modalityFlowFactorySpy) { buildFingerprintModality(anyNotNull(), anyNotNull()) }
    }

    @Test
    fun givenFingerprintFaceModality_buildAFlowModality_shouldContainAFingerprintAndFaceModals() {
        val app = mock<AppRequest>()
        val modalityFlowFactorySpy = spy(ModalityFlowFactoryImpl(mock(), packageName))

         modalityFlowFactorySpy.startModalityFlow(app, FINGER_FACE)

        verifyOnce(modalityFlowFactorySpy) { buildFaceModality(anyNotNull(), anyNotNull()) }
        verifyOnce(modalityFlowFactorySpy) { buildFingerprintModality(anyNotNull(), anyNotNull()) }
    }

}
