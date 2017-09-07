package com.github.sshaddicts.neuralswarm

import akka.actor.ActorSystem
import com.github.sshaddicts.neuralswarm.actor.Root
import java.util.logging.Level
import java.util.logging.Logger


fun main(argv: Array<String>) {

    val mongoLogger = Logger.getLogger("com.mongodb")
    mongoLogger.level = Level.SEVERE

    val actorSystem = ActorSystem.create("system")
    Root.create(actorSystem)
}