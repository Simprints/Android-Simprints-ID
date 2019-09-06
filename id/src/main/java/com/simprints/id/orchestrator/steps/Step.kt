package com.simprints.id.orchestrator.steps

import android.os.Parcelable
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.requests.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.DomainToModuleApiFingerprintRequest.fromDomainToModuleApiFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Step(val requestCode: Int,
                val activityName: String,
                val bundleKey: String,
                val request: Request,
                var status: Status) : Parcelable {

    var result: Result? = null
        set(value) {
            field = value
            if (field != null) {
                status = COMPLETED
            }
        }

    enum class Status {
        NOT_STARTED, ONGOING, COMPLETED
    }

    interface Request : Parcelable {
        fun toJson(): String
    }

    interface Result : Parcelable {
        fun toJson(): String
    }

    class JsonAdapter : TypeAdapter<Step>() {
        override fun read(input: JsonReader): Step {
            val step: Step

            with(input) {
                var requestCode = -1
                var activityName = ""
                var bundleKey = ""
                var request: Request? = null
                var response: Result? = null
                var status: Status = Status.NOT_STARTED

                beginObject()

                while (hasNext()) {
                    when (nextName()) {
                        FIELD_REQUEST_CODE -> requestCode = nextInt()
                        FIELD_ACTIVITY_NAME -> activityName = nextString()
                        FIELD_BUNDLE_KEY -> bundleKey = nextString()
                        FIELD_REQUEST -> request = parseRequest()
                        FIELD_RESULT -> response = parseResult()
                        FIELD_STATUS -> status = Status.valueOf(nextString())
                    }
                }

                step = Step(requestCode, activityName, bundleKey, request!!, status).also {
                    it.result = response
                }

                endObject()
            }

            return step
        }

        override fun write(output: JsonWriter, value: Step) {
            with(output) {
                beginObject()
                name(FIELD_REQUEST_CODE).value(value.requestCode)
                name(FIELD_ACTIVITY_NAME).value(value.activityName)
                name(FIELD_BUNDLE_KEY).value(value.bundleKey)
                name(FIELD_REQUEST).value(value.request.toJson())
                value.result?.let { name(FIELD_RESULT).value(it.toJson()) }
                name(FIELD_STATUS).value(value.status.name)
                endObject()
            }
        }

        private fun JsonReader.parseRequest(): Request {
            val faceCapture = FaceCaptureRequest.tryParse(this)
            val fingerprintEnrol = FingerprintEnrolRequest.tryParse(this)

            val possibleRequests = listOf(faceCapture, fingerprintEnrol)

            return possibleRequests.first {
                it != null
            } ?: throw JsonParseException("Could not parse JSON into request")
        }

        private fun JsonReader.parseResult(): Result? {
            TODO()
        }

        private companion object {
            const val FIELD_REQUEST_CODE = "requestCode"
            const val FIELD_ACTIVITY_NAME = "activityName"
            const val FIELD_BUNDLE_KEY = "bundleKey"
            const val FIELD_REQUEST = "request"
            const val FIELD_RESULT = "result"
            const val FIELD_STATUS = "status"
        }
    }
}

fun Step.Request.fromDomainToModuleApi(): Parcelable =
    when (this) {
        is FingerprintRequest -> fromDomainToModuleApiFingerprintRequest(this)
        is FaceRequest -> fromDomainToModuleApi()
        else -> throw Throwable("Invalid Request")
    }
