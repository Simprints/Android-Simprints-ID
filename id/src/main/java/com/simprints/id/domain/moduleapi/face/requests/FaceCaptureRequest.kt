package com.simprints.id.domain.moduleapi.face.requests

import com.google.gson.stream.JsonReader
import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceRequestType
import kotlinx.android.parcel.Parcelize
import com.simprints.moduleapi.face.requests.IFaceRequestType.CAPTURE as ModuleApiCaptureType

@Parcelize
data class FaceCaptureRequest(val nFaceSamplesToCapture: Int,
                              override val type: FaceRequestType = CAPTURE) : FaceRequest() {

    companion object {
        private const val FIELD_N_FACE_SAMPLES_TO_CAPTURE = "nFaceSamplesToCapture"
        private const val FIELD_TYPE = "type"

        fun tryParse(jsonReader: JsonReader): FaceCaptureRequest? {
            var request: FaceCaptureRequest? = null
            var nFaceSamplesToCapture = -1
            var type = VERIFY

            with(jsonReader) {
                beginObject()

                while (hasNext()) {
                    val elementName = nextName()
                    if (FaceCaptureRequest::class.java.fields.none { it.name == elementName }) {
                        break
                    } else {
                        when (elementName) {
                            FIELD_N_FACE_SAMPLES_TO_CAPTURE -> nFaceSamplesToCapture = nextInt()
                            FIELD_TYPE -> type = valueOf(nextString())
                        }

                        val fieldsAreValid = nFaceSamplesToCapture != -1 && type == CAPTURE

                        if (fieldsAreValid) {
                            request = FaceCaptureRequest(nFaceSamplesToCapture, type)
                            break
                        }
                    }
                }

                endObject()
            }

            return request
        }
    }

}

fun FaceCaptureRequest.fromDomainToModuleApi(): IFaceCaptureRequest =
    IFaceCaptureRequestImpl(nFaceSamplesToCapture)

@Parcelize
private data class IFaceCaptureRequestImpl(override val nFaceSamplesToCapture: Int,
                                           override val type: IFaceRequestType = ModuleApiCaptureType) : IFaceCaptureRequest
