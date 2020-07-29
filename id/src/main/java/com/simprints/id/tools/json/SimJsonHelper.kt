package com.simprints.id.tools.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.gson.Gson
import com.simprints.core.tools.json.JsonHelper

object SimJsonHelper {

    val jackson = ObjectMapper().registerKotlinModule()

    val gson: Gson by lazy {
        JsonHelper.defaultBuilder.create()
    }
}
