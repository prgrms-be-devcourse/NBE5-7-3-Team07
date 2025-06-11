package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	//runApplication<DemoApplication>(*args)
	val dan = 7
	for (i in 1..9) {
		println("$dan x $i = ${dan*i}")
	}
	a1()
}

fun a1() {
	for(i in 1..9) {
		println("$i")
	}
}
