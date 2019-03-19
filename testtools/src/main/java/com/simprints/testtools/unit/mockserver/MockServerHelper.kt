package com.simprints.testtools.unit.mockserver

import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert

fun assertQueryUrlParam(url: HttpUrl, paramName: String, expected: Any, transformUrlValue: (String?) -> Any? = { it }) {
    Assert.assertEquals(expected, transformUrlValue(url.queryParameter(paramName)))
}

fun assertPathUrlParam(url: HttpUrl, expected: String) {
    Assert.assertTrue(url.pathSegments().contains(expected))
}

fun mockSuccessfulResponse(): MockResponse =
    MockResponse().setResponseCode(200)

fun mockNotFoundResponse(): MockResponse =
    MockResponse().setResponseCode(404)

fun mockServerProblemResponse(): MockResponse =
    MockResponse().setResponseCode(500)
