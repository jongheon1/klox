package lox

import java.nio.charset.Charset
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.system.exitProcess

object Lox {
    private var hadError = false

    @JvmStatic
    fun main(args: Array<String>) {
        when {
            args.size > 1 -> {
                println("Usage: klox [script]")
                exitProcess(64)
            }

            args.size == 1 -> {
                runFile(args[0])
            }

            else -> {
                runPrompt()
            }
        }
    }

    private fun runFile(path: String) {
        val bytes = Path(path).readBytes()
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) exitProcess(65)
    }

    private fun runPrompt() {
        val reader = System.`in`.bufferedReader()

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        for (token in tokens) {
            println(token)
        }
    }

    fun error(
        line: Int,
        message: String,
    ) {
        report(line, "", message)
    }

    private fun report(
        line: Int,
        where: String,
        message: String,
    ) {
        println("[line $line] Error$where: $message")
        hadError = true
    }
}
