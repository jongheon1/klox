package lox

/** Unwinds the stack out of the innermost loop when a `break` is executed. */
private class BreakException : RuntimeException()

class Interpreter :
    Expr.Visitor<Any?>,
    Stmt.Visitor<Unit> {
    val globals =
        Environment().apply {
            define(
                "clock",
                object : LoxCallable {
                    override fun arity(): Int = 0

                    override fun call(
                        interpreter: Interpreter,
                        arguments: List<Any?>,
                    ): Any = System.currentTimeMillis() / 1000.0

                    override fun toString(): String = "<native fn>"
                },
            )
        }
    private var environment = globals
    private val locals: MutableMap<Expr, Int> = mutableMapOf()

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
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
                val divisor = right as Double
                if (divisor == 0.0) {
                    throw RuntimeError(expr.operator, "Division by zero.")
                }
                left as Double / divisor
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double * right as Double
            }

            TokenType.PLUS -> {
                when {
                    left is Double && right is Double -> left + right
                    left is String && right is String -> left + right
                    left is String || right is String -> stringify(left) + stringify(right)
                    else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }
            }

            TokenType.COMMA -> {
                right
            }

            else -> {
                error("Unreachable: unexpected binary operator ${expr.operator.type}")
            }
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { evaluate(it) }
        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }
        if (arguments.size != callee.arity()) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
        }
        return callee.call(this, arguments)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? = evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal): Any? = expr.value

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }
        return evaluate(expr.right)
    }

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
                error("Unreachable: unexpected unary operator ${expr.operator.type}")
            }
        }
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Any? {
        val condition = evaluate(expr.condition)

        return if (isTruthy(condition)) {
            evaluate(expr.thenExpr)
        } else {
            evaluate(expr.elseExpr)
        }
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)
        if (obj is LoxInstance) {
            return obj.get(expr.name)
        }
        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)
        if (obj !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }

        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitThisExpr(expr: Expr.This): Any? = lookUpVariable(expr.keyword, expr)

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        val distance = locals[expr]!!
        val superclass = environment.getAt(distance, "super") as LoxClass
        // `this` lives one scope nearer than `super` (see visitClassStmt).
        val obj = environment.getAt(distance - 1, "this") as LoxInstance

        val method =
            superclass.findMethod(expr.method.lexeme)
                ?: throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}'.")

        return method.bind(obj)
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? = lookUpVariable(expr.name, expr)

    private fun lookUpVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        val value =
            if (distance != null) {
                environment.getAt(distance, name.lexeme)
            } else {
                globals.get(name)
            }

        if (value === Environment.UNINITIALIZED) {
            throw RuntimeError(name, "Unassigned variable '${name.lexeme}'.")
        }
        return value
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    fun executeBlock(
        statements: List<Stmt>,
        environment: Environment,
    ) {
        val previous = this.environment

        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment, isInitializer = false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        val superclass =
            stmt.superclass?.let {
                val evaluated = evaluate(it)
                if (evaluated !is LoxClass) {
                    throw RuntimeError(it.name, "Superclass must be a class.")
                }
                evaluated
            }

        // Two-stage binding so the class's own methods can refer to the class by name.
        environment.define(stmt.name.lexeme, null)

        // If there's a superclass, wrap the methods in a scope that holds `super`.
        if (superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods =
            stmt.methods.associate { method ->
                method.name.lexeme to
                    LoxFunction(method, environment, isInitializer = method.name.lexeme == "init")
            }

        val klass = LoxClass(stmt.name.lexeme, superclass, methods)

        if (superclass != null) {
            environment = environment.enclosing!!
        }

        environment.assign(stmt.name, klass)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        try {
            while (isTruthy(evaluate(stmt.condition))) {
                execute(stmt.body)
            }
        } catch (_: BreakException) {
            // `break` exits the innermost enclosing loop.
        }
    }

    override fun visitBreakStmt(stmt: Stmt.Break) {
        throw BreakException()
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        val value = stmt.value?.let { evaluate(it) }
        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value =
            if (stmt.initializer != null) {
                evaluate(stmt.initializer)
            } else {
                Environment.UNINITIALIZED
            }
        environment.define(stmt.name.lexeme, value)
    }

    private fun isTruthy(any: Any?): Boolean =
        when (any) {
            null -> false
            is Boolean -> any
            else -> true
        }

    private fun isEqual(
        a: Any?,
        b: Any?,
    ): Boolean = a == b

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

    private fun stringify(any: Any?): String =
        when (any) {
            null -> "nil"
            is Double -> any.toString().removeSuffix(".0")
            else -> any.toString()
        }
}
