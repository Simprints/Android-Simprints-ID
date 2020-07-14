package com.simprints.face.configuration

import androidx.fragment.app.Fragment
import com.simprints.face.R
import org.koin.android.viewmodel.ext.android.viewModel

class ConfigurationFragment : Fragment(R.layout.configuration_fragment) {

    private val viewModel: ConfigurationViewModel by viewModel()

}
