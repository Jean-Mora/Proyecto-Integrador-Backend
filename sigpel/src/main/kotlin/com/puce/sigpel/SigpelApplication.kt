package com.puce.sigpel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SigpelApplication

fun main(args: Array<String>) {
    runApplication<SigpelApplication>(*args)
}
