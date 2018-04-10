package com.simprints.id.data.db

import com.simprints.id.exceptions.safe.NotSignedInException
import io.reactivex.Single


interface ProjectIdProvider {

    @Throws(NotSignedInException::class)
    fun getSignedInProjectId(): Single<String>
}
