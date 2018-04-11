package com.simprints.id.data.db

import io.reactivex.Single


interface ProjectIdProvider {

    fun getSignedInProjectId(): Single<String>

}
