package com.simprints.testframework.common.syntax

import io.reactivex.disposables.Disposable
import io.reactivex.observers.TestObserver
import junit.framework.AssertionFailedError
import org.junit.Assert
import org.mockito.Mockito
import org.mockito.verification.VerificationMode

fun <T> verifyOnce(mock: T, methodCall: T.() -> Any?) =
    verifyExactly(1, mock, methodCall)

fun <T> verifyNever(mock: T, methodCall: T.() -> Any?) =
    verifyExactly(0, mock, methodCall)

fun <T> verifyExactly(times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(Mockito::times, times, mock, methodCall)

fun <T> verifyAtLeast(times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(Mockito::atLeast, times, mock, methodCall)

fun <T> verifyAtMost(times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(Mockito::atMost, times, mock, methodCall)

private fun <T> verify(mode: (Int) -> VerificationMode, times: Int, mock: T, methodCall: T.() -> Any?) =
    Mockito.verify(mock, mode(times)).methodCall()

fun <T> verifyOnlyInteraction(mock: T, methodCall: T.() -> Any?) {
    Mockito.verify(mock).methodCall()
    Mockito.verifyNoMoreInteractions(mock)
}

fun <T> verifyOnlyInteractions(mock: T, vararg methodCalls: T.() -> Any?) {
    for (methodCall in methodCalls) {
        Mockito.verify(mock).methodCall()
    }
    Mockito.verifyNoMoreInteractions(mock)
}

inline fun <reified T : Throwable> assertThrows(executable: () -> Unit): T {
    try {
        executable()
    } catch (exception: Throwable) {
        when (exception) {
            is T -> return exception
            else -> throw(exception)
        }
    }
    throw AssertionFailedError("Expected an ${T::class.java.simpleName} to be thrown")
}

inline fun <reified T : Throwable> assertThrows(throwable: T, executable: () -> Unit): T {
    val thrown = assertThrows<T>(executable)
    Assert.assertEquals(throwable, thrown)
    return thrown
}

fun <T> TestObserver<T>.awaitAndAssertSuccess(): Disposable = this
    .await()
    .assertComplete()
    .assertNoErrors()
