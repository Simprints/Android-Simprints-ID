package com.simprints.feature.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.simprints.feature.login.screens.intent.LoginWrapperActivity

class ShowLoginWrapper : ActivityResultContract<Bundle, LoginResult>() {

    override fun createIntent(context: Context, input: Bundle): Intent =
        Intent(context, LoginWrapperActivity::class.java)
            .putExtra(LoginWrapperActivity.LOGIN_ARGS_EXTRA, input)

    override fun parseResult(resultCode: Int, intent: Intent?): LoginResult = intent
        ?.getParcelableExtra(LoginWrapperActivity.LOGIN_RESULT)
        ?: LoginResult(false)
}
