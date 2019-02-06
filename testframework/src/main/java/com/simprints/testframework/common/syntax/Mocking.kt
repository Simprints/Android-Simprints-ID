package com.simprints.testframework.common.syntax

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

inline fun <reified T> mock(): T =
    Mockito.mock(T::class.java)

fun <T> whenever(methodCall: T): OngoingStubbing<T> =
    Mockito.`when`(methodCall)

fun <T> whenever(methodCall: () -> T): OngoingStubbing<T> =
    Mockito.`when`(methodCall())

infix fun <T> OngoingStubbing<T>.thenReturn(value: T): OngoingStubbing<T> =
    thenReturn(value)

infix fun <T> OngoingStubbing<T>.thenThrow(e: Throwable): OngoingStubbing<T> =
    thenThrow(e)

fun <T> anyNotNull(): T {
    try {
        Mockito.any<T>()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return uninitialized()
}

private fun <T> uninitialized(): T = null as T
