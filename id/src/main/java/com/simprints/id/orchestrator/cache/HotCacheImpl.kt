package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelisedObject

class HotCacheImpl(private val preferences: SharedPreferences,
                   private val keystoreManager: KeystoreManager) : HotCache {

    private val steps = preferences.getStringSet(KEY_STEPS, null)?.toMutableSet()

    override fun save(step: Step?) {
        step?.let {
            encrypt(it).also { encryptedStep ->
                steps?.add(encryptedStep)
                preferences.edit()?.putStringSet(KEY_STEPS, steps)?.apply()
            }
        }
    }

    override fun load() = steps?.mapNotNull { decrypt(it) }

    override fun clear() {
        preferences.edit().putStringSet(KEY_STEPS, null).apply()
    }

    private fun encrypt(step: Step): String {
        val parcelisedObject = ParcelisedObject(step)
        val string = String(parcelisedObject.toBytes())
        parcelisedObject.recycle()
        return keystoreManager.encryptString(string)
    }

    private fun decrypt(encryptedStep: String?): Step? {
        return encryptedStep?.let {
            val bytes = keystoreManager.decryptString(it).toByteArray()
            val parcelisedObject = ParcelisedObject(bytes)
            val parcel = parcelisedObject.getParcel()
            parcelisedObject.recycle()
            Step.createFromParcel(parcel)
        }
    }

    private companion object {
        const val KEY_STEPS = "steps"
    }

}
