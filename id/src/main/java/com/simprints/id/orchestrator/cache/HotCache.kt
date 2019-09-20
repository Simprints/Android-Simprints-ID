package com.simprints.id.orchestrator.cache

import com.simprints.id.orchestrator.steps.Step

interface HotCache {

    fun save(step: Step)
    fun load(): List<Step>
    fun clear()

}
