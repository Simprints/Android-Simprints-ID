package com.simprints.id.data.db.remote.network

//it wraps a list to print the values of it separated by |
//Required by Retrofit to send params as moduleId=module1|module2
//As default Retrofit sends list as moduleId=module1&moduleId=module2
class PipeSeparatorWrapperForURLListParam<T>(vararg val values: T) {

    override fun toString(): String = values.joinToString(separator = "|")
}
