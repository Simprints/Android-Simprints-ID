package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.exceptions.safe.NotSignedInException
import io.reactivex.Single


interface LocalDbKeyProvider {

    @Throws(NotSignedInException::class)
    fun getLocalDbKey(projectId: String): Single<LocalDbKey>
}
