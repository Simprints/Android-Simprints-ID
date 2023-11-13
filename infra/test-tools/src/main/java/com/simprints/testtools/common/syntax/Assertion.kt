package com.simprints.testtools.common.syntax

import io.reactivex.disposables.Disposable
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert


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
