package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.lucrecium.imageProcessing.ImageProcessor
import com.github.sshaddicts.lucrecium.neuralNetwork.TextRecognizer
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.ProcessedData
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.actor.message.GetStorage
import com.github.sshaddicts.neuralswarm.actor.message.GetUserIfExists
import com.github.sshaddicts.neuralswarm.actor.message.Save
import com.github.sshaddicts.neuralswarm.entity.User
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralswarm.utils.akka.ask
import kotlinx.coroutines.experimental.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream


class ImageProcessorActor : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val netowrk: InputStream

    private val router: Deferred<ActorRef> by lazy {
        async(CommonPool) {
            context().parent().ask(GetRouter()) as ActorRef
        }
    }

    init {
        netowrk = ByteArrayInputStream(File("/network").readBytes())
        netowrk.reset()
    }

    private fun getDataFromLucrecium(bytes: ByteArray, width: Int, height: Int): ProcessedData {

        val processor = ImageProcessor(bytes, width, height)
        val data = processor.findTextRegions(ImageProcessor.NO_MERGE)

        try {
            val recognizer = TextRecognizer(netowrk)
            return ProcessedData(recognizer.getText(data))
        } finally {
            netowrk.reset()
        }
    }

    override fun onReceive(message: Any?) {

        val sender = context.sender()

        when (message) {
            is ProcessImageRequest -> launch(context.dispatcher().asCoroutineDispatcher()) {

                val storage = router.await().ask(GetStorage()) as ActorRef

                val user = storage.ask(GetUserIfExists(message.token)) as User?

                sender tell if (user != null) {

                    val data = try {
                        getDataFromLucrecium(message.bytes, message.details.width, message.details.height)
                    } catch (e: Throwable) {
                        log.error(e, e.message)
                    }

                    if (data is ProcessedData) {

                        user.history.add(data)

                        storage tell Save(user)

                        log.debug("Processed and saved data for user ${user.name}")

                        data
                    } else {
                        log.error("Failed to process and save data for user ${user.name}")
                    }

                } else {
                    log.error("No user found: ${user?.name}")
                }

            }
        }
    }

    companion object {
        val props: Props
            get() = Props.create(ImageProcessorActor::class.java)
    }
}