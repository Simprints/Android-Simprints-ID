package com.simprints.id.testTools.extensions

import io.reactivex.observers.TestObserver


fun <T> TestObserver<T>.awaitAndAssertSuccess(): TestObserver<T> {
    return this.await().assertComplete().assertNoErrors()
}
