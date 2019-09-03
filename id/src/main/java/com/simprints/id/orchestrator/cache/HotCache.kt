package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.simprints.id.orchestrator.steps.Step
import org.koin.core.KoinComponent
import javax.inject.Inject

class HotCache : KoinComponent {

    @Inject
    lateinit var preferences: SharedPreferences

    private val gson = Gson()

    private val steps by lazy { preferences.getStringSet(KEY_STEPS, null)?.toMutableSet() }

    fun save(step: Step) {
        val encryptedStep = encrypt(step)
        steps?.add(encryptedStep)
        preferences.edit().putStringSet(KEY_STEPS, steps).apply()
    }

    fun load() = steps?.map { decrypt(it) }

    private fun encrypt(step: Step): String {
        val json = gson.toJson(step)
        return Base64.encodeToString(json.toByteArray(), Base64.DEFAULT)
    }

    private fun decrypt(encryptedJson: String): Step {
        val json = String(Base64.decode(encryptedJson, Base64.DEFAULT))
        return gson.fromJson(json, Step::class.java)
    }

    private companion object {
        const val KEY_STEPS = "steps"
    }

}
