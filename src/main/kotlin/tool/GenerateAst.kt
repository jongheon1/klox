package tool

import java.io.PrintWriter
import kotlin.system.exitProcess

private data class AstType(
    val name: String,
    val fields: List<String>,
) {
    companion object {
        fun of(
            name: String,
            fieldList: String,
        ) = AstType(name, fieldList.split(", ").map { it.trim() })
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }

    val outputDir = args[0]

    defineAst(
        outputDir,
        "Expr",
        listOf(
            AstType.of("Binary", "left: Expr, operator: Token, right: Expr"),
            AstType.of("Grouping", "expression: Expr"),
            AstType.of("Literal", "value: Any?"),
            AstType.of("Unary", "operator: Token, right: Expr"),
        ),
    )
}

private fun defineAst(
    outputDir: String,
    baseName: String,
    types: List<AstType>,
) {
    val path = "$outputDir/$baseName.kt"

    PrintWriter(path, "UTF-8").use { writer ->
        val visitor = defineVisitor(baseName, types.map { it.name })
        val type = defineType(baseName, types)
        writer.println(
            """
            |package lox
            |
            |sealed class $baseName {
            |$visitor
            |
            |$type
            |
            |    abstract fun <R> accept(visitor: Visitor<R>): R
            |}
            """.trimMargin(),
        )
    }
}

private fun defineVisitor(
    baseName: String,
    typeNames: List<String>,
): String {
    val visitorMethods =
        typeNames.joinToString(
            separator = "\n",
        ) { typeName -> "|        fun visit$typeName$baseName(${baseName.lowercase()}: $typeName): R" }

    return """
            |    interface Visitor<R> {
            $visitorMethods
            |    }
        """.trimMargin()
}

private fun defineType(
    baseName: String,
    types: List<AstType>,
): String =
    types.joinToString("\n\n") { (className, fields) ->
        """
                |    class $className(${fields.joinToString { "val $it" }}) : $baseName() {
                |        override fun <R> accept(visitor: Visitor<R>): R {
                |            return visitor.visit$className$baseName(this)
                |        }
                |    }
        """.trimMargin()
    }
