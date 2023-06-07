package com.simprints.feature.dashboard.base

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.R
import com.simprints.infra.authstore.AuthStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class BaseFragment : Fragment(R.layout.fragment_base) {

    @Inject
    lateinit var authStore: AuthStore

    override fun onResume() {
        super.onResume()
        if (authStore.signedInProjectId.isNotEmpty() && authStore.signedInUserId.isNotEmpty()) {
            findNavController().navigate(R.id.action_baseFragment_to_mainFragment)
        } else {
            findNavController().navigate(R.id.action_baseFragment_to_requestLoginFragment)
        }
    }
}
