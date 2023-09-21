package com.simprints.face.capture.models

import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SecuredImageRef(override val path: Path) : ISecuredImageRef

