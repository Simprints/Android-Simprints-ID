package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import com.simprints.id.orchestrator.cache.crypto.StepEncoder
import com.simprints.id.orchestrator.steps.Step

class HotCacheImpl(private val preferences: SharedPreferences,
                   private val stepEncoder: StepEncoder) : HotCache {

    private val steps = preferences.getStringSet(KEY_STEPS, null)?.toMutableSet()

    override fun save(step: Step?) {
        step?.let {
            val stepNotInCache = steps?.none { cachedStep ->
                cachedStep.contains(it.id)
            } ?: true

            if (stepNotInCache) {
                addStepToCache(step)
            } else {
                val cachedStepString = steps?.find { cachedStep -> cachedStep.contains(step.id) }
                steps?.remove(cachedStepString)
                addStepToCache(step)
            }
        }
    }

    override fun load() = steps?.mapNotNull {
        val encodedStepWithoutId = it.split(SEPARATOR).last()
        stepEncoder.decode(encodedStepWithoutId)
    }

    override fun clear() {
        preferences.edit().putStringSet(KEY_STEPS, null).apply()
    }

    private fun addStepToCache(step: Step) {
        stepEncoder.encode(step).also { encodedStep ->
            val encodedStepWithId = "${step.id}$SEPARATOR$encodedStep"
            steps?.add(encodedStepWithId)
            preferences.edit()?.putStringSet(KEY_STEPS, steps)?.apply()
        }
    }

    private companion object {
        const val KEY_STEPS = "steps"
        const val SEPARATOR = '#'
    }

}
