package com.simprints.fingerprint.infra.scanner.exceptions

open class ScannerException(): RuntimeException(){
    constructor(message: String) : this()
    constructor(throwable: Throwable) : this()

}
