package com.simprints.id.domain.moduleapi.face.requests

import com.google.gson.stream.JsonReader
import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.moduleapi.face.requests.IFaceRequestType
import com.simprints.moduleapi.face.requests.IFaceVerifyRequest
import kotlinx.android.parcel.Parcelize
import com.simprints.moduleapi.face.requests.IFaceRequestType.VERIFY as ModuleApiVerifyRequestType
@Parcelize
data class FaceVerifyRequest(val projectId: String,
                             val userId: String,
                             val moduleId: String,
                             override val type: FaceRequestType = VERIFY) : FaceRequest() {

    companion object {
        private const val FIELD_PROJECT_ID = "projectId"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_MODULE_ID = "moduleId"
        private const val FIELD_TYPE = "type"

        fun tryParse(jsonReader: JsonReader): FaceVerifyRequest? {
            var request: FaceVerifyRequest? = null
            var projectId = ""
            var userId = ""
            var moduleId = ""
            var type = CAPTURE

            with(jsonReader) {
                beginObject()

                while (hasNext()) {
                    val elementName = nextName()
                    if (FaceIdentifyRequest::class.java.fields.none { it.name == elementName }) {
                        break
                    } else {
                        when (elementName) {
                            FIELD_PROJECT_ID -> projectId = nextString()
                            FIELD_USER_ID -> userId = nextString()
                            FIELD_MODULE_ID -> moduleId = nextString()
                            FIELD_TYPE -> type = valueOf(nextString())
                        }

                        val fieldsAreValid = projectId != ""
                            && userId != ""
                            && moduleId != ""
                            && type == VERIFY

                        if (fieldsAreValid) {
                            request = FaceVerifyRequest(projectId, userId, moduleId, type)
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

fun FaceVerifyRequest.fromDomainToModuleApi(): IFaceVerifyRequest = FaceVerifyRequestImpl()

@Parcelize
private data class FaceVerifyRequestImpl(override val type: IFaceRequestType = ModuleApiVerifyRequestType) : IFaceVerifyRequest
