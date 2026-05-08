import TokenType.AND
import TokenType.BANG
import TokenType.BANG_EQUAL
import TokenType.CLASS
import TokenType.COMMA
import TokenType.DOT
import TokenType.ELSE
import TokenType.EOF
import TokenType.EQUAL
import TokenType.EQUAL_EQUAL
import TokenType.FALSE
import TokenType.FOR
import TokenType.FUN
import TokenType.GREATER
import TokenType.GREATER_EQUAL
import TokenType.IDENTIFIER
import TokenType.IF
import TokenType.LEFT_BRACE
import TokenType.LEFT_PAREN
import TokenType.LESS
import TokenType.LESS_EQUAL
import TokenType.MINUS
import TokenType.NIL
import TokenType.NUMBER
import TokenType.OR
import TokenType.PLUS
import TokenType.PRINT
import TokenType.RETURN
import TokenType.RIGHT_BRACE
import TokenType.RIGHT_PAREN
import TokenType.SEMICOLON
import TokenType.SLASH
import TokenType.STAR
import TokenType.STRING
import TokenType.SUPER
import TokenType.THIS
import TokenType.TRUE
import TokenType.VAR
import TokenType.WHILE

class Scanner(
    private val source: String,
) {
    private val tokens: MutableList<Token> = mutableListOf()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    companion object {
        private val keywords: Map<String, TokenType> =
            mapOf(
                "and" to AND,
                "class" to CLASS,
                "else" to ELSE,
                "false" to FALSE,
                "for" to FOR,
                "fun" to FUN,
                "if" to IF,
                "nil" to NIL,
                "or" to OR,
                "print" to PRINT,
                "return" to RETURN,
                "super" to SUPER,
                "this" to THIS,
                "true" to TRUE,
                "var" to VAR,
                "while" to WHILE,
            )
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean = current >= source.length

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> {
                addToken(LEFT_PAREN)
            }

            ')' -> {
                addToken(RIGHT_PAREN)
            }

            '{' -> {
                addToken(LEFT_BRACE)
            }

            '}' -> {
                addToken(RIGHT_BRACE)
            }

            ',' -> {
                addToken(COMMA)
            }

            '.' -> {
                addToken(DOT)
            }

            '-' -> {
                addToken(MINUS)
            }

            '+' -> {
                addToken(PLUS)
            }

            ';' -> {
                addToken(SEMICOLON)
            }

            '*' -> {
                addToken(STAR)
            }

            '!' -> {
                addToken(if (match('=')) BANG_EQUAL else BANG)
            }

            '=' -> {
                addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            }

            '<' -> {
                addToken(if (match('=')) LESS_EQUAL else LESS)
            }

            '>' -> {
                addToken(if (match('=')) GREATER_EQUAL else GREATER)
            }

            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else if (match('*')) {
                    blockComment()
                } else {
                    addToken(SLASH)
                }
            }

            ' ', '\r', '\t' -> { }

            '\n' -> {
                line++
            }

            '"' -> {
                string()
            }

            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun advance(): Char = source[current++]

    private fun addToken(
        type: TokenType,
        literal: Any? = null,
    ) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000' // why \0 is illegal
        return source[current]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }
        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    private fun number() {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun isAlpha(c: Char): Boolean =
        (c in 'a'..'z') ||
            (c in 'A'..'Z') ||
            c == '_'

    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()

        val text = source.substring(start, current)
        val type = keywords[text] ?: IDENTIFIER
        addToken(type)
    }

    private fun blockComment() {
        var depth = 1
        while (depth > 0 && !isAtEnd()) {
            when {
                peek() == '/' && peekNext() == '*' -> {
                    advance()
                    advance()
                    depth++
                }

                peek() == '*' && peekNext() == '/' -> {
                    advance()
                    advance()
                    depth--
                }

                else -> {
                    if (peek() == '\n') line++
                    advance()
                }
            }
        }

        if (depth > 0) Lox.error(line, "Unterminated block comment.")
    }
}
