package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import akka.routing.RoundRobinPool
import com.github.sshaddicts.neuralclient.data.AuthenticationRequest
import com.github.sshaddicts.neuralclient.data.HistoryRequest
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.RegistrationRequest
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.actor.message.GetUserIfExists
import com.github.sshaddicts.neuralswarm.actor.message.Save
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralswarm.utils.akka.config

/**
 * Actor that used for load balancing and routing messages between all actors.
 */

class RouterActor : NeuralswarmActor() {

    private val toStorage: (Any) -> Unit = { storage.tell(it, sender) }

    override fun createReceive(): Receive = receiveBuilder()

            .match(GetRouter::class.java, {
                sender tell self
            })

            .match(Save::class.java, toStorage)
            .match(GetUserIfExists::class.java, toStorage)
            .match(RegistrationRequest::class.java, toStorage)
            .match(AuthenticationRequest::class.java, toStorage)
            .match(HistoryRequest::class.java, toStorage)
            .match(ProcessImageRequest::class.java, {
                processor.tell(it, sender)
            })

            .build()

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)
    private val storage: ActorRef
    private val processor: ActorRef

    private val api: ActorRef = context.actorOf(ApiActor.props, "api")

    override fun preStart() {
        log.debug("${javaClass.simpleName} starting.")
    }

    init {
        log.debug("creating processor actors...")

        val processorActors = config.getInt("instances.processor")
        val storageActors = config.getInt("instances.storage")

        processor = context.actorOf(RoundRobinPool(processorActors).props(ImageProcessorActor.props))

        log.debug("creating storage actors...")

        storage = context.actorOf(RoundRobinPool(storageActors).props(StorageActor.props))

        log.debug("all actors ware created.")

        context.watch(api)
        context.watch(processor)
        context.watch(storage)
    }

    companion object {
        fun props(): Props = Props.create(RouterActor::class.java)
    }
}