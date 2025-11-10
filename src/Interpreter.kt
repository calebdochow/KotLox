package kotlox

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    // Execution environment
    private val globals = Environment()
    private var environment: Environment = globals

    // Expression-based interpreter (chapter 6)
    fun interpret(expression: Expr?) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    // Statement-based interpreter
    fun interpret(statements: List<Stmt>) {
        try {
            for (stmt in statements) {
                if (stmt is Stmt.Expression) {
                    val value = evaluate(stmt.expression)
                    println(stringify(value))
                } else {
                    execute(stmt)
                }
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }


    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun executeBlock(statements: Iterable<Stmt>, newEnv: Environment) {
        val previous = environment
        try {
            environment = newEnv
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            environment = previous
        }
    }



    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            TokenType.BANG -> !isTruthy(right)

            else -> null
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }
            }

            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)

            else -> null
        }
    }

    private fun evaluate(expr: Expr?): Any? {
        return expr?.accept(this)
    }

    private fun isTruthy(obj: Any?): Boolean {
        return when (obj) {
            null -> false
            is Boolean -> obj
            else -> true
        }
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"

        if (value is Double) {
            var text = value.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return value.toString()
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        // evaluate arguments into a list
        val arguments = mutableListOf<Any?>()
        for (arg in expr.arguments) {
            arguments.add(evaluate(arg))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        if (arguments.size != callee.arity()) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
        }

        return callee.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        throw RuntimeError(expr.name, "Property access not yet implemented.")
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        return when (expr.operator.type) {
            TokenType.OR -> {
                if (isTruthy(left)) return left 
                evaluate(expr.right)
            }
            TokenType.AND -> {
                if (!isTruthy(left)) return left 
                evaluate(expr.right)
            }
            else -> throw RuntimeError(expr.operator, "Unknown logical operator")
        }
    }


    override fun visitSetExpr(expr: Expr.Set): Any? {
        throw RuntimeError(expr.name, "Property assignment not yet implemented.")
    }

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        throw RuntimeError(expr.keyword, "Super not yet implemented.")
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        throw RuntimeError(expr.keyword, "This not yet implemented.")
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    /* Statement visitor methods */
    override fun visitExpressionStmt(expr: Stmt.Expression) {
        evaluate(expr.expression)
    }

    override fun visitPrintStmt(expr: Stmt.Print) {
        val value = evaluate(expr.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(expr: Stmt.Var) {
    val value = if (expr.initializer != null) evaluate(expr.initializer) else null
    environment.define(expr.name.lexeme, value)
    }

    override fun visitBlockStmt(expr: Stmt.Block) {
        executeBlock(expr.statements, Environment(environment))
    }

    override fun visitIfStmt(expr: Stmt.If) {
        val cond = evaluate(expr.condition)
        if (isTruthy(cond)) {
            execute(expr.thenBranch)
        } else if (expr.elseBranch != null) {
            execute(expr.elseBranch)
        }
    }

    override fun visitWhileStmt(expr: Stmt.While) {
        while (isTruthy(evaluate(expr.condition))) {
            execute(expr.body)
        }
    }

    override fun visitFunctionStmt(expr: Stmt.Function) {
        val function = LoxFunction(expr, environment, false)
        environment.define(expr.name.lexeme, function)
    }

    override fun visitReturnStmt(expr: Stmt.Return) {
        val value = if (expr.value != null) evaluate(expr.value) else null
        throw Return(value)
    }

}
