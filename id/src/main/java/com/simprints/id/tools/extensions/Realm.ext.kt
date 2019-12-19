package com.simprints.id.tools.extensions

import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import io.realm.*
import kotlinx.coroutines.suspendCancellableCoroutine

inline fun <reified T> List<T>.toRealmList(): RealmList<T> =
    RealmList(*this.toTypedArray())

private suspend fun <T: RealmObject, S: RealmQuery<T>> findAllAwait(query: S): RealmResults<T>? = suspendCancellableCoroutine { continuation ->
    val result =  query.findAllAsync()

    val listener = RealmChangeListener<RealmResults<T>> { queryResults ->
        if(queryResults.isLoaded) {
            if (queryResults.isValid) {
                continuation.resumeSafely(queryResults)
            } else {
                continuation.resumeSafely(null)
            }
        }
    }
    result.addChangeListener(listener)
}

private suspend fun <T: RealmObject, S: RealmQuery<T>> findFirstAwait(query: S): T? = suspendCancellableCoroutine { continuation ->
    val listener = RealmChangeListener { queryResult: T? ->
        if(queryResult?.isLoaded == true) {
            if (queryResult.isValid) {
                continuation.resumeSafely(queryResult)
            } else {
                continuation.resumeSafely(null)
            }
        }
    }
    query.findFirstAsync().addChangeListener(listener)
}

private suspend fun executeAsync(realm: Realm, block: (Realm) -> Unit): Unit = suspendCancellableCoroutine { continuation ->
    realm.executeTransactionAsync({ block(it)  }, { continuation.resumeSafely(Unit) }, { continuation.resumeWithExceptionSafely(it) })
}

suspend fun <S: RealmObject> RealmQuery<S>.await() = findAllAwait(this)

suspend fun <S: RealmObject> RealmQuery<S>.awaitFirst() = findFirstAwait(this)

suspend fun Realm.transactAwait(block: (Realm) -> Unit) =  executeAsync(this, block)
