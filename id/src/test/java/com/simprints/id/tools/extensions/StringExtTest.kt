package com.simprints.id.tools.extensions

import org.junit.Assert
import org.junit.Test

class StringExtTest {

    @Test
    fun testMd5() {
        val string = "test-test-test"
        val md5 = string.md5()
        Assert.assertEquals(md5, "730a623d42add4ba8452e0025a70c486")
    }
}
