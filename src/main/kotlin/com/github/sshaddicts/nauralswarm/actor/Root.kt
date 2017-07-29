package com.github.sshaddicts.nauralswarm.actor

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedActor
import akka.event.Logging
import akka.event.LoggingAdapter

class Root : UntypedActor() {

    val log: LoggingAdapter = Logging.getLogger(context.system(), this)

    init {
        log.debug("Root actor has been started.")
    }

    override fun onReceive(message: Any?) {

    }

    companion object {
        fun create(system: ActorSystem): ActorRef
                = system.actorOf(Props.create(Root::class.java), "neuralswarm")
    }
}