package com.simprints.id.data.loginInfo

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.safe.CredentialMissingException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import io.reactivex.Single

open class LoginInfoManagerImpl(override var prefs: ImprovedSharedPreferences) : LoginInfoManager {

    companion object {
        const val ENCRYPTED_PROJECT_SECRET: String = "ENCRYPTED_PROJECT_SECRET"
        const val PROJECT_ID: String = "PROJECT_ID"
        const val PROJECT_ID_CLAIM: String = "PROJECT_ID_CLAIM"
        const val USER_ID_CLAIM: String = "USER_ID_CLAIM"

        const val USER_ID: String = "USER_ID"
        private const val DEFAULT_VALUE: String = ""
    }

    override var encryptedProjectSecret: String = ""
        get() {
            val value = prefs.getPrimitive(ENCRYPTED_PROJECT_SECRET, DEFAULT_VALUE)
            if (value.isBlank()) {
                throw CredentialMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(ENCRYPTED_PROJECT_SECRET, field).commit()
        }

    override var signedInProjectId: String = ""
        get() {
            val value = prefs.getPrimitive(PROJECT_ID, DEFAULT_VALUE)
            if (value.isBlank()) {
                throw CredentialMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(PROJECT_ID, field).commit()
        }

    override var signedInUserId: String = ""
        get() {
            val value = prefs.getPrimitive(USER_ID, DEFAULT_VALUE)
            if (value.isBlank()) {
                throw CredentialMissingException()
            }
            return value
        }
        set(value) {
            field = value
            prefs.edit().putPrimitive(USER_ID, field).commit()
        }

    override fun getEncryptedProjectSecretOrEmpty(): String =
        try {
            encryptedProjectSecret
        } catch (e: CredentialMissingException) {
            ""
        }

    override fun getSignedInProjectIdOrEmpty(): String =
        try {
            signedInProjectId
        } catch (e: CredentialMissingException) {
            ""
        }

    override fun getSignedInUserIdOrEmpty(): String =
        try {
            signedInUserId
        } catch (e: CredentialMissingException) {
            ""
        }

    override fun getSignedInProjectId(): Single<String> =
        Single.create<String> {
            try {
                it.onSuccess(signedInProjectId)
            } catch (e: CredentialMissingException) {
                it.onError(NotSignedInException())
            }
        }

    override var projectIdTokenClaim: String? = DEFAULT_VALUE
        get() = prefs.getPrimitive(PROJECT_ID_CLAIM, DEFAULT_VALUE)
        set(value) {
            field = value
            prefs.edit().putPrimitive(PROJECT_ID_CLAIM, field ?: DEFAULT_VALUE).commit()
        }

    override var userIdTokenClaim: String? = DEFAULT_VALUE
        get() = prefs.getPrimitive(USER_ID_CLAIM, DEFAULT_VALUE)
        set(value) {
            field = value
            prefs.edit().putPrimitive(USER_ID_CLAIM, field ?: DEFAULT_VALUE).commit()
        }


    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        getSignedInProjectIdOrEmpty().isNotEmpty() &&
            getSignedInProjectIdOrEmpty() == possibleProjectId &&
            getEncryptedProjectSecretOrEmpty().isNotEmpty()

    override fun cleanCredentials() {
        encryptedProjectSecret = ""
        signedInProjectId = ""
        signedInUserId = ""
        clearCachedTokenClaims()
    }

    override fun clearCachedTokenClaims() {
        projectIdTokenClaim = ""
        userIdTokenClaim = ""
    }

    override fun storeCredentials(projectId: String, userId: String) {
        signedInProjectId = projectId
        signedInUserId = userId
    }
}
