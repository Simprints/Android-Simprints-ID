package com.simprints.id.testUtils.mockServer

import okhttp3.HttpUrl
import org.junit.Assert

fun assertUrlParam(url: HttpUrl, paramName: String, expected: Any, transformUrlValue: (String?) -> Any? = { it }) {
    Assert.assertEquals(expected, transformUrlValue(url.queryParameter(paramName)))
}
