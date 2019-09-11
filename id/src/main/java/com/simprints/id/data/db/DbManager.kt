package com.simprints.id.data.db

import com.simprints.id.secure.models.Token
import io.reactivex.Completable

interface DbManager {

    fun signIn(projectId: String, userId: String, token: Token): Completable
    fun signOut()
}
