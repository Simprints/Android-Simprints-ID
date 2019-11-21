package com.simprints.id.tools.extensions

import io.realm.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

inline fun <reified T> List<T>.toRealmList(): RealmList<T> =
    RealmList(*this.toTypedArray())

private suspend fun <T: RealmObject, S: RealmQuery<T>> findAllAwait(query: S): RealmResults<T>? = suspendCancellableCoroutine { continuation ->
    val result =  query.findAllAsync()

    val listener = RealmChangeListener<RealmResults<T>> { queryResults ->
        if(queryResults.isLoaded) {
            if (queryResults.isValid) {
                continuation.resume(queryResults)
            } else {
                continuation.resume(null)
            }
        }
    }
    result.addChangeListener(listener)
}

private suspend fun <T: RealmObject, S: RealmQuery<T>> findFirstAwait(query: S): T? = suspendCancellableCoroutine { continuation ->
    val listener = RealmChangeListener { queryResult: T? ->
        if(queryResult?.isLoaded == true) {
            if (queryResult.isValid) {
                continuation.resume(queryResult)
            } else {
                continuation.resume(null)
            }
        }
    }
    query.findFirstAsync().addChangeListener(listener)
}

private suspend fun executeAsync(realm: Realm, block: (Realm) -> Unit): Unit = suspendCancellableCoroutine { continuation ->
    realm.executeTransactionAsync({ block(it)  }, { continuation.resume(Unit) }, { continuation.resumeWithException(it) })
}

suspend fun <S: RealmObject> RealmQuery<S>.await() = findAllAwait(this)

suspend fun <S: RealmObject> RealmQuery<S>.awaitFirst() = findFirstAwait(this)

suspend fun Realm.transactAwait(block: (Realm) -> Unit) =  executeAsync(this, block)
