package com.simprints.face.refusal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.simprints.face.R
import com.simprints.face.capture.FaceCaptureViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class RefusalFragment : Fragment(R.layout.refusal_fragment) {

    private val mainVm: FaceCaptureViewModel by sharedViewModel()
    private val vm: RefusalViewModel by viewModel { parametersOf(mainVm) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
