package com.simprints.feature.orchestrator.steps

import androidx.annotation.IdRes
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.ModalityCaptureStepParams
import com.simprints.core.domain.step.ModalityCaptureStepResultItem
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.step.StepResult
import com.simprints.face.capture.FaceCaptureParams
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.consent.ConsentParams
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.exitform.ExitFormResult
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
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.fingerprint.connect.FingerprintConnectParams
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResult
import com.simprints.matcher.MatchResultItem
import java.io.Serializable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "resultType")
@JsonSubTypes(
    JsonSubTypes.Type(value = LoginResult::class, name = "LoginResult"),
    JsonSubTypes.Type(value = SetupResult::class, name = "SetupResult"),
    JsonSubTypes.Type(value = ConsentResult::class, name = "ConsentResult"),
    JsonSubTypes.Type(value = FingerprintConnectResult::class, name = "FingerprintConnectResult"),
    JsonSubTypes.Type(value = FingerprintCaptureResult::class, name = "FingerprintCaptureResult"),
    JsonSubTypes.Type(value = FaceCaptureResult::class, name = "FaceCaptureResult"),
    JsonSubTypes.Type(value = FaceCaptureResult.Item::class, name = "FaceCaptureResult.Item"),
    JsonSubTypes.Type(value = MatchResult::class, name = "MatchResult"),
    JsonSubTypes.Type(value = MatchResultItem::class, name = "MatchResultItem"),
    JsonSubTypes.Type(value = EnrolLastBiometricResult::class, name = "EnrolLastBiometricResult"),
    JsonSubTypes.Type(value = FetchSubjectResult::class, name = "FetchSubjectResult"),
    JsonSubTypes.Type(value = SelectSubjectResult::class, name = "SelectSubjectResult"),
    JsonSubTypes.Type(value = AlertResult::class, name = "AlertResult"),
    JsonSubTypes.Type(value = ExitFormResult::class, name = "ExitFormResult"),
    JsonSubTypes.Type(value = ValidateSubjectPoolResult::class, name = "ValidateSubjectPoolResult"),
    JsonSubTypes.Type(value = SelectSubjectAgeGroupResult::class, name = "SelectSubjectAgeGroupResult"),
    // Common data classes used in multiple step results
    JsonSubTypes.Type(value = ModalityCaptureStepResultItem::class, name = "ModalityCaptureStepResultItem"),
    JsonSubTypes.Type(value = CaptureSample::class, name = "CaptureSample"),
    JsonSubTypes.Type(value = SecuredImageRef::class, name = "SecuredImageRef"),
)
abstract class StepResultMixin : StepResult

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "paramsType")
@JsonSubTypes(
    JsonSubTypes.Type(value = LoginParams::class, name = "LoginParams"),
    JsonSubTypes.Type(value = ConsentParams::class, name = "ConsentParams"),
    JsonSubTypes.Type(value = FetchSubjectParams::class, name = "FetchSubjectParams"),
    JsonSubTypes.Type(value = SelectSubjectParams::class, name = "SelectSubjectParams"),
    JsonSubTypes.Type(value = ValidateSubjectPoolFragmentParams::class, name = "ValidateSubjectPoolFragmentParams"),
    JsonSubTypes.Type(value = FaceCaptureParams::class, name = "FaceCaptureParams"),
    JsonSubTypes.Type(value = FingerprintConnectParams::class, name = "FingerprintConnectParams"),
    JsonSubTypes.Type(value = FingerprintCaptureParams::class, name = "FingerprintCaptureParams"),
    // Match params are updated after capture steps
    JsonSubTypes.Type(value = MatchStepStubPayload::class, name = "MatchStepStubPayload"),
    JsonSubTypes.Type(value = MatchParams::class, name = "MatchParams"),
    // Below are subclasses of enrol-last step that takes results of other steps as parameters
    JsonSubTypes.Type(value = EnrolLastBiometricParams::class, name = "EnrolLastBiometricParams"),
    JsonSubTypes.Type(value = EnrolLastBiometricStepResult::class, name = "EnrolLastBiometricStepResult"),
    JsonSubTypes.Type(
        value = EnrolLastBiometricStepResult.EnrolLastBiometricsResult::class,
        name = "EnrolLastBiometricStepResult.EnrolLastBiometricsResult",
    ),
    JsonSubTypes.Type(
        value = EnrolLastBiometricStepResult.MatchResult::class,
        name = "EnrolLastBiometricStepResult.MatchResult",
    ),
    JsonSubTypes.Type(
        value = EnrolLastBiometricStepResult.MatchResult.Item::class,
        name = "EnrolLastBiometricStepResult.MatchResult.Item",
    ),
    JsonSubTypes.Type(
        value = EnrolLastBiometricStepResult.CaptureResult::class,
        name = "EnrolLastBiometricStepResult.CaptureResult",
    ),
    JsonSubTypes.Type(value = MatchResult::class, name = "MatchResult"),
    JsonSubTypes.Type(value = MatchResultItem::class, name = "MatchResultItem"),
    // Additional types that are used in top-level params
    JsonSubTypes.Type(value = CaptureSample::class, name = "CaptureSample"),
    JsonSubTypes.Type(value = BiometricDataSource::class, name = "BiometricDataSource"),
    JsonSubTypes.Type(value = SubjectQuery::class, name = "SubjectQuery"),
)
abstract class StepParamsMixin : StepParams

@Keep
internal data class Step(
    val id: Int,
    @IdRes val navigationActionId: Int,
    @IdRes val destinationId: Int,
    var params: StepParams? = null,
    var status: StepStatus = StepStatus.NOT_STARTED,
    var result: StepResult? = null,
) : Serializable {
    // Do not remove.
    // Even though it may be marked as unused by IDE, it is referenced in the JsonTypeInfo annotation
    @Suppress("unused")
    val paramsType: String
        get() = this::class.java.simpleName

    // Do not remove.
    // Even though it may be marked as unused by IDE, it is referenced in the JsonTypeInfo annotation
    @Suppress("unused")
    val resultType: String
        get() = this::class.java.simpleName
}

@Keep
enum class StepStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}
