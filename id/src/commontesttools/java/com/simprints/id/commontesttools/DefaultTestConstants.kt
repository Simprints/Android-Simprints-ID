package com.simprints.id.commontesttools

import android.util.Base64
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.data.secure.LocalDbKey

object DefaultTestConstants {
    const val DEFAULT_PROJECT_ID = "vkbpRLfHvpQqaeoxZgyx"
    const val DEFAULT_MODULE_ID = "the_one_and_only_module"
    const val DEFAULT_MODULE_ID_2 = "the_one_and_only_module2"
    const val DEFAULT_USER_ID = "the_lone_user"
    const val DEFAULT_PROJECT_SECRET = "3xDCW0IL/m7nNBWPlVQljh4RzZgcho3Gp7WEj07YqgSER6ESXeY8tVczlNsxubug7co45/PsfG7JiC9oo/U54w=="

    val DEFAULT_TEST_CALLOUT_CREDENTIALS = TestCalloutCredentials(
        projectId = DEFAULT_PROJECT_ID,
        moduleId = DEFAULT_MODULE_ID,
        userId = DEFAULT_USER_ID)

    const val DEFAULT_REALM_KEY_STRING = "Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ=="
    val DEFAULT_REALM_KEY: ByteArray = Base64.decode(DEFAULT_REALM_KEY_STRING, Base64.NO_WRAP)

    val DEFAULT_LOCAL_DB_KEY = LocalDbKey(
        projectId = DEFAULT_PROJECT_ID,
        value = DEFAULT_REALM_KEY)
}
