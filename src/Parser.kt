package kotlox

import kotlox.TokenType.*
import kotlox.Return

/**
 * Full Parser for KotLox (Chapters 6-10):
 * - declarations (fun, var)
 * - statements (for, if, while, print, return, block, expr-stmt)
 * - expressions including calls and logical (and/or)
 */
class Parser(private val tokens: List<Token>) {
    private var current = 0

    // entry point: parse a sequence of declarations/statements into a program
    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            val decl = declaration()
            if (decl != null) statements.add(decl)
        }
        return statements
    }

    // --------------------
    // Declarations
    // --------------------
    private fun declaration(): Stmt? {
        try {
            if (match(FUN)) return function("function")
            if (match(VAR)) return varDeclaration()
            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
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

    // helper for function declarations: function(kind) -> Stmt.Function
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
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")
        consume(LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    // --------------------
    // Statements
    // --------------------
    private fun statement(): Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(WHILE)) return whileStatement()
        if (match(PRINT)) return printStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        if (match(RETURN)) return returnStatement()
        return expressionStatement()
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Stmt? = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        var condition: Expr? = null
        if (!check(SEMICOLON)) condition = expression()
        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) increment = expression()
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        // desugar increment into end of loop body
        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
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
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        var value: Expr? = null
        if (!check(SEMICOLON)) {
            value = expression()
        }
        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            val decl = declaration()
            if (decl != null) statements.add(decl)
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    // --------------------
    // Expressions (with assignment & logical)
    // --------------------
    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = or()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    // or → and ( "or" and )* ;
    private fun or(): Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    // and → equality ( "and" equality )* ;
    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
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

    // call -> primary ( "(" arguments? ")" )* ;
    private fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }
        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    // --------------------
    // helpers
    // --------------------
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
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

    // --------------------
    // errors & recovery
    // --------------------
    private class ParseError : RuntimeException()

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token.line, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }
}