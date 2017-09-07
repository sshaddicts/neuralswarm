package com.github.sshaddicts.neuralswarm.utils.akka

import akka.actor.ActorRef
import akka.actor.UntypedActor

abstract class NeuralswarmActor : UntypedActor() {
    protected infix fun ActorRef.tell(message: Any?) = this.tell(message, self)
}
