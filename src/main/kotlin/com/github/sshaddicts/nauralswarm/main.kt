package com.github.sshaddicts.nauralswarm

import akka.actor.ActorSystem
import com.github.sshaddicts.nauralswarm.actor.Root

fun main(argv: Array<String>) {
    val actorSystem = ActorSystem.create("system")
    val root = Root.create(actorSystem)
}