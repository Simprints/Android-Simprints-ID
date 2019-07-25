package com.simprints.clientapi.tools.json

import com.google.gson.Gson

class GsonBuilderImpl: GsonBuilder {
    override fun build(): Gson = Gson()
}
