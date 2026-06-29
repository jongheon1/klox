package lox

import lox.TokenType.AND
import lox.TokenType.BANG
import lox.TokenType.BANG_EQUAL
import lox.TokenType.BREAK
import lox.TokenType.CLASS
import lox.TokenType.ELSE
import lox.TokenType.EOF
import lox.TokenType.EQUAL
import lox.TokenType.EQUAL_EQUAL
import lox.TokenType.FALSE
import lox.TokenType.FOR
import lox.TokenType.FUN
import lox.TokenType.GREATER
import lox.TokenType.GREATER_EQUAL
import lox.TokenType.IDENTIFIER
import lox.TokenType.IF
import lox.TokenType.LEFT_BRACE
import lox.TokenType.LEFT_PAREN
import lox.TokenType.LESS
import lox.TokenType.LESS_EQUAL
import lox.TokenType.MINUS
import lox.TokenType.NIL
import lox.TokenType.NUMBER
import lox.TokenType.OR
import lox.TokenType.PLUS
import lox.TokenType.PRINT
import lox.TokenType.RETURN
import lox.TokenType.RIGHT_BRACE
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

    // How many loops we are currently inside (parse-time). `break` is only
    // legal when this is > 0. Reset to 0 when descending into a function body.
    private var loopDepth = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            declaration()?.let { statements.add(it) }
        }
        return statements
    }

    private fun declaration(): Stmt? {
        try {
            if (match(CLASS)) return classDeclaration()
            if (match(FUN)) return function("function")
            if (match(VAR)) return varDeclaration()
            return statement()
        } catch (_: ParseError) {
            synchronize()
            return null
        }
    }

    private fun classDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect class name.")

        var superclass: Expr.Variable? = null
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.")
            superclass = Expr.Variable(previous())
        }

        consume(LEFT_BRACE, "Expect '{' before class body.")

        val methods = mutableListOf<Stmt.Function>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"))
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.")
        return Stmt.Class(name, superclass, methods)
    }

    private fun function(kind: String): Stmt.Function {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")

        val parameters = mutableListOf<Token>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.")
                }

                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(TokenType.COMMA))
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters.")
        consume(LEFT_BRACE, "Expect '{' before $kind body.")

        val enclosingLoopDepth = loopDepth
        loopDepth = 0
        try {
            val body = block()
            return Stmt.Function(name, parameters, body)
        } finally {
            loopDepth = enclosingLoopDepth
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(RETURN)) return returnStatement()
        if (match(WHILE)) return whileStatement()
        if (match(BREAK)) return breakStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun breakStatement(): Stmt {
        val keyword = previous()
        if (loopDepth == 0) {
            error(keyword, "Must be inside a loop to use 'break'.")
        }
        consume(SEMICOLON, "Expect ';' after 'break'.")
        return Stmt.Break(keyword)
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer =
            if (match(SEMICOLON)) {
                null
            } else if (match(VAR)) {
                varDeclaration()
            } else {
                expressionStatement()
            }

        val condition =
            if (!check(SEMICOLON)) {
                expression()
            } else {
                Expr.Literal(true)
            }

        consume(SEMICOLON, "Expect ';' after loop condition.")

        val increment =
            if (!check(RIGHT_PAREN)) {
                expression()
            } else {
                null
            }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        loopDepth++
        var body =
            try {
                statement()
            } finally {
                loopDepth--
            }

        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        val elseBranch =
            if (match(ELSE)) {
                statement()
            } else {
                null
            }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")

        loopDepth++
        val body =
            try {
                statement()
            } finally {
                loopDepth--
            }
        return Stmt.While(condition, body)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            declaration()?.let { statements.add(it) }
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value =
            if (!check(SEMICOLON)) {
                expression()
            } else {
                null
            }

        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun expression(): Expr = comma()

    private fun comma(): Expr {
        var expr = assignment()

        while (match(TokenType.COMMA)) {
            val operator = previous()
            val right = assignment()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun assignment(): Expr {
        val expr = ternary()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            } else if (expr is Expr.Get) {
                return Expr.Set(expr.obj, expr.name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun ternary(): Expr {
        var expr = or()

        if (match(TokenType.QUESTION)) {
            val thenExpr = expression()
            consume(TokenType.COLON, "Expect ':' after expression")
            val elseExpr = ternary()
            expr = Expr.Ternary(expr, thenExpr, elseExpr)
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

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

        return call()
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if (match(TokenType.DOT)) {
                val name = consume(IDENTIFIER, "Expect property name after '.'.")
                expr = Expr.Get(expr, name)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(expr: Expr): Expr {
        val arguments = mutableListOf<Expr>()

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(assignment())
            } while (match(TokenType.COMMA))
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return Expr.Call(expr, paren, arguments)
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

            match(TokenType.THIS) -> {
                Expr.This(previous())
            }

            match(TokenType.SUPER) -> {
                val keyword = previous()
                consume(TokenType.DOT, "Expect '.' after 'super'.")
                val method = consume(IDENTIFIER, "Expect superclass method name.")
                Expr.Super(keyword, method)
            }

            match(IDENTIFIER) -> {
                Expr.Variable(previous())
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

                else -> {}
            }
            advance()
        }
    }
}
