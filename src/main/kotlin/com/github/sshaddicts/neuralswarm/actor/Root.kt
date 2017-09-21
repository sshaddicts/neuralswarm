package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.event.LoggingAdapter
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor

/**
 * Root actor of [ActorSystem]
 */
class Root : NeuralswarmActor() {
    override fun createReceive(): Receive = receiveBuilder()
            .match(GetRouter::class.java, {
                sender tell router
            })
            .build()

    private val log: LoggingAdapter = Logging.getLogger(context.system(), this)

    private val router: ActorRef = context.actorOf(RouterActor.props(), "router")

    init {
        log.debug("${javaClass.simpleName} created.")
    }

    companion object {
        fun create(system: ActorSystem): ActorRef
                = system.actorOf(Props.create(Root::class.java), "neuralswarm")
    }
}
