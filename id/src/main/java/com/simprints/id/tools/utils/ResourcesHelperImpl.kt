package com.simprints.id.tools.utils

import android.content.Context
import com.simprints.id.tools.ResourcesHelper

class ResourcesHelperImpl(val ctx: Context) : ResourcesHelper {
    override fun getString(res: Int): String = ctx.getString(res)
}
