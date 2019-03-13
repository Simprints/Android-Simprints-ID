package com.simprints.id.domain.responses

import com.simprints.id.domain.refusal_form.RefusalFormAnswer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RefusalFormResponse(val answer: RefusalFormAnswer): Response
