package com.dastanapps.dagger2

/**
 *
 * "Iqbal Ahmed" created on 12/7/19
 */
class Engine {
    fun start() {
        println("Start Engine")
    }
}

//Constructor Injection
class Car(private val engine: Engine) {
    fun start() {
        println("Constructor Injection")
        engine.start()
    }
}

//Field Injection
class Car2 {
    lateinit var engine: Engine
    fun start() {
        println("Field Injection")
        engine.start()
    }
}

class DIDemo {
    fun run() {
        val engine = Engine()
        val car = Car(engine)
        car.start()

        val car2 = Car2()
        car2.engine = engine
        car2.start()
    }
}

fun main(args: Array<String>) {
    DIDemo().run()
}
