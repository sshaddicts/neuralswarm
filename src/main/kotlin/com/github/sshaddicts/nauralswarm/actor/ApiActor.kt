package com.github.sshaddicts.nauralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.sshaddicts.nauralswarm.actor.abstract.WampActor
import com.github.sshaddicts.nauralswarm.actor.message.GetProcessor
import com.github.sshaddicts.nauralswarm.actor.message.GetRouter
import com.github.sshaddicts.nauralswarm.actor.message.GetStorage
import com.github.sshaddicts.nauralswarm.utils.akka.ask
import com.github.sshaddicts.nauralswarm.utils.serialization.mapper
import com.github.sshaddicts.neuralclient.data.AuthenticationRequest
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.RegistrationRequest
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
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
        client.registerProcedure("history.get").subscribe({}, errorHandler)

        log.debug("registered procedures")
    }

    private fun userRegister(request: Request) = launch(CommonPool) {
        val storage = router.await().ask(GetStorage()) as ActorRef
        val response = storage ask mapper.treeToValue<RegistrationRequest>(request.keywordArguments())

        if (response is Boolean) {
            request.replyError(ApplicationError.AUTHORIZATION_FAILED)
        } else {
            request.reply(response)
        }
    }

    private fun processImage(request: Request) = launch(CommonPool) {
        val processor = router.await().ask(GetProcessor()) as ActorRef
        val response = processor ask mapper.treeToValue<ProcessImageRequest>(request.keywordArguments())

        request.reply(mapper.createArrayNode(), mapper.valueToTree(response))
    }

    private fun userAuth(request: Request) = launch(CommonPool) {
        val storage = router.await().ask(GetStorage()) as ActorRef
        val response = storage ask mapper.treeToValue<AuthenticationRequest>(request.keywordArguments())

        if (response is Boolean) {
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