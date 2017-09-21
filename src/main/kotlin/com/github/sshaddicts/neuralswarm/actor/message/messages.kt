package com.github.sshaddicts.neuralswarm.actor.message

import com.github.sshaddicts.neuralswarm.entity.Token
import com.github.sshaddicts.neuralswarm.entity.User

/**
 * Request to get a router actor
 */
class GetRouter

/**
 * Request to save user data.
 */
class Save(val user: User)

/**
 * Request to get user of user with given token exists.
 */
class GetUserIfExists(val token: Token) {
    constructor(token: String) : this(Token.load(token))
}