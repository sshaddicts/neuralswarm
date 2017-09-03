package com.github.sshaddicts.nauralswarm.actor

import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.nauralswarm.entity.User
import com.github.sshaddicts.nauralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.nauralswarm.utils.token.generateHash
import com.github.sshaddicts.nauralswarm.utils.token.validatePassword
import com.github.sshaddicts.neuralclient.data.AuthenticationRequest
import com.github.sshaddicts.neuralclient.data.RegistrationRequest
import org.litote.kmongo.*
import java.util.*

class StorageActor : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val client = KMongo.createClient("mongo")
    private val database = client.getDatabase("neuralswarm")
    private val collection = database.getCollection<User>()

    init {
        log.debug("${javaClass.simpleName} created.")
    }

    override fun onReceive(message: Any?) = when (message) {
        is AuthenticationRequest -> {

            val user = collection.findOne("{name: '${message.username}'}")

            sender tell if (user != null && user.validatePassword(message.password)) {

                log.debug("User: ${message.username} - successful authentication.")

                user.token.toString()
            } else {

                log.debug("User: ${message.username} - authentication failed.")

                false
            }

        }

        is RegistrationRequest -> {

            val notExists = collection.count("{name: '${message.username}'}") == 0.toLong()

            sender tell if (notExists) {
                val date = Date()

                val user = User(
                        name = message.username,
                        hash = generateHash(message.password, date),
                        registrationDate = date
                )

                collection.save(user)

                log.debug("User: ${message.username} - successful registration.")
                log.debug("User: ${message.username} now have a token: ${user.token}")

                user.token.toString()
            } else {
                log.debug("User: ${message.username} - registration failed.")

                false
            }

        }

        else -> log.error("invalid message: $message")
    }

    companion object {
        val props: Props
            get() = Props.create(StorageActor::class.java)
    }
}