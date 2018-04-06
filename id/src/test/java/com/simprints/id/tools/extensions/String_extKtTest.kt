package com.simprints.id.tools.extensions

import junit.framework.Assert
import org.junit.Test

class String_extKtTest {

    @Test
    fun fromLowerCamelToLowerUnderscore() {
        Assert.assertEquals("some_lower_case_camel", "someLowerCaseCamel".fromLowerCamelToLowerUnderscore())
        Assert.assertEquals("some_lower_case_camel", "SomeLowerCaseCamel".fromLowerCamelToLowerUnderscore())
    }
}
