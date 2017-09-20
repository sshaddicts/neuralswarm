package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.sshaddicts.neuralclient.data.*
import com.github.sshaddicts.neuralswarm.actor.abstract.WampActor
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.utils.akka.ask
import com.github.sshaddicts.neuralswarm.utils.serialization.mapper
import kotlinx.coroutines.experimental.*
import org.apache.commons.net.util.Base64
import ws.wamp.jawampa.ApplicationError
import ws.wamp.jawampa.Request
import ws.wamp.jawampa.WampClient

class ApiActor : WampActor("ws://crossbar:6668", "api") {
    override fun createReceive(): Receive = receiveBuilder().build()

    private val errorHandler: (Throwable) -> Unit = { log.error(it, it.message) }

    override val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val router: Deferred<ActorRef> by lazy {
        async(CommonPool) {
            context().parent().ask(GetRouter()) as ActorRef
        }
    }

    override fun postStop() {
        stopWamp()
    }

    init {
        startWamp().subscribe {
            log.debug("ApiActor connected to the realm.")
            registerProcedures(it)
        }

        log.debug("${javaClass.simpleName} created.")
    }

    private fun registerProcedures(client: WampClient) {

        log.debug("registering procedures...")

        client.registerProcedure("user.auth").subscribe({ userAuth(it) }, errorHandler)
        client.registerProcedure("process.image").subscribe({ processImage(it) }, errorHandler)
        client.registerProcedure("user.register").subscribe({ userRegister(it) }, errorHandler)
        client.registerProcedure("history.get").subscribe({ getHistory(it) }, errorHandler)

        log.debug("registered procedures")
    }

    private fun getHistory(request: Request) = launch(context.dispatcher().asCoroutineDispatcher()) {
        val response = router.await() ask mapper.treeToValue<HistoryRequest>(request.keywordArguments())

        log.debug("get history: sending reply.")

        if (response is Unit) {
            request.replyError(ApplicationError.INVALID_ARGUMENT, "failed to get history")
        } else {
            request.reply(mapper.createArrayNode(), mapper.valueToTree(response))
        }
    }

    private fun userRegister(request: Request) = launch(context.dispatcher().asCoroutineDispatcher()) {
        val response = router.await() ask mapper.treeToValue<RegistrationRequest>(request.keywordArguments())

        log.debug("registration: sending reply.")
        if (response is Unit) {
            request.replyError(ApplicationError.INVALID_ARGUMENT, "registration failed")
        } else {
            request.reply(response)
        }
    }

    private fun processImage(request: Request) = launch(context.dispatcher().asCoroutineDispatcher()) {
        val response = router.await() ask mapper.treeToValue<ProcessImageRequest>(request.keywordArguments())

        log.debug("process image: sending reply.")

        when (response) {
            is Unit -> request.replyError(ApplicationError.INVALID_ARGUMENT, "failed to process image")
            is Pair<*, *> -> request.reply(
                    mapper.valueToTree(arrayOf(Base64.encodeBase64String(response.second as ByteArray))),
                    mapper.valueToTree(response.first as ProcessedData)
            )
            else -> request.replyError(ApplicationError.TRANSPORT_CLOSED, "internal error")
        }
    }

    private fun userAuth(request: Request) = launch(context.dispatcher().asCoroutineDispatcher()) {
        val response = router.await() ask mapper.treeToValue<AuthenticationRequest>(request.keywordArguments())

        log.debug("auth: sending reply.")
        if (response is Unit) {
            request.replyError(ApplicationError.AUTHORIZATION_FAILED)
        } else {
            request.reply(response)
        }
    }

    companion object {
        val props: Props
            get() = Props.create(ApiActor::class.java)
    }
}