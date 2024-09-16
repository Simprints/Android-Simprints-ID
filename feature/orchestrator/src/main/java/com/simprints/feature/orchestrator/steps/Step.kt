package com.simprints.feature.orchestrator.steps

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.login.LoginResult
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.setup.SetupResult
import com.simprints.feature.validatepool.ValidateSubjectPoolResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import java.io.Serializable


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = LoginResult::class, name = "LoginResult"),
    JsonSubTypes.Type(value = SetupResult::class, name = "SetupResult"),
    JsonSubTypes.Type(value = ConsentResult::class, name = "ConsentResult"),
    JsonSubTypes.Type(value = FingerprintConnectResult::class, name = "FingerprintConnectResult"),
    JsonSubTypes.Type(value = FingerprintCaptureResult::class, name = "FingerprintCaptureResult"),
    JsonSubTypes.Type(
        value = FingerprintCaptureResult.Item::class,
        name = "FingerprintCaptureResult.Item"
    ),
    JsonSubTypes.Type(
        value = FingerprintCaptureResult.Sample::class,
        name = "FingerprintCaptureResult.Sample"
    ),

    JsonSubTypes.Type(value = FingerprintMatchResult::class, name = "FingerprintMatchResult"),
    JsonSubTypes.Type(
        value = FingerprintMatchResult.Item::class,
        name = "FingerprintMatchResult.Item"
    ),

    JsonSubTypes.Type(value = FaceCaptureResult::class, name = "FaceCaptureResult"),
    JsonSubTypes.Type(value = FaceCaptureResult.Item::class, name = "FaceCaptureResult.Item"),
    JsonSubTypes.Type(value = FaceCaptureResult.Sample::class, name = "FaceCaptureResult.Sample"),

    JsonSubTypes.Type(value = FaceMatchResult::class, name = "FaceMatchResult"),
    JsonSubTypes.Type(value = FaceMatchResult.Item::class, name = "FaceMatchResult.Item"),
    JsonSubTypes.Type(value = EnrolLastBiometricResult::class, name = "EnrolLastBiometricResult"),
    JsonSubTypes.Type(value = FetchSubjectResult::class, name = "FetchSubjectResult"),
    JsonSubTypes.Type(value = SelectSubjectResult::class, name = "SelectSubjectResult"),
    JsonSubTypes.Type(value = AlertResult::class, name = "AlertResult"),
    JsonSubTypes.Type(value = ExitFormResult::class, name = "ExitFormResult"),
    JsonSubTypes.Type(value = ValidateSubjectPoolResult::class, name = "ValidateSubjectPoolResult"),
    JsonSubTypes.Type(value = SelectSubjectAgeGroupResult::class, name = "SelectSubjectAgeGroupResult"),
)
abstract class SerializableMixin : Serializable

@Keep
internal data class Step(
    val id: Int,
    @IdRes val navigationActionId: Int,
    @IdRes val destinationId: Int,
    var payload: Bundle,
    var status: StepStatus = StepStatus.NOT_STARTED,
    var result: Serializable? = null,
) : Serializable {

    // Do not remove.
    // Even though it may be marked as unused by IDE, it is referenced in the JsonTypeInfo annotation
    @Suppress("unused")
    val type: String
        get() = this::class.java.simpleName
}

@Keep
enum class StepStatus {

    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}
