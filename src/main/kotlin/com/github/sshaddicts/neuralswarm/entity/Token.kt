package com.github.sshaddicts.neuralswarm.entity

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.sshaddicts.neuralswarm.utils.serialization.mapper
import com.github.sshaddicts.neuralswarm.utils.token.Aes

/**
 * Custom JWT implementation with AES256
 */
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