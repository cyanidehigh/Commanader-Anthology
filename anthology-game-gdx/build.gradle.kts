plugins {
    `java-library`
}

dependencies {
    api(project(":anthology-core"))
    implementation(libs.gdx)
    implementation(libs.gdx.backend.lwjgl3)
    implementation(libs.gdx.freetype)
    implementation(libs.sqlite.jdbc)
    runtimeOnly(libs.slf4j.nop)
    runtimeOnly(libs.gdx.platform.desktop) {
        artifact {
            classifier = "natives-desktop"
        }
    }
    runtimeOnly(libs.gdx.freetype.platform) {
        artifact {
            classifier = "natives-desktop"
        }
    }
}

tasks.register<JavaExec>("launchGdxSmokeWindow") {
    group = "verification"
    description = "Launches the libGDX Commander Anthology game smoke window."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.commanderanthology.game.gdx.GdxGameSmokeLauncher")
}

tasks.register<JavaExec>("gdxLauncherSmokeTest") {
    group = "verification"
    description = "Runs the libGDX launcher boundary smoke test without opening a window."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.game.gdx.GdxLauncherSmokeTest")
}
