package com.simprints.feature.dashboard.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.R
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    @Inject
    lateinit var loginManager: LoginManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (loginManager.signedInProjectId.isNotEmpty() && loginManager.signedInUserId.isNotEmpty()) {
            findNavController().navigate(R.id.action_mainFragment_to_requestLoginFragment)
        } else {
            findNavController().navigate(R.id.action_mainFragment_to_requestLoginFragment)
        }
    }
}
