package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.GsonBuilder
import com.simprints.id.orchestrator.steps.Step

class HotCacheImpl(private val preferences: SharedPreferences) : HotCache {

    private val gson by lazy {
        GsonBuilder().also(::registerStepAdapter).create()
    }

    private val steps by lazy { preferences.getStringSet(KEY_STEPS, null)?.toMutableSet() }

    override fun save(step: Step) {
        val encryptedStep = encrypt(step)
        steps?.add(encryptedStep)
        preferences.edit().putStringSet(KEY_STEPS, steps).apply()
    }

    override fun load() = steps?.map { decrypt(it) }

    override fun clear() {
        preferences.edit().putStringSet(KEY_STEPS, null).apply()
    }

    private fun encrypt(step: Step): String {
        val json = gson.toJson(step)
        return Base64.encodeToString(json.toByteArray(), Base64.DEFAULT)
    }

    private fun decrypt(encryptedJson: String): Step {
        val json = String(Base64.decode(encryptedJson, Base64.DEFAULT))
        return gson.fromJson(json, Step::class.java)
    }

    private fun registerStepAdapter(gsonBuilder: GsonBuilder) {
        gsonBuilder.registerTypeAdapter(Step::class.java, Step.JsonAdapter())
    }

    private companion object {
        const val KEY_STEPS = "steps"
    }

}
