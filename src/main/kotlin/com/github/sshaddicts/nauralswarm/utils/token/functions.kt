package com.github.sshaddicts.nauralswarm.utils.token

import com.github.sshaddicts.nauralswarm.entity.User
import java.math.BigInteger
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

fun User.validatePassword(password: String): Boolean =
        generateHash(password, this.registrationDate) == this.hash

fun generateHash(password: String, date: Date): String {
    val iterations = 1000
    val chars = password.toCharArray()
    val salt = date.asSalt()

    val spec = PBEKeySpec(chars, salt, iterations, 64 * 8)
    val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = skf.generateSecret(spec).encoded
    return iterations.toString() + ":" + salt.toHex() + ":" + hash.toHex()
}

private fun Date.asSalt() = this.toString().toByteArray()

private fun ByteArray.toHex(): String {
    val bi = BigInteger(1, this)

    val hex = bi.toString(16)

    val paddingLength = this.size * 2 - hex.length

    return if (paddingLength > 0) {
        String.format("%0" + paddingLength + "d", 0) + hex
    } else {
        hex
    }
}

