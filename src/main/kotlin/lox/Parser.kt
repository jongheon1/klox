package lox

import lox.TokenType.BANG
import lox.TokenType.BANG_EQUAL
import lox.TokenType.CLASS
import lox.TokenType.EOF
import lox.TokenType.EQUAL_EQUAL
import lox.TokenType.FALSE
import lox.TokenType.FOR
import lox.TokenType.FUN
import lox.TokenType.GREATER
import lox.TokenType.GREATER_EQUAL
import lox.TokenType.IF
import lox.TokenType.LEFT_PAREN
import lox.TokenType.LESS
import lox.TokenType.LESS_EQUAL
import lox.TokenType.MINUS
import lox.TokenType.NIL
import lox.TokenType.NUMBER
import lox.TokenType.PLUS
import lox.TokenType.PRINT
import lox.TokenType.RETURN
import lox.TokenType.RIGHT_PAREN
import lox.TokenType.SEMICOLON
import lox.TokenType.SLASH
import lox.TokenType.STAR
import lox.TokenType.STRING
import lox.TokenType.TRUE
import lox.TokenType.VAR
import lox.TokenType.WHILE

class Parser(
    private val tokens: List<Token>,
) {
    companion object {
        class ParseError : RuntimeException()
    }

    private var current = 0

    fun parse(): Expr? =
        try {
            expression()
        } catch (error: ParseError) {
            null
        }

    private fun expression(): Expr = equality()

    private fun equality(): Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr =
        when {
            match(FALSE) -> {
                Expr.Literal(false)
            }

            match(TRUE) -> {
                Expr.Literal(true)
            }

            match(NIL) -> {
                Expr.Literal(null)
            }

            match(NUMBER, STRING) -> {
                Expr.Literal(previous().literal)
            }

            match(LEFT_PAREN) -> {
                val expr = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression.")
                Expr.Grouping(expr)
            }

            else -> {
                throw error(peek(), "Expect expression.")
            }
        }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun consume(
        type: TokenType,
        message: String,
    ): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(
        token: Token,
        message: String,
    ): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return
                }

                else -> { }
            }
            advance()
        }
    }
}
