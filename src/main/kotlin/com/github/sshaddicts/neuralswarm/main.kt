package com.github.sshaddicts.neuralswarm

import akka.actor.ActorSystem
import com.github.sshaddicts.neuralswarm.actor.Root
import org.opencv.core.Core

fun main(argv: Array<String>) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    val actorSystem = ActorSystem.create("system")
    Root.create(actorSystem)
}