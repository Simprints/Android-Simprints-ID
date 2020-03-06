package com.simprints.id.tools.extensions

import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import io.realm.*
import kotlinx.coroutines.suspendCancellableCoroutine

inline fun <reified T> List<T>.toRealmList(): RealmList<T> =
    RealmList(*this.toTypedArray())

// Strong reference to query results otherwise they may build garbage collected while we are
// waiting for the listeners to deliver the results
private val firstAsyncQueriesResult = mutableListOf<RealmObject>()
private val findAllQueriesResult = mutableListOf<RealmResults<RealmObject>>()

private suspend fun <T : RealmObject, S : RealmQuery<T>> findAllAwait(query: S): RealmResults<T>? = suspendCancellableCoroutine { continuation ->
    val result = query.findAllAsync()
    findAllQueriesResult.add(result as RealmResults<RealmObject>)

    val listener = RealmChangeListener<RealmResults<T>> { queryResults ->
        if (queryResults.isLoaded) {
            if (queryResults.isValid) {
                continuation.resumeSafely(queryResults)
            } else {
                continuation.resumeSafely(null)
            }
            try {
                findAllQueriesResult.remove(result)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
    result.addChangeListener(listener)
}

private suspend fun <T : RealmObject, S : RealmQuery<T>> findFirstAwait(query: S): T? = suspendCancellableCoroutine { continuation ->
    val queryResult = query.findFirstAsync()
    firstAsyncQueriesResult.add(queryResult)

    val listener = RealmChangeListener { result: T? ->
        if (result?.isLoaded == true) {
            if (result.isValid) {
                continuation.resumeSafely(result)
            } else {
                continuation.resumeSafely(null)
            }
            try {
                firstAsyncQueriesResult.remove(queryResult)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
    queryResult.addChangeListener(listener)
}

private suspend fun executeAsync(realm: Realm, block: (Realm) -> Unit): Unit = suspendCancellableCoroutine { continuation ->
    realm.executeTransactionAsync({ block(it) }, { continuation.resumeSafely(Unit) }, { continuation.resumeWithExceptionSafely(it) })
}

suspend fun <S : RealmObject> RealmQuery<S>.await() = findAllAwait(this)

suspend fun <S : RealmObject> RealmQuery<S>.awaitFirst() = findFirstAwait(this)

suspend fun Realm.transactAwait(block: (Realm) -> Unit) = executeAsync(this, block)
