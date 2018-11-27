package com.simprints.id.shared

import com.google.common.truth.Truth

fun truthAssertValuesNotNull(vararg values: Any?) = Truth.assertThat(values).asList().doesNotContain(null)
fun truthAssertValuesAreFalse(vararg values: Boolean?) = {
    truthAssertValuesNotNull(*values)
    Truth.assertThat(values).asList().doesNotContain(false)
}
