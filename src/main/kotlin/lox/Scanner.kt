package lox

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
                "and" to TokenType.AND,
                "class" to TokenType.CLASS,
                "else" to TokenType.ELSE,
                "false" to TokenType.FALSE,
                "for" to TokenType.FOR,
                "fun" to TokenType.FUN,
                "if" to TokenType.IF,
                "nil" to TokenType.NIL,
                "or" to TokenType.OR,
                "print" to TokenType.PRINT,
                "return" to TokenType.RETURN,
                "super" to TokenType.SUPER,
                "this" to TokenType.THIS,
                "true" to TokenType.TRUE,
                "var" to TokenType.VAR,
                "while" to TokenType.WHILE,
            )
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean = current >= source.length

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> {
                addToken(TokenType.LEFT_PAREN)
            }

            ')' -> {
                addToken(TokenType.RIGHT_PAREN)
            }

            '{' -> {
                addToken(TokenType.LEFT_BRACE)
            }

            '}' -> {
                addToken(TokenType.RIGHT_BRACE)
            }

            ',' -> {
                addToken(TokenType.COMMA)
            }

            '.' -> {
                addToken(TokenType.DOT)
            }

            '-' -> {
                addToken(TokenType.MINUS)
            }

            '+' -> {
                addToken(TokenType.PLUS)
            }

            ';' -> {
                addToken(TokenType.SEMICOLON)
            }

            '*' -> {
                addToken(TokenType.STAR)
            }

            '!' -> {
                addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            }

            '=' -> {
                addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            }

            '<' -> {
                addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            }

            '>' -> {
                addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            }

            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else if (match('*')) {
                    blockComment()
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            '?' -> {
                addToken(TokenType.QUESTION)
            }

            ':' -> {
                addToken(TokenType.COLON)
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
        addToken(TokenType.STRING, value)
    }

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    private fun number() {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
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
        val type = keywords[text] ?: TokenType.IDENTIFIER
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
