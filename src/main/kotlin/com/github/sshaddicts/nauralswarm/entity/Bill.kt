package com.github.sshaddicts.nauralswarm.entity

import org.litote.kmongo.MongoId


data class Bill (
        @MongoId
        val id: String? = null,
        val items: List<Item> = ArrayList()
)