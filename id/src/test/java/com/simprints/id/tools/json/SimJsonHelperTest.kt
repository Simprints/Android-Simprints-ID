//package com.simprints.id.tools.json
//
//import com.google.common.truth.Truth.assertThat
//import com.google.gson.reflect.TypeToken
//import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationPayload
//import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionPayload
//import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMovePayload
//import com.simprints.id.data.db.event.remote.events.ApiEvent
//import org.junit.Test
//TOFIX
//class SimJsonHelperTest {
//
//    companion object {
//        private const val EVENTS_JSON = "[{\"id\":\"e9257686-663f-4943-943e-09f9fdd9252b\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordCreation\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}},{\"id\":\"e9257686-663f-4943-943e-09f9fdd925sd\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordDeletion\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}},{\"id\":\"e9257686-663f-4943-943e-09f9fdd925dsa\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"enrolmentRecordCreation\":{\"attendantId\":\"user a\",\"moduleId\":\"module03\",\"projectId\":\"TEST6Oai41ps1pBNrzBL\",\"subjectId\":\"5d08d867-fa87-41e4-8df0-98c7256950c7\",\"type\":\"EnrolmentRecordCreation\",\"biometricReferences\":[{\"templates\":[{\"finger\":\"LEFT_THUMB\",\"quality\":78,\"template\":\"Rk1SACAyMAAAAAEIAAABLAGQAMUAxQEAABBOJ0BnAF0uAEC4AGwfAIB7AHkrAECOAIEcAIDLAIUVAIBsAJMlAEC6AJsVAEAXAJyeAEC9AJ+bAEBoAKGqAEDSAKgVAICtAK0RAECMAK8aAIBfALAiAICXALOXAEDWAL4UAIAwAMsbAIC9ANCQAEBhANEeAIBQANebAICUANgVAEApAOQVAIBAAPUUAEDmAPsGAICCAQETAEBjAQQaAEB8AQuWAIDUASgDAICnASsRAEC4ATSUAIB/AUidAIDBAUmXAIDCAVSvAICnAVigAED8AV74AECqAV+uAICiAXGsAEB5AXOqAEC5AXw4AAAA\"},{\"finger\":\"LEFT_INDEX_FINGER\",\"quality\":80,\"template\":\"Rk1SACAyMAAAAAEaAAABLAGQAMUAxQEAABBQKoCMAECsAEBnAF0uAEC4AGocAIB7AHkrAIDkAHudAECOAIEcAIDLAIUVAIDoAJEcAIBsAJMlAIC1AJoVAEBnAKGsAEC9AKGYAEDSAKgVAICtAK0RAECMALAcAIBfALEkAICXALKaAEDWAL4UAIAuAMwYAIC9ANCQAIBRANadAICTANkYAIBeANkfAEAmAOUUAIBAAPUVAEDnAPsHAICBAQETAEBiAQMYAED2AQ0HAICAAQ2UAICnASoRAEDYASoAAIC6AS+RAIB+AUeaAIC/AUiXAIClAVahAEDCAVmxAED7AV37AECmAWKqAIB2AWyqAECmAW2sAEC4AXg1AAAA\"}],\"type\":\"FingerprintReference\"},{\"templates\":[],\"type\":\"FaceReference\"}]},\"enrolmentRecordDeletion\":{\"attendantId\":\"user a\",\"moduleId\":\"module01\",\"projectId\":\"TEST6Oai41ps1pBNrzBL\",\"subjectId\":\"5d08d867-fa87-41e4-8df0-98c7256950c7\",\"type\":\"EnrolmentRecordDeletion\"},\"type\":\"EnrolmentRecordMove\"}}]"
//    }
//
//    @Test
//    fun givenJson_shouldParseCorrectly() {
//        val listType = object : TypeToken<ArrayList<ApiEvent?>?>() {}.type
//        val apiEvents = SimJsonHelper.gson.fromJson<List<ApiEvent>>(EVENTS_JSON, listType)
//        assertThat(apiEvents.size).isEqualTo(3)
//        assertThat(apiEvents[0].payload).isInstanceOf(ApiEnrolmentRecordCreationPayload::class.java)
//        assertThat(apiEvents[1].payload).isInstanceOf(ApiEnrolmentRecordDeletionPayload::class.java)
//
//        with(apiEvents[2].payload) {
//            assertThat(this).isInstanceOf(ApiEnrolmentRecordMovePayload::class.java)
//            assertThat((this as ApiEnrolmentRecordMovePayload).enrolmentRecordCreation).isNotNull()
//            assertThat(this.enrolmentRecordDeletion).isNotNull()
//        }
//    }
//}
