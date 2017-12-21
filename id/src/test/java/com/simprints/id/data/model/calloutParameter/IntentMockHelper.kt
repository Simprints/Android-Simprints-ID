package com.simprints.id.data.model.calloutParameter

import android.content.Intent
import android.os.Bundle
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever


fun mockIntent(vararg params: Pair<String, Any>): Intent {
    val intent = mock<Intent>()
    val mockBundle = mockBundle(*params)
    whenever(intent.extras).thenReturn(mockBundle)
    return intent
}

private fun mockBundle(vararg params: Pair<String, Any>): Bundle {
    val bundle = mock<Bundle>()
    for ((key, value) in params) {
        whenever(bundle.get(key)).thenReturn(value)
    }
    return bundle
}
