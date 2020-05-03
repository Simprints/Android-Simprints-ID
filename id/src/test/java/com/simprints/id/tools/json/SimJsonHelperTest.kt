package com.simprints.id.tools.json

import com.google.common.truth.Truth.assertThat
import com.google.gson.reflect.TypeToken
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEvent
import org.junit.Test

class SimJsonHelperTest {

    companion object {
        private const val EVENTS_JSON = "[{\"id\":\"e9257686-663f-4943-943e-09f9fdd9252b\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordCreation\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}},{\"id\":\"e9257686-663f-4943-943e-09f9fdd925sd\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordDeletion\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}},{\"id\":\"e9257686-663f-4943-943e-09f9fdd925dsa\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordMove\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}}]"
    }

    @Test
    fun givenJson_shouldParseCorrectly() {
        val listType = object : TypeToken<ArrayList<ApiEvent?>?>() {}.type
        val apiEvents = SimJsonHelper.gson.fromJson<List<ApiEvent>>(EVENTS_JSON, listType)
        assertThat(apiEvents.size).isEqualTo(3)
        assertThat(apiEvents[0].payload).isInstanceOf(ApiEnrolmentRecordCreationPayload::class.java)
        assertThat(apiEvents[1].payload).isInstanceOf(ApiEnrolmentRecordDeletionPayload::class.java)
        assertThat(apiEvents[2].payload).isInstanceOf(ApiEnrolmentRecordMovePayload::class.java)
    }
}
