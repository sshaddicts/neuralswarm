package com.github.sshaddicts.neuralswarm.actor.message

import com.github.sshaddicts.neuralswarm.entity.Token
import com.github.sshaddicts.neuralswarm.entity.User

class GetRouter

class Save(val user: User)

class GetUserIfExists(val token: Token) {
    constructor(token: String) : this(Token.load(token))
}