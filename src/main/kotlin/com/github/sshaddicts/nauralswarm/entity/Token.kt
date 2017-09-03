package com.github.sshaddicts.nauralswarm.entity

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.sshaddicts.nauralswarm.utils.serialization.mapper
import com.github.sshaddicts.nauralswarm.utils.token.Aes

data class Token(
        val userId: String,
        val tokenId: String
) {
    override fun toString() = Aes.encrypt(mapper.writeValueAsString(this), privateKey)

    companion object {
        private val privateKey = "0LHQvtC00Y8g0L/RltGB0Y7QvQ=="

        fun load(string: String): Token = mapper.readValue(Aes.decrypt(string, privateKey))
    }
}