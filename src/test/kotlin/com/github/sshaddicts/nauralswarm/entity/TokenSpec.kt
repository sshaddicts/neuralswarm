package com.github.sshaddicts.nauralswarm.entity

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals


object TokenSpec : Spek({
    describe("user tokenId") {
        it("should works properly") {

            val token = Token(
                    tokenId = "test",
                    userId = "test"
            )

            val encrypted = token.toString()

            val decrypted = Token.load(encrypted)

            assertEquals(token.tokenId, decrypted.tokenId)
            assertEquals(token.userId, decrypted.userId)
        }
    }
})