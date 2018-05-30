package com.simprints.id.data.db.dbRecovery

import io.reactivex.Completable

interface LocalDbRecoverer {

    fun recoverDb(): Completable
}
