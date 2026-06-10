import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import lox.Interpreter
import lox.Parser
import lox.Resolver
import lox.Scanner
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Runs [source] through the full pipeline and returns its trimmed stdout.
 * Also prints a terminal-style transcript (source + output) so the test
 * reads like a real `klox` session when you watch the test run.
 */
private fun run(source: String): String {
    val program = source.trimIndent()
    val tokens = Scanner(program).scanTokens()
    val statements = Parser(tokens).parse()
    val interpreter = Interpreter()
    Resolver(interpreter).resolve(statements)

    val realOut = System.out
    val buffer = ByteArrayOutputStream()
    System.setOut(PrintStream(buffer))
    try {
        interpreter.interpret(statements)
    } finally {
        System.setOut(realOut)
    }

    val output = buffer.toString().replace("\r\n", "\n").trim()
    printTranscript(program, output)
    return output
}

/** Renders a source snippet and its output as a faux terminal session. */
private fun printTranscript(
    program: String,
    output: String,
) {
    val out = StringBuilder()
    out.appendLine()
    out.appendLine("$ cat program.lox")
    program.lines().forEach { out.appendLine(it) }
    out.appendLine("$ klox program.lox")
    output.lines().forEach { out.appendLine(it) }
    println(out)
}

class CommaOperatorTest :
    StringSpec({
        "evaluates to the rightmost operand" {
            run(
                """
                print 1, 2, 3;
                """,
            ) shouldBe "3"
        }

        "chains assignments and evaluates to the last one" {
            run(
                """
                var a; var b; var c;
                a = 1, b = 2, c = 3;
                print a;
                print b;
                print c;
                """,
            ) shouldBe "1\n2\n3"
        }

        "evaluates left operands for their side effects, then returns the right" {
            run(
                """
                var a = 0;
                var b = (a = 1, a + 4);
                print a;
                print b;
                """,
            ) shouldBe "1\n5"
        }

        "does not treat function call arguments as the comma operator" {
            run(
                """
                fun add(x, y) {
                  return x + y;
                }
                print add(1, 2);
                """,
            ) shouldBe "3"
        }

        "allows a comma expression in a ternary's middle operand" {
            run(
                """
                print true ? 1, 2 : 3;
                """,
            ) shouldBe "2"
        }
    })
