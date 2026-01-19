package com.simprints.infra.events.event.domain.models

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.models.TokenKeyType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed class Event {
    abstract val id: String

    abstract val type: EventType
    abstract val payload: EventPayload

    abstract var scopeId: String?
    abstract var projectId: String?

    open fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    abstract fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>): Event

    final override fun equals(other: Any?): Boolean = other is Event && other.id == id

    final override fun hashCode(): Int = id.hashCode()

    @Suppress("UNCHECKED_CAST")
    fun toJson() = JsonHelper.json.encodeToString(
        concreteSerializer as KSerializer<Event>,
        this,
    )

    /**
     * Returns the concrete Kotlinx Serialization serializer for the given [Event] instance.
     *
     * Important background:
     * - Kotlinx Serialization uses a *class discriminator* to support polymorphic and sealed
     *   class serialization.
     * - By default, this discriminator is named `"type"`.
     * - Our domain model already defines a real business field named `type` on [Event].
     * - Using Kotlin’s built-in polymorphic serialization would therefore cause a name
     *   collision between the JSON discriminator and the domain `type` field, or would
     *   require changing the JSON shape (and thus a database migration).
     *
     * Why this method exists:
     * - We intentionally avoid polymorphic serialization to preserve the existing,
     *   persisted JSON schema.
     * - We avoid reflective serializer lookup (e.g. `this::class.starProjectedType`) to
     *   retain compile-time safety and predictable performance.
     *
     * How it works:
     * - The serializer is selected explicitly based on the concrete runtime type of the event.
     * - This acts as a manual, stable dispatch mechanism, using the Kotlin type system rather
     *   than a JSON discriminator.
     *
     * Trade-off:
     * - This approach is more manual than native polymorphic serialization,
     *   but it guarantees zero JSON schema changes and avoids database migrations.
     */

    private val concreteSerializer: KSerializer<out Event>
        get() = when (this) {
            is AlertScreenEvent -> AlertScreenEvent.serializer()
            is AgeGroupSelectionEvent -> AgeGroupSelectionEvent.serializer()
            is AuthenticationEvent -> AuthenticationEvent.serializer()
            is AuthorizationEvent -> AuthorizationEvent.serializer()
            is BiometricReferenceCreationEvent -> BiometricReferenceCreationEvent.serializer()
            is CandidateReadEvent -> CandidateReadEvent.serializer()
            is CompletionCheckEvent -> CompletionCheckEvent.serializer()
            is ConfirmationCallbackEvent -> ConfirmationCallbackEvent.serializer()
            is ConfirmationCalloutEventV2 -> ConfirmationCalloutEventV2.serializer()
            is ConfirmationCalloutEventV3 -> ConfirmationCalloutEventV3.serializer()
            is ConnectivitySnapshotEvent -> ConnectivitySnapshotEvent.serializer()
            is ConsentEvent -> ConsentEvent.serializer()
            is EnrolmentCallbackEvent -> EnrolmentCallbackEvent.serializer()
            is EnrolmentCalloutEventV2 -> EnrolmentCalloutEventV2.serializer()
            is EnrolmentCalloutEventV3 -> EnrolmentCalloutEventV3.serializer()
            is EnrolmentEventV2 -> EnrolmentEventV2.serializer()
            is EnrolmentEventV4 -> EnrolmentEventV4.serializer()
            is EnrolmentLastBiometricsCalloutEventV2 ->
                EnrolmentLastBiometricsCalloutEventV2.serializer()
            is EnrolmentLastBiometricsCalloutEventV3 ->
                EnrolmentLastBiometricsCalloutEventV3.serializer()
            is EnrolmentUpdateEvent -> EnrolmentUpdateEvent.serializer()
            is ErrorCallbackEvent -> ErrorCallbackEvent.serializer()
            is EventDownSyncRequestEvent -> EventDownSyncRequestEvent.serializer()
            is EventUpSyncRequestEvent -> EventUpSyncRequestEvent.serializer()
            is ExternalCredentialCaptureEvent -> ExternalCredentialCaptureEvent.serializer()
            is ExternalCredentialCaptureValueEvent ->
                ExternalCredentialCaptureValueEvent.serializer()
            is ExternalCredentialConfirmationEvent ->
                ExternalCredentialConfirmationEvent.serializer()
            is ExternalCredentialSearchEvent -> ExternalCredentialSearchEvent.serializer()
            is ExternalCredentialSelectionEvent -> ExternalCredentialSelectionEvent.serializer()
            is FaceCaptureBiometricsEvent -> FaceCaptureBiometricsEvent.serializer()
            is FaceCaptureConfirmationEvent -> FaceCaptureConfirmationEvent.serializer()
            is FaceCaptureEvent -> FaceCaptureEvent.serializer()
            is FaceFallbackCaptureEvent -> FaceFallbackCaptureEvent.serializer()
            is FaceOnboardingCompleteEvent -> FaceOnboardingCompleteEvent.serializer()
            is FingerprintCaptureBiometricsEvent ->
                FingerprintCaptureBiometricsEvent.serializer()
            is FingerprintCaptureEvent -> FingerprintCaptureEvent.serializer()
            is GuidSelectionEvent -> GuidSelectionEvent.serializer()
            is IdentificationCallbackEvent -> IdentificationCallbackEvent.serializer()
            is IdentificationCalloutEventV2 -> IdentificationCalloutEventV2.serializer()
            is IdentificationCalloutEventV3 -> IdentificationCalloutEventV3.serializer()
            is IntentParsingEvent -> IntentParsingEvent.serializer()
            is InvalidIntentEvent -> InvalidIntentEvent.serializer()
            is LicenseCheckEvent -> LicenseCheckEvent.serializer()
            is OneToManyMatchEvent -> OneToManyMatchEvent.serializer()
            is OneToOneMatchEvent -> OneToOneMatchEvent.serializer()
            is PersonCreationEvent -> PersonCreationEvent.serializer()
            is RefusalCallbackEvent -> RefusalCallbackEvent.serializer()
            is RefusalEvent -> RefusalEvent.serializer()
            is SampleUpSyncRequestEvent -> SampleUpSyncRequestEvent.serializer()
            is ScannerConnectionEvent -> ScannerConnectionEvent.serializer()
            is ScannerFirmwareUpdateEvent -> ScannerFirmwareUpdateEvent.serializer()
            is SuspiciousIntentEvent -> SuspiciousIntentEvent.serializer()
            is VerificationCallbackEvent -> VerificationCallbackEvent.serializer()
            is VerificationCalloutEventV2 -> VerificationCalloutEventV2.serializer()
            is VerificationCalloutEventV3 -> VerificationCalloutEventV3.serializer()
            is Vero2InfoSnapshotEvent -> Vero2InfoSnapshotEvent.serializer()
        }
}
