package com.simprints.fingerprint.infra.scanner.nfc.android

import android.nfc.Tag
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcTag

internal class AndroidNfcTag(
    val tag: Tag?,
) : ComponentNfcTag
