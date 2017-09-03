package com.github.sshaddicts.nauralswarm.entity

import org.litote.kmongo.MongoId


data class Item (
        @MongoId
        val id: String? = null,
        val name: String,
        val price: Double
)