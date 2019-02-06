package com.simprints.testframework.unit.mockserver

import okhttp3.HttpUrl
import org.junit.Assert

fun assertQueryUrlParam(url: HttpUrl, paramName: String, expected: Any, transformUrlValue: (String?) -> Any? = { it }) {
    Assert.assertEquals(expected, transformUrlValue(url.queryParameter(paramName)))
}

fun assertPathUrlParam(url: HttpUrl, expected: String) {
    Assert.assertTrue(url.pathSegments().contains(expected))
}
