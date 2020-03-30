package com.simprints.id.tools.extensions

import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.getString(field: String): String = this.get(field).asString

//To be removed when RemoteConfigWrapper is updated with the fix to store values in preferences synchronously
fun JsonElement.getProjectLanguagesFromJsonOrEmpty(): Array<String> =
    try{
        val parameters = (this as JsonObject).get(REMOTE_CONFIG_PARAMETERS_ELEMENT_KEY).asJsonObject
        val projectLanguagesString = (parameters.get(REMOTE_CONFIG_PROJECT_LANGUAGES_ELEMENT_KEY)).asString
        projectLanguagesString.split(",").toTypedArray()
    } catch (t: Throwable) {
        arrayOf("")
    }

private const val REMOTE_CONFIG_PARAMETERS_ELEMENT_KEY = "parameters"
private const val REMOTE_CONFIG_PROJECT_LANGUAGES_ELEMENT_KEY = "ProjectLanguages"
