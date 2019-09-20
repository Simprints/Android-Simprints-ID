package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.id.orchestrator.cache.crypto.StepEncoder
import com.simprints.id.orchestrator.steps.Step

class HotCacheImpl(private val preferences: SharedPreferences,
                   private val stepEncoder: StepEncoder) : HotCache {

    private val memoryCache = arrayListOf<String>()

    override fun save(step: Step) {
        val stepNotInCache = memoryCache.none { cachedStep ->
            cachedStep.contains(step.id)
        }

        if (stepNotInCache) {
            addStepToCache(step)
        } else {
            val cachedStepString = memoryCache.find { cachedStep -> cachedStep.contains(step.id) }
            memoryCache.remove(cachedStepString)
            addStepToCache(step)
        }
    }

    override fun load(): List<Step> {
        val preferencesCache = loadFromPreferences()

        val cache = if (memoryCache.isEmpty() && preferencesCache.isNotEmpty())
            memoryCache.apply { addAll(preferencesCache) }
        else
            memoryCache

        return cache.map {
            val encodedStepWithoutId = it.split(SEPARATOR).last()
            stepEncoder.decode(encodedStepWithoutId)
        }
    }

    override fun clear() {
        preferences.edit().putStringSet(KEY_STEPS, null).apply()
        memoryCache.clear()
    }

    private fun addStepToCache(step: Step) {
        stepEncoder.encode(step).also { encodedStep ->
            val encodedStepWithId = "${step.id}$SEPARATOR$encodedStep"
            memoryCache.add(encodedStepWithId)
            preferences.edit()?.putStringSet(KEY_STEPS, memoryCache.toSet())?.apply()
        }
    }

    private fun loadFromPreferences() = preferences.getStringSet(KEY_STEPS, setOf())!!.toList()

    private companion object {
        const val KEY_STEPS = "steps"
        const val SEPARATOR = '#'
    }

}
