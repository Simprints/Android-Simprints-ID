package com.simprints.testtools.reflection

import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible

suspend fun Any.runPrivateSuspendFunction(name: String, vararg args: Any?): Any? =
    this::class.memberFunctions.first { it.name.contains(name, ignoreCase = true) }.also {
        it.isAccessible = true
        return it.callSuspend(this, *args)
    }

suspend fun Any.runPrivateParentSuspendFunction(name: String, vararg args: Any?): Any? =
    this::class.superclasses.first().memberFunctions.first {
        it.name.contains(name, ignoreCase = true)
    }.also {
        it.isAccessible = true
        return it.callSuspend(this, *args)
    }