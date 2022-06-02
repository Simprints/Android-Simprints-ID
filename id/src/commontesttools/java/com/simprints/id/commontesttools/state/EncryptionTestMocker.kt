package com.simprints.id.commontesttools.state

import android.content.Context
import android.content.SharedPreferences

fun setupFakeEncryptedSharedPreferences(ctx: Context): SharedPreferences {
    return ctx.getSharedPreferences("test", 0)
}

