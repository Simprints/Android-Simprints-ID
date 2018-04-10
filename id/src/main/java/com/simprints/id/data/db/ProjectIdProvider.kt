package com.simprints.id.data.db

import com.simprints.id.exceptions.safe.NotSignedInException
import io.reactivex.Single


interface ProjectIdProvider {

    fun getSignedInProjectId(): Single<String>

}
