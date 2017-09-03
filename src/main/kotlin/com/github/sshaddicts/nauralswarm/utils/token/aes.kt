package com.github.sshaddicts.nauralswarm.utils.token

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Aes {
    fun encrypt(strToEncrypt: String, secret: String): String {

        val secretKey = secretize(secret)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8"))))

    }

    fun decrypt(strToDecrypt: String, secret: String): String {

        val secretKey = secretize(secret)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))

    }

    private fun secretize(key: String): SecretKeySpec = SecretKeySpec(
            MessageDigest.getInstance("SHA-1").digest(
                    key.toByteArray(charset("UTF-8"))
            ).copyOf(16),
            "AES"
    )
}