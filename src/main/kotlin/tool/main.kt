package tool

import lox.Lox

fun main() {
    val lox = Lox
    lox.main(arrayOf("./test2.lox"))
}

class Foo {
    var func = { i: Int -> i + 1}

    fun bar() {
        func = { i -> i * i }
        func(1)
    }
}