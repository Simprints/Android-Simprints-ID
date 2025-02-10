package com.simprints.feature.dashboard.base

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.feature.dashboard.R
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.uibase.navigation.navigateSafely
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class BaseFragment : Fragment(R.layout.fragment_base) {
    @Inject
    lateinit var authStore: AuthStore

    override fun onResume() {
        super.onResume()
        if (authStore.signedInProjectId.isNotEmpty()) {
            findNavController().navigateSafely(this, BaseFragmentDirections.actionBaseFragmentToMainFragment())
        } else {
            findNavController().navigateSafely(this, BaseFragmentDirections.actionBaseFragmentToRequestLoginFragment())
        }
    }
}
