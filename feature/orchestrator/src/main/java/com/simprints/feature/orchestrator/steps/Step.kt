package com.simprints.feature.orchestrator.steps

import androidx.annotation.IdRes
import androidx.annotation.Keep
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import com.simprints.face.capture.FaceCaptureParams
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.consent.ConsentParams
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.fetchsubject.FetchSubjectParams
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.LoginResult
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.selectsubject.SelectSubjectParams
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.setup.SetupResult
import com.simprints.feature.validatepool.ValidateSubjectPoolFragmentParams
import com.simprints.feature.validatepool.ValidateSubjectPoolResult
import com.simprints.fingerprint.capture.FingerprintCaptureParams
import com.simprints.fingerprint.connect.FingerprintConnectParams
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.matching.MatchResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Step contains all of the information required to execute an orchestration step and the result of the execution.
 */
@Keep
@Serializable
data class Step(
    val id: Int,
    @IdRes val navigationActionId: Int,
    @IdRes val destinationId: Int,
    var params: StepParams? = null,
    var status: StepStatus = StepStatus.NOT_STARTED,
    var result: StepResult? = null,
)

@Keep
@Serializable
enum class StepStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}

val orchestratorSerializersModule = SerializersModule {

    // Register all StepResult subclasses
    polymorphic(StepResult::class) {
        subclass(LoginResult::class)
        subclass(SetupResult::class)
        subclass(ConsentResult::class)
        subclass(FingerprintConnectResult::class)
        subclass(MatchResult::class)
        subclass(EnrolLastBiometricResult::class)
        subclass(FetchSubjectResult::class)
        subclass(SelectSubjectResult::class)
        subclass(AlertResult::class)
        subclass(ExitFormResult::class)
        subclass(ValidateSubjectPoolResult::class)
        subclass(SelectSubjectAgeGroupResult::class)
        subclass(ExternalCredentialSearchResult::class)
        subclass(CredentialMatch::class)
        subclass(BiometricTemplateCapture::class)
        subclass(BiometricReferenceCapture::class)
        subclass(ComparisonResult::class)
    }

    // Register all StepParams subclasses
    polymorphic(StepParams::class) {
        subclass(LoginParams::class)
        subclass(ConsentParams::class)
        subclass(FetchSubjectParams::class)
        subclass(SelectSubjectParams::class)
        subclass(ValidateSubjectPoolFragmentParams::class)
        subclass(FaceCaptureParams::class)
        subclass(FingerprintConnectParams::class)
        subclass(FingerprintCaptureParams::class)
        subclass(MatchStepStubPayload::class)
        subclass(MatchParams::class)
        subclass(EnrolLastBiometricParams::class)
        subclass(EnrolLastBiometricStepResult.EnrolLastBiometricsResult::class)
        subclass(EnrolLastBiometricStepResult.MatchResult::class)
        subclass(EnrolLastBiometricStepResult.CaptureResult::class)
        subclass(ExternalCredentialParams::class)
        subclass(BiometricReferenceCapture::class)
        subclass(BiometricTemplateCapture::class)
        subclass(ComparisonResult::class)
        subclass(BiometricDataSource.CommCare::class)
        subclass(BiometricDataSource.Simprints::class)
        subclass(EnrolmentRecordQuery::class)
        subclass(AgeGroup::class)
    }
}
