plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.face.basebiosdk"
}

dependencies {
    api(project(":infra:template-protection")) // TODO PoC
}
