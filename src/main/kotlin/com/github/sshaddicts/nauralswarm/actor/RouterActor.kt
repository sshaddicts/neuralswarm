package com.github.sshaddicts.nauralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.nauralswarm.actor.message.GetProcessor
import com.github.sshaddicts.nauralswarm.actor.message.GetStorage
import com.github.sshaddicts.nauralswarm.utils.akka.NeuralswarmActor
import kotlin.reflect.KClassifier

class RouterActor(private val actors: Map<KClassifier, ActorRef>) : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    override fun onReceive(message: Any?) = when (message) {
        is GetStorage -> sender tell actors[StorageActor::class]
        is GetProcessor -> sender tell actors[ImageProcessorActor::class]
        else -> log.error("illegal message: $message")
    }

    init {
        log.debug("${javaClass.simpleName} created.")
    }

    companion object {
        fun props(actors: Map<KClassifier, ActorRef>): Props = Props.create(RouterActor::class.java, actors)
    }
}