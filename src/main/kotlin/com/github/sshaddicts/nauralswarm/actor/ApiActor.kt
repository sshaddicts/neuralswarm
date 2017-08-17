package com.github.sshaddicts.nauralswarm.actor

import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.nauralswarm.actor.abstract.WampActor
import com.github.sshaddicts.neuralclient.ProcessedData
import java.util.*


class ApiActor : WampActor("ws://localhost:7778/api", "realm1") {

    override val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    override fun postStop() {
        stopWamp()
    }

    init {
        startWamp()
        registerProcedures()
    }


    private fun registerProcedures() = connection {
        log.debug("registering RPC")

        it.registerProcedure("process.image").subscribe { request ->

            try {
                val encoded: String = request.arguments().first().toString()
                val bytes = Base64.getMimeDecoder().decode(encoded)

                request.reply(ProcessedData(mapOf(
                        "size" to bytes.size.toString()
                )))
            } catch (e: Throwable) {
                log.error(e, e.message)
            }
        }
    }

    override fun onReceive(message: Any?) {

    }

    companion object {
        fun props(): Props = Props.create(ApiActor::class.java)
    }
}