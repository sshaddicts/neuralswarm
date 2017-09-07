package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.event.LoggingAdapter
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import kotlin.reflect.KClassifier

class Root : NeuralswarmActor() {
    private val log: LoggingAdapter = Logging.getLogger(context.system(), this)

    private val actors: MutableMap<KClassifier, ActorRef> = mutableMapOf(
            ApiActor::class to context.actorOf(ApiActor.props, "api"),
            StorageActor::class to context.actorOf(StorageActor.props, "storage"),
            ImageProcessorActor::class to context.actorOf(ImageProcessorActor.props, "processor")
    )

    init {
        log.debug("${javaClass.simpleName} created.")
    }

    override fun preStart() {

        actors[RouterActor::class] = context.actorOf(RouterActor.props(actors), "router")

        log.info("Root actor has been created.")
    }

    override fun onReceive(message: Any?) = when (message) {
        is GetRouter -> sender tell actors[RouterActor::class]
        else -> log.error("illegal message: $message")
    }

    companion object {
        fun create(system: ActorSystem): ActorRef
                = system.actorOf(Props.create(Root::class.java), "neuralswarm")
    }
}