package com.simprints.testframework.common.syntax

import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.mockito.stubbing.OngoingStubbing

inline fun <reified T> mock(): T =
    Mockito.mock(T::class.java)

fun <T> whenever(methodCall: T): InfixOngoingStubbing<T> =
    InfixOngoingStubbing(Mockito.`when`(methodCall))

fun <T> whenever(methodCall: () -> T): InfixOngoingStubbing<T> =
    InfixOngoingStubbing(Mockito.`when`(methodCall()))

/**
 * This class lightly wraps [OngoingStubbing] to allow some methods to be infix
 */
class InfixOngoingStubbing<T>(private val ongoingStubbing: OngoingStubbing<T>)
    : OngoingStubbing<T> by ongoingStubbing {

    override infix fun thenReturn(value: T) =
        InfixOngoingStubbing(ongoingStubbing.thenReturn(value))

    infix fun thenThrow(e: Throwable) =
        InfixOngoingStubbing(ongoingStubbing.thenThrow(e))

    override fun thenThrow(throwableType: Class<out Throwable>) =
        InfixOngoingStubbing(ongoingStubbing.thenThrow(throwableType))

    override fun then(answer: Answer<*>) =
        InfixOngoingStubbing(ongoingStubbing.then(answer))

    override fun thenAnswer(answer: Answer<*>?) =
        InfixOngoingStubbing(ongoingStubbing.thenAnswer(answer))
}

/**
 * A complement to [Mockito.any] that appropriately ensures non-null values
 */
fun <T> anyNotNull(): T {
    try {
        Mockito.any<T>()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return uninitialized()
}

@Suppress("UNCHECKED_CAST")
private fun <T> uninitialized(): T = null as T
