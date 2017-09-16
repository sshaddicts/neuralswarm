package com.github.sshaddicts.neuralswarm

import akka.actor.ActorSystem
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.neuralswarm.actor.Root
import org.opencv.core.Core
import java.util.logging.Level
import java.util.logging.Logger


fun main(argv: Array<String>) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    println(System.getProperty("java.library.path"))

    val mongoLogger = Logger.getLogger("com.mongodb")
    mongoLogger.level = Level.SEVERE

    val actorSystem = ActorSystem.create("system")
    Root.create(actorSystem)
}