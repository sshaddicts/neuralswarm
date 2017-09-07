package com.github.sshaddicts.neuralswarm.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.sshaddicts.neuralclient.data.ProcessedData
import org.apache.commons.lang3.StringUtils
import org.litote.kmongo.MongoId
import java.util.*


data class User (
        @MongoId
        val id: String? = null,
        val name: String,
        val hash: String,
        val registrationDate: Date,
        val tokenId: String = UUID.randomUUID().toString().replace("-", StringUtils.EMPTY),
        val history: MutableList<ProcessedData> = ArrayList()
) {
    @get:JsonIgnore
    val token: Token
        get() = Token(id!!, tokenId)
}