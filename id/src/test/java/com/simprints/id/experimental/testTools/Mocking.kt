package com.simprints.id.experimental.testTools

import com.nhaarman.mockito_kotlin.atLeast
import com.nhaarman.mockito_kotlin.atMost
import com.nhaarman.mockito_kotlin.times
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.mockito.verification.VerificationMode

inline fun <reified T> mock(): T =
    Mockito.mock(T::class.java)

// Setup behaviour

fun <T> whenever(methodCall: () -> T): OngoingStubbing<T> =
    Mockito.`when`(methodCall())

infix fun <T> OngoingStubbing<T>.thenReturn(value: T): OngoingStubbing<T> =
    thenReturn(value)

infix fun <T> OngoingStubbing<T>.thenThrow(e: Throwable): OngoingStubbing<T> =
    thenThrow(e)

// Assertion operations

fun <T> verifyOnce(mock: T, methodCall: T.() -> Any?) =
    verifyExactly(1, mock, methodCall)

fun <T> verifyNever(mock: T, methodCall: T.() -> Any?) =
    verifyExactly(0, mock, methodCall)

fun <T> verifyExactly(times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(::times, times, mock, methodCall)

fun <T> verifyAtLeast(times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(::atLeast, times, mock, methodCall)

fun <T> verifyAtMost(times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(::atMost, times, mock, methodCall)

private fun <T> verify(mode: (Int) -> VerificationMode, times: Int, mock: T, methodCall: T.() -> Any?) =
    Mockito.verify(mock, mode(times)).methodCall()
