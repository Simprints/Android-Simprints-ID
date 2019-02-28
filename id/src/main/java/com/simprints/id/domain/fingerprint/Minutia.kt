package com.simprints.id.domain.fingerprint

import java.util.*

class Minutia(var x: Short = 0,
              var y: Short = 0,
              var type: Byte = 0,
              var direction: Byte = 0,
              var quality: Byte = 0) {

    override fun toString(): String {
        return String.format(Locale.UK, "X: %d, Y: %d, type: %d, direction: %d, quality: %d",
                x, y, type, direction, quality)
    }
}
