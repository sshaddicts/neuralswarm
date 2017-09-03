package com.github.sshaddicts.nauralswarm.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.litote.kmongo.MongoId
import java.util.*


data class User (
        @MongoId
        val id: String? = null,
        val name: String,
        val hash: String,
        val registrationDate: Date,
        private var tokenId: String = UUID.randomUUID().toString().replace("-", "kek"),
        val bills: List<Bill> = ArrayList()
) {
    @get:JsonIgnore
    val token: Token
        get() = Token(id!!, tokenId)
}