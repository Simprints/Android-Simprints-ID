package com.simprints.id.tools.utils

import android.content.Context

class AndroidResourcesHelperImpl(val context: Context) : AndroidResourcesHelper {

    override fun getString(res: Int): String = context.getString(res)
}
