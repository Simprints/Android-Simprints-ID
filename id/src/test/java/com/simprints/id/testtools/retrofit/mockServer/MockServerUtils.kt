package com.simprints.id.testtools.retrofit.mockServer

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.tools.json.JsonHelper
import okhttp3.mockwebserver.MockResponse

fun mockServerProblemResponse(): MockResponse =
    MockResponse().setResponseCode(500)

fun mockNotFoundResponse(): MockResponse =
    MockResponse().setResponseCode(404)

fun mockResponseForUploadPatient(): MockResponse =
    MockResponse().setResponseCode(200)

fun mockResponseForDownloadPatient(patient: fb_Person): MockResponse {
    return MockResponse().let {
        it.setResponseCode(200)
        it.setBody(JsonHelper.toJson(patient))
    }
}
