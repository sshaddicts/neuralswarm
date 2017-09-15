package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.kittinunf.fuel.Fuel
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
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class ImageProcessorActor : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val router: Deferred<ActorRef> by lazy {
        async(CommonPool) {
            context().parent().ask(GetRouter()) as ActorRef
        }
    }

    private fun getDataFromLucrecium(bytes: ByteArray, width: Int, height: Int): ProcessedData {

        val processor = ImageProcessor(bytes, width, height)

        val data = processor.findTextRegions(ImageProcessor.NO_MERGE)
        val recognizer = TextRecognizer("netFile.nf")

        return ProcessedData(recognizer.getText(data))
    }

    init {
        log.debug("${javaClass.simpleName} created.")

        if(Files.exists(Paths.get("netFile.nf"))){
            Fuel.download("http://httpbin.org/bytes/32768").destination { response, url ->
                File.createTempFile("temp", ".tmp")
            }.response { req, res, result ->

            }
        }
    }

    override fun onReceive(message: Any?) {

        val sender = context.sender()

        when (message) {
            is ProcessImageRequest -> launch(context.dispatcher().asCoroutineDispatcher()) {

                val storage = router.await().ask(GetStorage()) as ActorRef

                val user = storage.ask(GetUserIfExists(message.token)) as User?

                sender tell if (user != null) {

                    val data = getDataFromLucrecium(message.bytes, message.details.width, message.details.height)

                    user.history.add(data)

                    storage tell Save(user)

                    log.debug("Processed and saved data for user ${user.name}")

                    data
                } else {
                    log.error("Failed to process and save data for user ${user?.name}")
                }

            }
        }
    }

    companion object {
        val props: Props
            get() = Props.create(ImageProcessorActor::class.java)
    }
}