package com.github.sshaddicts.neuralswarm.utils.akka

import akka.actor.AbstractActor
import akka.actor.ActorRef

abstract class NeuralswarmActor : AbstractActor() {
    protected infix fun ActorRef.tell(message: Any?) = this.tell(message, self)
}
