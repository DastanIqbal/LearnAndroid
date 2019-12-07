package com.dastanapps.dagger2

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */

object ServiceLocator {
    val engine = Engine()
}

class Car3 {
    private val engine = ServiceLocator.engine

    fun start() {
        println("Service Locator")
        engine.start()
    }
}

class ServiceLocatorDemo {
    private val TAG = this::class.java.simpleName

    fun run() {
        val car = Car3()
        car.start()
    }
}

fun main(args: Array<String>) {
    ServiceLocatorDemo().run()
}