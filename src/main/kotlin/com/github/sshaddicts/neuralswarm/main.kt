package com.github.sshaddicts.neuralswarm

import akka.actor.ActorSystem
import com.github.sshaddicts.lucrecium.imageProcessing.ImageProcessor
import com.github.sshaddicts.neuralswarm.actor.ImageProcessorActor.Companion.labels
import com.github.sshaddicts.neuralswarm.actor.Root
import com.github.sshaddicts.neuralswarm.utils.neural.processor
import com.github.sshaddicts.neuralswarm.utils.neural.recognizer
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgcodecs.Imgcodecs.imdecode
import java.io.File


fun main(argv: Array<String>) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

//      НЕ РАБЗИТ
//    val bytes = File("/image").readBytes()
//
//    val mat = imdecode(MatOfByte(*bytes), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE)
//
//    val data = processor.findTextRegions(mat, ImageProcessor.NO_MERGE, true)
//    val recognized = recognizer.recognize(data.chars, labels)
//
//    recognized.forEach {
//        println(it)
//    }

    val actorSystem = ActorSystem.create("system")
    Root.create(actorSystem)
}