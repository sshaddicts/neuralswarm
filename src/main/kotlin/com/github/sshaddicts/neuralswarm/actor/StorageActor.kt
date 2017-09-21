package com.github.sshaddicts.neuralswarm.actor

import akka.actor.Props
import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging
import com.github.sshaddicts.neuralclient.data.AuthenticationRequest
import com.github.sshaddicts.neuralclient.data.History
import com.github.sshaddicts.neuralclient.data.HistoryRequest
import com.github.sshaddicts.neuralclient.data.RegistrationRequest
import com.github.sshaddicts.neuralswarm.actor.message.GetUserIfExists
import com.github.sshaddicts.neuralswarm.actor.message.Save
import com.github.sshaddicts.neuralswarm.entity.Token
import com.github.sshaddicts.neuralswarm.entity.User
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralswarm.utils.token.generateHash
import com.github.sshaddicts.neuralswarm.utils.token.validatePassword
import com.mongodb.client.MongoCollection
import org.litote.kmongo.*
import java.util.*

/**
 * Actor that communicate with data storage.
 *
 * @see HistoryRequest
 * @see GetUserIfExists
 * @see Save
 * @see AuthenticationRequest
 * @see RegistrationRequest
 */
class StorageActor : NeuralswarmActor() {

    private val log: DiagnosticLoggingAdapter = Logging.getLogger(this)

    private val client = KMongo.createClient("mongo")
    private val database = client.getDatabase("neuralswarm")
    private val collection = database.getCollection<User>()

    init {
        log.debug("${javaClass.simpleName} created.")
    }

    override fun createReceive(): Receive = receiveBuilder()
            .match(HistoryRequest::class.java, { message ->
                sender tell try {

                    History(collection.findOne(Token.load(message.token)).history)

                } catch (e: Throwable) {
                    log.error(e.message)
                }
            })

            .match(GetUserIfExists::class.java, { message ->
                sender tell try {

                    collection.findOne(message.token)

                } catch (e: Throwable) {
                    log.error(e.message)
                }
            })

            .match(Save::class.java, { message ->
                log.debug("Saving user: ${message.user.name}")
                collection.save(message.user)
            })

            .match(AuthenticationRequest::class.java, { (username, password) ->
                val user = collection.findOne("{name: '$username'}")

                sender tell if (user != null && user.validatePassword(password)) {

                    log.debug("User: $username - successful authentication.")

                    user.token.toString()
                } else {

                    log.debug("User: $username - authentication failed.")

                    Unit
                }
            })

            .match(RegistrationRequest::class.java, { (username, password) ->
                val notExists = collection.count("{name: '$username'}") == 0.toLong()

                sender tell if (notExists) {
                    val date = Date()

                    val user = User(
                            name = username,
                            hash = generateHash(password, date),
                            registrationDate = date
                    )

                    collection.insertOne(user)

                    log.debug("User: $username - successful registration.")
                    log.debug("User: $username now have a token: ${user.token}")

                    user.token.toString()
                } else {
                    log.debug("User: $username - registration failed.")

                    Unit
                }
            })

            .build()

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