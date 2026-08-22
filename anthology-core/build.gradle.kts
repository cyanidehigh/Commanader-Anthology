plugins {
    `java-library`
}

tasks.register<JavaExec>("gameFoundationSmokeTest") {
    group = "verification"
    description = "Runs the Commander game foundation smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.GameFoundationSmokeTest")
}

tasks.register<JavaExec>("basicAiGameplaySmokeTest") {
    group = "verification"
    description = "Runs the basic AI legal-gameplay smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.BasicAiGameplaySmokeTest")
}

tasks.register<JavaExec>("realCardFixtureSmokeTest") {
    group = "verification"
    description = "Runs the real-card keyword fixture smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.RealCardFixtureSmokeTest")
}

tasks.register<JavaExec>("stateBasedActionsSmokeTest") {
    group = "verification"
    description = "Runs the state-based actions smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.StateBasedActionsSmokeTest")
}

tasks.register<JavaExec>("startingGameSmokeTest") {
    group = "verification"
    description = "Runs the Commander starting-game smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.StartingGameSmokeTest")
}

tasks.register<JavaExec>("timingAndPrioritySmokeTest") {
    group = "verification"
    description = "Runs the timing and priority smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.TimingAndPrioritySmokeTest")
}

tasks.register<JavaExec>("manaPoolSmokeTest") {
    group = "verification"
    description = "Runs the mana pool smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.ManaPoolSmokeTest")
}

tasks.register<JavaExec>("manaCostPaymentSmokeTest") {
    group = "verification"
    description = "Runs the mana cost parser/payment smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.core.commandersim.ManaCostPaymentSmokeTest")
}
