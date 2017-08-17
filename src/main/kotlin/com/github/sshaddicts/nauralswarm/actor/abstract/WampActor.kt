package com.github.sshaddicts.nauralswarm.actor.abstract

import akka.actor.UntypedActor
import akka.event.DiagnosticLoggingAdapter
import com.github.sshaddicts.nauralswarm.utils.akka.config
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.deferred
import ws.wamp.jawampa.WampClient
import ws.wamp.jawampa.WampClientBuilder
import ws.wamp.jawampa.WampSerialization
import ws.wamp.jawampa.connection.IWampClientConnectionConfig
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider
import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig
import java.util.concurrent.TimeUnit


abstract class WampActor(uri: String, realm: String) : UntypedActor() {

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

    protected fun startWamp() {
        try {
            val wamp = builder.build()

            wamp.statusChanged()
                    .subscribe { status ->
                        log.debug(status.toString())
                        when (status) {
                            is WampClient.ConnectedState ->
                                defer.resolve(wamp)
                        }
                    }

            wamp.open()
        } catch (e: Throwable) {
            log.error(e, "Wamp build/connection failed!")
        }
    }

    protected fun stopWamp() = connection { it.close() }
}