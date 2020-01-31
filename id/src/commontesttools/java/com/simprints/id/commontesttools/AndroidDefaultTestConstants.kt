package com.simprints.id.commontesttools

import android.util.Base64
import com.simprints.id.data.secure.LocalDbKey

object AndroidDefaultTestConstants {

    private const val DEFAULT_REALM_KEY_STRING = "Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ=="
    val DEFAULT_REALM_KEY: ByteArray = Base64.decode(DEFAULT_REALM_KEY_STRING, Base64.NO_WRAP)

    val DEFAULT_LOCAL_DB_KEY = LocalDbKey(
        projectId = DefaultTestConstants.DEFAULT_PROJECT_ID,
        value = DEFAULT_REALM_KEY)
}
