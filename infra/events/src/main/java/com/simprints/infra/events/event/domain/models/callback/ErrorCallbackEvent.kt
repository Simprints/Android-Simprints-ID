package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ERROR
import java.util.UUID

@Keep
data class ErrorCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ErrorCallbackPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        reason: ErrorCallbackPayload.Reason,
    ) : this(
        UUID.randomUUID().toString(),
        ErrorCallbackPayload(createdAt, EVENT_VERSION, reason),
        CALLBACK_ERROR,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class ErrorCallbackPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val reason: Reason,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLBACK_ERROR,
    ) : EventPayload() {
        override fun toSafeString(): String = "reason: $reason"

        @Keep
        enum class Reason {
            DIFFERENT_PROJECT_ID_SIGNED_IN,
            DIFFERENT_USER_ID_SIGNED_IN,
            GUID_NOT_FOUND_ONLINE,
            GUID_NOT_FOUND_OFFLINE,
            UNEXPECTED_ERROR,
            BLUETOOTH_NOT_SUPPORTED,
            LOGIN_NOT_COMPLETE,
            ENROLMENT_LAST_BIOMETRICS_FAILED,
            LICENSE_MISSING,
            LICENSE_INVALID,
            FINGERPRINT_CONFIGURATION_ERROR,
            BACKEND_MAINTENANCE_ERROR,
            PROJECT_ENDING,
            PROJECT_PAUSED,
            BLUETOOTH_NO_PERMISSION,
            FACE_CONFIGURATION_ERROR,
            AGE_GROUP_NOT_SUPPORTED,
            ;

            companion object {
                fun fromAppResponseErrorReasonToEventReason(appResponseErrorReason: AppErrorReason) = when (appResponseErrorReason) {
                    AppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    AppErrorReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                    AppErrorReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                    AppErrorReason.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
                    AppErrorReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    AppErrorReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                    AppErrorReason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
                    AppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
                    AppErrorReason.LICENSE_MISSING -> LICENSE_MISSING
                    AppErrorReason.LICENSE_INVALID -> LICENSE_INVALID
                    AppErrorReason.FINGERPRINT_CONFIGURATION_ERROR -> FINGERPRINT_CONFIGURATION_ERROR
                    AppErrorReason.FACE_CONFIGURATION_ERROR -> FACE_CONFIGURATION_ERROR
                    AppErrorReason.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
                    AppErrorReason.PROJECT_PAUSED -> PROJECT_PAUSED
                    AppErrorReason.ROOTED_DEVICE -> throw Throwable("Can't convert from rooted device")
                    AppErrorReason.PROJECT_ENDING -> PROJECT_ENDING
                    AppErrorReason.BLUETOOTH_NO_PERMISSION -> BLUETOOTH_NO_PERMISSION
                    AppErrorReason.AGE_GROUP_NOT_SUPPORTED -> AGE_GROUP_NOT_SUPPORTED
                }
            }
        }
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
