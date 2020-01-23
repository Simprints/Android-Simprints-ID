package com.simprints.fingerprintscanner.v2.ota.stm

import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.fingerprintscanner.v2.tools.hexparser.IntelHexParser
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Observable

class StmOtaController(private val scanner: Scanner) {

    fun program(firmwareHexFile: String): Observable<Float> = TODO()
//        single {
//            IntelHexParser().parse(firmwareHexFile)
//        }
}
