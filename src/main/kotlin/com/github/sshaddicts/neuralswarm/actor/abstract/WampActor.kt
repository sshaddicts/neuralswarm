package com.github.sshaddicts.neuralswarm.actor.abstract

import akka.event.DiagnosticLoggingAdapter
import com.github.sshaddicts.neuralswarm.utils.akka.NeuralswarmActor
import com.github.sshaddicts.neuralswarm.utils.akka.config
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.deferred
import rx.Observable
import ws.wamp.jawampa.WampClient
import ws.wamp.jawampa.WampClientBuilder
import ws.wamp.jawampa.WampSerialization
import ws.wamp.jawampa.connection.IWampClientConnectionConfig
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider
import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig
import java.util.concurrent.TimeUnit

/**
 * Actor that contains [WampClient] and communicate with WAMP realm through it.
 */
abstract class WampActor(uri: String, realm: String) : NeuralswarmActor() {

    abstract val log: DiagnosticLoggingAdapter

    private val provider = NettyWampClientConnectorProvider()

    private val defer: Deferred<WampClient, Throwable> = deferred()

    protected fun connection(closure: (WampClient) -> Unit) = defer.promise.success(closure)

    private val wampConfig: IWampClientConnectionConfig = NettyWampConnectionConfig
            .Builder()
            .withMaxFramePayloadLength(config.getInt("wamp.max-frame-payload"))
            .build()

    private val builder: WampClientBuilder = WampClientBuilder()
            .withConnectorProvider(provider)
            .withConnectionConfiguration(wampConfig)
            .withCloseOnErrors(false)
            .withUri(uri)
            .withRealm(realm)
            .withInfiniteReconnects()
            .withReconnectInterval(config.getInt("wamp.reconnect-interval"), TimeUnit.SECONDS)
            .withSerializations(arrayOf(WampSerialization.MessagePack))

    protected fun startWamp(): Observable<WampClient> {
        val wamp = builder.build()

        wamp.statusChanged().subscribe {
            log.debug("WAMP status: $it")
        }

        val obs = wamp.statusChanged().filter { it is WampClient.ConnectedState }
                .map {
                    defer.resolve(wamp)
                    wamp
                }


        wamp.open()

        return obs
    }

    protected fun stopWamp() = connection { it.close() }
}