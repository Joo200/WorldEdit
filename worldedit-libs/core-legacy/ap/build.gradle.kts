plugins {
    id("buildlogic.libs")
}

dependencies {
    // These are here because they use net.kyori:text-api -- so they need to be relocated too
    "shade"(libs.piston-legacy.coreAp.annotations)
    "shade"(libs.piston-legacy.coreAp.processor)
}
