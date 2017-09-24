package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.lucrecium.imageProcessing.ImageProcessor
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.ProcessedData
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.actor.message.GetUserIfExists
import com.github.sshaddicts.neuralswarm.actor.message.Save
import com.github.sshaddicts.neuralswarm.entity.User
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralswarm.utils.akka.ask
import com.github.sshaddicts.neuralswarm.utils.neural.processor
import com.github.sshaddicts.neuralswarm.utils.neural.recognizer
import kotlinx.coroutines.experimental.*
import java.io.File
import java.util.*

/**
 * Actor that used for processing images.
 *
 * @see ProcessImageRequest
 */
class ImageProcessorActor : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val router: Deferred<ActorRef> by lazy {
        async(CommonPool) {
            context().parent().ask(GetRouter()) as ActorRef
        }
    }

    private fun processData(bytes: ByteArray): Pair<ProcessedData, ByteArray> {

        val file = File("/varimg/${UUID.randomUUID().toString().replace("-", "")}")

        file.writeBytes(bytes)

        log.debug("created temporary file: ${file.absolutePath}")

        try {
            val data = processor.findTextRegions(file.absolutePath, ImageProcessor.NO_MERGE, true)

            log.debug("found ${data.chars.size} elements")

            val response = recognizer.recognize(data.chars, labels)

            log.debug("response has ${response.size} entries")

            return Pair(ProcessedData(response), data.overlay)
        } finally {
            file.delete()
            log.debug("deleted temporary file: ${file.absolutePath}")
        }
    }

    override fun preStart() {
        log.debug("${javaClass.simpleName} starting.")
    }

    override fun createReceive(): Receive = receiveBuilder()

            .match(ProcessImageRequest::class.java, {
                log.debug("handling process image request...")
                handleRequest(it, sender)
            })

            .build()

    private fun handleRequest(message: ProcessImageRequest, sender: ActorRef) = launch(context.dispatcher().asCoroutineDispatcher()) {

        if (message.isAnonymous) {
            val pair = try {
                processData(message.bytes)
            } catch (e: Throwable) {
                log.error(e, e.message)
            }

            sender tell if (pair is Pair<*, *>) {
                log.debug("Processed and saved data for anonymous user.")

                pair
            } else {
                log.error("Failed to process and save data for anonymous user.")
            }

        } else {

            val user = router.await().ask(GetUserIfExists(message.token)) as User?

            sender tell if (user != null) {

                val pair = try {
                    processData(message.bytes)
                } catch (e: Throwable) {
                    log.error(e, e.message)
                }

                if (pair is Pair<*, *>) {

                    user.history.add(pair.first as ProcessedData)

                    router.await() tell Save(user)

                    log.debug("Processed and saved data for user ${user.name}")

                    pair
                } else {
                    log.error("Failed to process and save data for user ${user.name}")
                }
            } else {
                log.error("No user found: ${user?.name}")
            }
        }
    }

    companion object {
        val props: Props
            get() = Props.create(ImageProcessorActor::class.java)

        val labels: List<String> = listOf(
        "0","1","2","3","4","5",
        "6","7","8","9","dot","slash",
        "Є","І","Ї","А","Б","В",
        "Г","Д","Е","Ж","З","И",
        "Й","К","Л","М","Н","О",
        "П","Р","С","Т","У","Ф",
        "Х","Ц","Ч","Ш","Щ","Ь",
        "Ю","Я",
        "а","б","в","г","д","е",
        "ж","з","и","й","к","л",
        "м","н","о","п","р","с",
        "т","у","ф","х","ц","ч",
        "ш","щ","ь","э","ю","я",
        "є","і","ї","ґ"
        )
    }
}