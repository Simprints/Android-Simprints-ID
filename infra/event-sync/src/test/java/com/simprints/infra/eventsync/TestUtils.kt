package com.simprints.infra.eventsync

import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible


suspend fun Any.runPrivateSuspendFunction(name: String, vararg args: Any?): Any? =
    this::class.memberFunctions.first { it.name.contains(name, ignoreCase = true) }.also {
        it.isAccessible = true
        return it.callSuspend(this, *args)
    }