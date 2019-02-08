package com.simprints.id.commontesttools.testTools.extensions

import io.reactivex.observers.TestObserver

fun <T> TestObserver<T>.awaitAndAssertSuccess(): TestObserver<T> {
    return this.await().assertComplete().assertNoErrors()
}
