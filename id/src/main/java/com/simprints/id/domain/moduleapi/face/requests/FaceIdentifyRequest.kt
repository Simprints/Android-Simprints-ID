package com.simprints.id.domain.moduleapi.face.requests

import com.google.gson.stream.JsonReader
import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.moduleapi.face.requests.IFaceIdentifyRequest
import com.simprints.moduleapi.face.requests.IFaceRequestType
import kotlinx.android.parcel.Parcelize
import com.simprints.moduleapi.face.requests.IFaceRequestType.IDENTIFY as ModuleApiIdentifyRequestType

@Parcelize
data class FaceIdentifyRequest(val projectId: String,
                               val userId: String,
                               val moduleId: String,
                               override val type: FaceRequestType = IDENTIFY) : FaceRequest() {

    companion object {
        private const val FIELD_PROJECT_ID = "projectId"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_MODULE_ID = "moduleId"
        private const val FIELD_TYPE = "type"

        fun tryParse(jsonReader: JsonReader): FaceIdentifyRequest? {
            var request: FaceIdentifyRequest? = null
            var projectId = ""
            var userId = ""
            var moduleId = ""
            var type: FaceRequestType? = null

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
                            && type == IDENTIFY

                        if (fieldsAreValid) {
                            request = FaceIdentifyRequest(projectId, userId, moduleId, type!!)
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

fun FaceIdentifyRequest.fromDomainToModuleApi(): IFaceIdentifyRequest = FaceIdentifyRequestImpl()

@Parcelize
private data class FaceIdentifyRequestImpl(override val type: IFaceRequestType = ModuleApiIdentifyRequestType) : IFaceIdentifyRequest
