package com.github.sshaddicts.nauralswarm.utils.wamp

import com.github.sshaddicts.nauralswarm.utils.serialization.mapper
import ws.wamp.jawampa.Request
import ws.wamp.jawampa.WampClient


fun WampClient.publishObject(topic: String, item: Any) =
        this.publish(topic, mapper.createArrayNode(), mapper.valueToTree(item))

fun Request.replyWithObject(item: Any) =
        this.reply(mapper.createArrayNode(), mapper.valueToTree(item))