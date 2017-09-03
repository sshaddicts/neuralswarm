package com.github.sshaddicts.nauralswarm.actor

import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.nauralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.ProcessedData


class ImageProcessorActor : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val mockResponseData = ProcessedData(
            listOf("foo" to 1.toDouble())
    )

    init {
        log.debug("${javaClass.simpleName} created.")
    }

    override fun onReceive(message: Any?) = when (message) {
        is ProcessImageRequest -> sender tell mockResponseData
        else -> log.error("illegal message: $message")
    }

    companion object {
        val props: Props
            get() = Props.create(ImageProcessorActor::class.java)
    }
}