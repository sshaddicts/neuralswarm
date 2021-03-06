package com.github.sshaddicts.neuralswarm.utils.akka

import akka.actor.ActorRef
import akka.pattern.Patterns
import kotlinx.coroutines.experimental.future.await
import scala.compat.java8.FutureConverters


suspend infix fun ActorRef.ask(message: Any): Any =
        FutureConverters.toJava(Patterns.ask(this, message, 5000000))
                .toCompletableFuture()
                .await()