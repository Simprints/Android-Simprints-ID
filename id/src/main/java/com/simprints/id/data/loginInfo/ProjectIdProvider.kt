package com.simprints.id.data.loginInfo

import io.reactivex.Single


interface ProjectIdProvider {

    fun getSignedInProjectId(): Single<String>

}
