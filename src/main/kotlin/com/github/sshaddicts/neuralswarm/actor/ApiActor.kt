package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.sshaddicts.neuralclient.data.AuthenticationRequest
import com.github.sshaddicts.neuralclient.data.HistoryRequest
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.RegistrationRequest
import com.github.sshaddicts.neuralswarm.actor.abstract.WampActor
import com.github.sshaddicts.neuralswarm.actor.message.GetProcessor
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.actor.message.GetStorage
import com.github.sshaddicts.neuralswarm.utils.akka.ask
import com.github.sshaddicts.neuralswarm.utils.serialization.mapper
import kotlinx.coroutines.experimental.*
import ws.wamp.jawampa.ApplicationError
import ws.wamp.jawampa.Request
import ws.wamp.jawampa.WampClient

class ApiActor : WampActor("ws://crossbar:7778/api", "realm1") {

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
        val storage = router.await().ask(GetStorage()) as ActorRef
        val response = storage ask mapper.treeToValue<HistoryRequest>(request.keywordArguments())

        log.debug("get history: sending reply.")

        if (response is Unit) {
            request.replyError(ApplicationError.INVALID_ARGUMENT, "failed to get history")
        } else {
            request.reply(mapper.createArrayNode(), mapper.valueToTree(response))
        }
    }

    private fun userRegister(request: Request) = launch(context.dispatcher().asCoroutineDispatcher()) {
        val storage = router.await().ask(GetStorage()) as ActorRef
        val response = storage ask mapper.treeToValue<RegistrationRequest>(request.keywordArguments())

        log.debug("registration: sending reply.")
        if (response is Unit) {
            request.replyError(ApplicationError.INVALID_ARGUMENT, "registration failed")
        } else {
            request.reply(response)
        }
    }

    private fun processImage(request: Request) = launch(context.dispatcher().asCoroutineDispatcher()) {
        val processor = router.await().ask(GetProcessor()) as ActorRef
        val response = processor ask mapper.treeToValue<ProcessImageRequest>(request.keywordArguments())


        log.debug("process image: sending reply.")
        if (response is Unit) {
            request.replyError(ApplicationError.INVALID_ARGUMENT, "failed to process image")
        } else {
            request.reply(mapper.createArrayNode(), mapper.valueToTree(response))
        }
    }

    private fun userAuth(request: Request) = launch(context.dispatcher().asCoroutineDispatcher()) {
        val storage = router.await().ask(GetStorage()) as ActorRef
        val response = storage ask mapper.treeToValue<AuthenticationRequest>(request.keywordArguments())

        log.debug("auth: sending reply.")
        if (response is Unit) {
            request.replyError(ApplicationError.AUTHORIZATION_FAILED)
        } else {
            request.reply(response)
        }
    }

    override fun onReceive(message: Any?) {

    }

    companion object {
        val props: Props
            get() = Props.create(ApiActor::class.java)
    }
}