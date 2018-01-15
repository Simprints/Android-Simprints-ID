package com.simprints.id.tools

import java.util.regex.Pattern

class GuidValidator(private val pattern: Pattern = Pattern.compile(GUID_REG_EX)) {

    companion object {
        private val GUID_REG_EX = "^([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}$"
    }

    fun isGuid(expression: String): Boolean =
            pattern.matcher(expression).matches()

}

