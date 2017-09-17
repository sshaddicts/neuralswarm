package com.github.sshaddicts.neuralswarm.actor

import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.neuralswarm.actor.message.GetUserIfExists
import com.github.sshaddicts.neuralswarm.actor.message.Save
import com.github.sshaddicts.neuralswarm.entity.Token
import com.github.sshaddicts.neuralswarm.entity.User
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralswarm.utils.token.generateHash
import com.github.sshaddicts.neuralswarm.utils.token.validatePassword
import com.github.sshaddicts.neuralclient.data.AuthenticationRequest
import com.github.sshaddicts.neuralclient.data.History
import com.github.sshaddicts.neuralclient.data.HistoryRequest
import com.github.sshaddicts.neuralclient.data.RegistrationRequest
import com.mongodb.client.MongoCollection
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

    override fun postStop() {
        log.debug("Storage actor is DOWN.")
    }

    override fun preStart() {
        log.debug("Storage actor is going up...")
    }

    override fun onReceive(message: Any?) = when (message) {

        is HistoryRequest -> {
            sender tell try {

                History(collection.findOne(Token.load(message.token)).history)

            } catch (e: Throwable) {
                log.error(e.message)
            }
        }

        is GetUserIfExists -> {

            sender tell try {

                collection.findOne(message.token)

            } catch (e: Throwable) {
                log.error(e.message)
            }
        }

        is Save -> {

            log.debug("Saving user: ${message.user.name}")
            collection.save(message.user)

        }

        is AuthenticationRequest -> {

            val user = collection.findOne("{name: '${message.username}'}")

            sender tell if (user != null && user.validatePassword(message.password)) {

                log.debug("User: ${message.username} - successful authentication.")

                user.token.toString()
            } else {

                log.debug("User: ${message.username} - authentication failed.")

                Unit
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

                collection.insertOne(user)

                log.debug("User: ${message.username} - successful registration.")
                log.debug("User: ${message.username} now have a token: ${user.token}")

                user.token.toString()
            } else {
                log.debug("User: ${message.username} - registration failed.")

                Unit
            }

        }

        else -> log.error("invalid message: $message")
    }

    private fun MongoCollection<User>.findOne(token: Token): User {
        val user = this.findOneById(token.userId) ?: throw RuntimeException("No user with id ${token.userId} found.")

        log.debug("token old: ${user.token.tokenId}")
        log.debug("token new: ${token.tokenId}")

        if (user.token.tokenId != token.tokenId) {
            throw RuntimeException("Token is not valid")
        }

        return user
    }

    companion object {
        val props: Props
            get() = Props.create(StorageActor::class.java)
    }
}