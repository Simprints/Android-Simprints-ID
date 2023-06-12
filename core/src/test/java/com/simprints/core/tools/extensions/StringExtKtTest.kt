package com.simprints.core.tools.extensions

import com.simprints.core.tools.extentions.fromLowerCamelToLowerUnderscore
import org.junit.Assert
import org.junit.Test

class StringExtKtTest {

    @Test
    fun fromLowerCamelToLowerUnderscore() {
        Assert.assertEquals("some_lower_case_camel", "someLowerCaseCamel".fromLowerCamelToLowerUnderscore())
        Assert.assertEquals("some_lower_case_camel", "SomeLowerCaseCamel".fromLowerCamelToLowerUnderscore())
    }
}
