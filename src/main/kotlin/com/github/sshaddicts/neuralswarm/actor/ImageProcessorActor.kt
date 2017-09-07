package com.github.sshaddicts.neuralswarm.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.ProcessedData
import com.github.sshaddicts.neuralswarm.actor.message.GetRouter
import com.github.sshaddicts.neuralswarm.actor.message.GetStorage
import com.github.sshaddicts.neuralswarm.actor.message.GetUserIfExists
import com.github.sshaddicts.neuralswarm.actor.message.Save
import com.github.sshaddicts.neuralswarm.entity.User
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralswarm.utils.akka.ask
import com.github.sshaddicts.neuralswarm.utils.serialization.mapper
import kotlinx.coroutines.experimental.*


class ImageProcessorActor : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val router: Deferred<ActorRef> by lazy {
        async(CommonPool) {
            context().parent().ask(GetRouter()) as ActorRef
        }
    }

    private fun getDataFromLucrecium(bytes: ByteArray): ProcessedData {
        val node = mapper.createObjectNode()

        node.put("name", "суміш ароматична \"Сметана та зелень\"")
        node.put("price", 15.99)

        return ProcessedData(listOf(node))
    }

    init {
        log.debug("${javaClass.simpleName} created.")
    }

    override fun onReceive(message: Any?) {

        val sender = context.sender()

        when (message) {
            is ProcessImageRequest -> launch(context.dispatcher().asCoroutineDispatcher()) {

                val storage = router.await().ask(GetStorage()) as ActorRef

                val user = storage.ask(GetUserIfExists(message.token)) as User?

                sender tell if (user != null) {

                    val data = getDataFromLucrecium(message.bytes)

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