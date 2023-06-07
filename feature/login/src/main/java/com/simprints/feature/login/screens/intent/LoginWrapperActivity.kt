package com.simprints.feature.login.screens.intent

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.login.R
import com.simprints.feature.login.databinding.ActivityLoginWrapperBinding
import com.simprints.infra.uibase.navigation.handleResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class LoginWrapperActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityLoginWrapperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.loginHost.handleResult<Parcelable>(this, R.id.loginFormFragment) { result ->
            setResult(RESULT_OK, Intent().also { it.putExtra(LOGIN_RESULT, result) })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val args = intent.extras?.getBundle(LOGIN_ARGS_EXTRA)
        if (args == null) {
            finish()
            return
        }
        findNavController(R.id.loginHost).setGraph(R.navigation.graph_login, args)
    }

    companion object {

        internal const val LOGIN_ARGS_EXTRA = "login_args"
        internal const val LOGIN_RESULT = "login_fragment_result"
    }
}
