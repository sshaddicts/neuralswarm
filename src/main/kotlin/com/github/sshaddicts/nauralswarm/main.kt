package com.github.sshaddicts.nauralswarm

import akka.actor.ActorSystem
import com.github.sshaddicts.nauralswarm.actor.Root
import java.util.logging.Level
import java.util.logging.Logger


fun main(argv: Array<String>) {

    val mongoLogger = Logger.getLogger("org.mongodb.driver")
    mongoLogger.level = Level.SEVERE

    val actorSystem = ActorSystem.create("system")
    Root.create(actorSystem)
}