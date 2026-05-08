import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ScannerTest :
    StringSpec({
        "scans single-character tokens" {
            val tokens = Scanner("(){},.").scanTokens()

            tokens.map { it.type } shouldBe
                listOf(
                    TokenType.LEFT_PAREN,
                    TokenType.RIGHT_PAREN,
                    TokenType.LEFT_BRACE,
                    TokenType.RIGHT_BRACE,
                    TokenType.COMMA,
                    TokenType.DOT,
                    TokenType.EOF,
                )
        }

        "distinguishes one- and two-character operators via lookahead" {
            val tokens = Scanner("! != = == < <= > >=").scanTokens()

            tokens.map { it.type } shouldBe
                listOf(
                    TokenType.BANG,
                    TokenType.BANG_EQUAL,
                    TokenType.EQUAL,
                    TokenType.EQUAL_EQUAL,
                    TokenType.LESS,
                    TokenType.LESS_EQUAL,
                    TokenType.GREATER,
                    TokenType.GREATER_EQUAL,
                    TokenType.EOF,
                )
        }

        "scans string and number literals with their values" {
            val tokens = Scanner("\"hello\" 12.5").scanTokens()

            tokens.map { it.type to it.literal } shouldBe
                listOf(
                    TokenType.STRING to "hello",
                    TokenType.NUMBER to 12.5,
                    TokenType.EOF to null,
                )
        }

        "distinguishes keywords from identifiers" {
            val tokens = Scanner("var foo123 if").scanTokens()

            tokens.map { it.type to it.lexeme } shouldBe
                listOf(
                    TokenType.VAR to "var",
                    TokenType.IDENTIFIER to "foo123",
                    TokenType.IF to "if",
                    TokenType.EOF to "",
                )
        }

        "skips comments and whitespace, tracks line numbers across newlines" {
            val source =
                """
                // line comment
                /* block
                   /* nested */
                   still in comment */
                var x
                """.trimIndent()

            val tokens = Scanner(source).scanTokens()

            tokens.map { it.type to it.line } shouldBe
                listOf(
                    TokenType.VAR to 5,
                    TokenType.IDENTIFIER to 5,
                    TokenType.EOF to 5,
                )
        }
    })
