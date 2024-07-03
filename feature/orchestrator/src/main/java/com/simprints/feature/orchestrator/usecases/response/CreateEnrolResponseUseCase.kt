package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.tools.extentions.toBytes
import com.simprints.core.tools.extentions.toFloats
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.infra.protection.auxiliary.AuxDataRepository
import com.simprints.infra.protection.polyprotect.TemplateEncoder
import java.io.Serializable
import java.util.UUID
import javax.inject.Inject

internal class CreateEnrolResponseUseCase @Inject constructor(
    private val subjectFactory: SubjectFactory,
    private val enrolSubject: EnrolSubjectUseCase,
    private val auxDataRepository: AuxDataRepository,
    private val templateEncoder: TemplateEncoder,
) {

    suspend operator fun invoke(request: ActionRequest.EnrolActionRequest, results: List<Serializable>): AppResponse {
        val fingerprintCapture = results.filterIsInstance(FingerprintCaptureResult::class.java).lastOrNull()
        val faceCapture = results.filterIsInstance(FaceCaptureResult::class.java).lastOrNull()

        return try {
            val subjectId = UUID.randomUUID().toString()
            val auxData = auxDataRepository.createAuxData(subjectId)

            val subject = subjectFactory.buildSubjectFromCaptureResults(
                subjectId = subjectId,
                projectId = request.projectId,
                attendantId = request.userId,
                moduleId = request.moduleId,
                fingerprintResponse = fingerprintCapture,
                faceResponse = faceCapture?.let { capture ->
                    // Deep copy to replace the templates in samples
                    capture.copy(
                        results = capture.results.map { result ->
                            result.copy(sample = result.sample?.let { sample ->
                                sample.copy(
                                    template = templateEncoder.encodeTemplate(
                                        template = sample.template.toFloats(),
                                        auxData = auxData,
                                    ).toBytes()
                                )
                            })
                        }
                    )
                }
            )
            enrolSubject(subject)

            // TODO Storing aux data for this subject - PoC
            auxDataRepository.saveAuxData(auxData)

            AppEnrolResponse(subject.subjectId)
        } catch (e: Exception) {
            Simber.e(e)
            AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)
        }
    }
}
