package kotlox

internal class Interpreter : Expr.Visitor<Any?> {

    fun interpret(expression: Expr?) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
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
        throw RuntimeError(expr.name, "Assignment not yet implemented.")
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        throw RuntimeError(expr.paren, "Function calls not yet implemented.")
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        throw RuntimeError(expr.name, "Property access not yet implemented.")
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        throw RuntimeError(expr.operator, "Logical operators not yet implemented.")
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
        throw RuntimeError(expr.name, "Variables not yet implemented.")
    }
}
