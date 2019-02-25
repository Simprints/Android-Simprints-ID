package com.simprints.testtools.common.syntax

import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.stubbing.OngoingStubbing

inline fun <reified T> mock(): T =
    Mockito.mock(T::class.java)

inline fun <reified T> spy(): T =
    Mockito.spy(T::class.java)

fun <T> spy(t: T): T =
    Mockito.spy(t)

/** For mocks only */
fun <T> whenever(methodCall: T): InfixOngoingStubbing<T> =
    InfixOngoingStubbing(Mockito.`when`(methodCall))

/** For mocks only */
fun <T> whenever(methodCall: () -> T): InfixOngoingStubbing<T> =
    InfixOngoingStubbing(Mockito.`when`(methodCall()))

/** For both mocks and spies */
fun <T, R> whenever(mock: T, methodCall: T.() -> R): InfixStubber<T, R> =
    InfixStubber(mock, methodCall)

/**
 * This class is for re-arranging and infix-ing the "doReturn ... when ..." family of methods
 */
class InfixStubber<T, R>(private val obj: T, private val methodCall: T.() -> R) {

    infix fun thenReturn(value: R) =
        Mockito.doReturn(value).`when`(obj).methodCall()

    infix fun thenThrow(e: Throwable) =
        Mockito.doThrow(e).`when`(obj).methodCall()

    infix fun thenThrow(throwableType: Class<out Throwable>) =
        Mockito.doThrow(throwableType).`when`(obj).methodCall()

    infix fun thenAnswer(answer: Answer<*>) =
        Mockito.doAnswer(answer).`when`(obj).methodCall()

    infix fun thenAnswer(answer: (InvocationOnMock) -> Any?) =
        Mockito.doAnswer(answer).`when`(obj).methodCall()

    infix fun then(answer: Answer<*>) =
        thenAnswer(answer)

    infix fun then(answer: (InvocationOnMock) -> Any?) =
        thenAnswer(answer)

    fun thenDoNothing() =
        Mockito.doNothing().`when`(obj).methodCall()

    infix fun thenDoNothing(@Suppress("UNUSED_PARAMETER") ignored: () -> Unit) =
        Mockito.doNothing().`when`(obj).methodCall()
}

/**
 * This class lightly wraps [OngoingStubbing] to allow some methods to be infix
 */
class InfixOngoingStubbing<T>(private val ongoingStubbing: OngoingStubbing<T>)
    : OngoingStubbing<T> by ongoingStubbing {

    override infix fun thenReturn(value: T) =
        InfixOngoingStubbing(ongoingStubbing.thenReturn(value))

    infix fun thenThrow(e: Throwable) =
        InfixOngoingStubbing(ongoingStubbing.thenThrow(e))

    override infix fun thenThrow(throwableType: Class<out Throwable>) =
        InfixOngoingStubbing(ongoingStubbing.thenThrow(throwableType))

    override infix fun then(answer: Answer<*>) =
        InfixOngoingStubbing(ongoingStubbing.then(answer))

    override infix fun thenAnswer(answer: Answer<*>) =
        InfixOngoingStubbing(ongoingStubbing.thenAnswer(answer))
}

/**
 * An explicit [Mockito.any] that allows null values
 */
fun <T> anyOrNull(): T? = Mockito.any<T>()

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
