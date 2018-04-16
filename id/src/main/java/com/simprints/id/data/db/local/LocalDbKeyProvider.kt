package com.simprints.id.data.db.local

import com.simprints.id.data.db.local.models.LocalDbKey
import io.reactivex.Single

interface LocalDbKeyProvider {

    fun getLocalDbKey(): Single<LocalDbKey>
}
