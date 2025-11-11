package kotlox.tools
import kotlox.Expr

class AstPrinter : Expr.Visitor<String> {

    fun print(expr: Expr): String = expr.accept(this)

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return "(assign ${expr.name.lexeme} ${expr.value.accept(this)})"
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        val builder = StringBuilder()
        builder.append("(call ")
        builder.append(expr.callee.accept(this))
        for (arg in expr.arguments) {
            builder.append(" ")
            builder.append(arg.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    override fun visitGetExpr(expr: Expr.Get): String {
        return "(get ${expr.obj.accept(this)} ${expr.name.lexeme})"
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): String {
        return "(set ${expr.obj.accept(this)} ${expr.name.lexeme} ${expr.value.accept(this)})"
    }

    override fun visitSuperExpr(expr: Expr.Super): String {
        return "(super ${expr.method.lexeme})"
    }

    override fun visitThisExpr(expr: Expr.This): String {
        return "(this)"
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return expr.name.lexeme
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }
}
