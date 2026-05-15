package lox

class Interpreter : Expr.Visitor<Any?> {
    fun interpret(expr: Expr?) {
        try {
            val value = evaluate(expr)
            println(stringify(value))
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double > right as Double
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double >= right as Double
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double <= right as Double
            }

            TokenType.BANG_EQUAL -> {
                !isEqual(left, right)
            }

            TokenType.EQUAL_EQUAL -> {
                isEqual(left, right)
            }

            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double - right as Double
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double / right as Double
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double * right as Double
            }

            TokenType.PLUS -> {
                when {
                    left is Double && right is Double -> left + right
                    left is String && right is String -> left + right
                    else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }
            }

            else -> {
                null
            }
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? = evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal): Any? = expr.value

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            TokenType.BANG -> {
                !isTruthy(right)
            }

            else -> {
                null
            }
        }
    }

    private fun evaluate(expr: Expr?): Any? = expr?.accept(this)

    private fun isTruthy(any: Any?): Boolean =
        when (any) {
            null -> false
            is Boolean -> any
            else -> true
        }

    private fun isEqual(
        a: Any?,
        b: Any?,
    ): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    private fun checkNumberOperand(
        operator: Token,
        operand: Any?,
    ) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(
        operator: Token,
        left: Any?,
        right: Any?,
    ) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun stringify(any: Any?): String {
        if (any == null) return "nil"
        if (any is Double) {
            var text = any.toString()
            if (text.endsWith(".0")) {
                text = text.dropLast(2)
            }
            return text
        }
        return any.toString()
    }
}
