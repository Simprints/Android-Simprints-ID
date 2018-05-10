package com.simprints.id.data.secure


interface SecureDataManager {

    fun setLocalDatabaseKey(localDatabaseKey: String)

    fun getLocalDatabaseKey(): String

}
