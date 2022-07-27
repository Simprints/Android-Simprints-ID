package com.simprints.testtools.common.syntax

import com.nhaarman.mockitokotlin2.*
import io.reactivex.disposables.Disposable
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert
import org.mockito.Mockito
import org.mockito.verification.VerificationMode

fun <T> verifyOnce(mock: T, methodCall: T.() -> Any?) =
    verifyExactly(1, mock, methodCall)

fun <T> verifyExactly(times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(Mockito::times, times, mock, methodCall)

private fun <T> verify(mode: (Int) -> VerificationMode, times: Int, mock: T, methodCall: T.() -> Any?) =
    verify(mock, mode(times)).methodCall()



fun <T> verifyOnlyInteraction(mock: T, methodCall: T.() -> Any?) {
    verify(mock).methodCall()
    verifyNoMoreInteractions(mock)
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
    failTest("Expected an ${T::class.java.simpleName} to be thrown")
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

fun <T> TestSubscriber<T>.awaitCompletionWithNoErrors() {
    awaitTerminalEvent()
    assertComplete()
    assertNoErrors()
}

fun failTest(message: String?): Nothing {
    Assert.fail(message)
    throw Exception(message)
}
