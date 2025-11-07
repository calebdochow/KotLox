package kotlox
import kotlox.TokenType
import kotlox.Token
import kotlox.Scanner
import kotlox.Expr
import kotlox.Parser
import kotlox.tools.AstPrinter

fun main() {
    println("Testbed running...\n")

    runTest("Single Character Tokens", ::testSingleCharTokens)
    runTest("Multi Character Tokens", ::testMultiCharTokens)
    runTest("Numbers", ::testNumbers)
    runTest("Strings", ::testStrings)
    runTest("Identifiers and Keywords", ::testIdentifiersAndKeywords)
    runTest("Comments", ::testComments)

    println("\nScanner tests passed successfully!")
    
    // Parser tests (chapter 6 - expressions)
    runTest("Unary expression", ::testUnaryExpression)
    runTest("Binary precedence", ::testBinaryPrecedence)
    runTest("Grouping and precedence", ::testGroupingPrecedence)
    runTest("Equality chaining", ::testEqualityChaining)
    runTest("Literals (true/false/nil/string)", ::testLiterals)
    
    // Build: -123 * (45.67)
    val expression = Expr.Binary(
        Expr.Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Expr.Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Expr.Grouping(Expr.Literal(45.67))
    )

    println(AstPrinter().print(expression))

}

/* ──────────────── Parser Test Helpers & Cases ──────────────── */

private fun parse(input: String): Expr {
    val tokens = scan(input)
    val parser = Parser(tokens)
    return parser.parse() ?: throw AssertionError("Parser returned null for input: $input")
}

fun testUnaryExpression() {
    val input = "-123"
    val expr = parse(input)
    val printed = kotlox.tools.AstPrinter().print(expr)
    // note: numbers are parsed as Double, so whole numbers print with .0
    assertEqual(printed, "(- 123.0)", input)
}

fun testBinaryPrecedence() {
    val input = "1 + 2 * 3"
    val expr = parse(input)
    val printed = kotlox.tools.AstPrinter().print(expr)
    assertEqual(printed, "(+ 1.0 (* 2.0 3.0))", input)
}

fun testGroupingPrecedence() {
    val input = "(1 + 2) * 3"
    val expr = parse(input)
    val printed = kotlox.tools.AstPrinter().print(expr)
    assertEqual(printed, "(* (group (+ 1.0 2.0)) 3.0)", input)
}

fun testEqualityChaining() {
    val input = "1 == 2 != 3"
    val expr = parse(input)
    val printed = kotlox.tools.AstPrinter().print(expr)
    assertEqual(printed, "(!= (== 1.0 2.0) 3.0)", input)
}

fun testLiterals() {
    // parse only the first expression at a time, so test them individually
    var expr = parse("true")
    assertEqual(kotlox.tools.AstPrinter().print(expr), "true", "true")

    expr = parse("false")
    assertEqual(kotlox.tools.AstPrinter().print(expr), "false", "false")

    expr = parse("nil")
    assertEqual(kotlox.tools.AstPrinter().print(expr), "nil", "nil")

    expr = parse("\"hi\"")
    assertEqual(kotlox.tools.AstPrinter().print(expr), "hi", "string literal")
}

private fun runTest(name: String, test: () -> Unit) {
    print("Running $name... ")
    try {
        test()
        println("[OK] Passed")
    } catch (e: AssertionError) {
        println("[FAIL] Failed")
        println("   ${e.message}")
        throw e // stop on first failure
    }
}

private fun scan(input: String): List<Token> {
    val scanner = Scanner(input)
    return scanner.scanTokens()
}

private fun assertEqual(actual: Any?, expected: Any?, input: String, message: String = "") {
    if (actual != expected) {
        throw AssertionError(
            "Input: \"$input\"\n   Expected: $expected\n   Actual: $actual\n   $message"
        )
    }
}

/* ──────────────── Scanner Test Cases ──────────────── */

fun testSingleCharTokens() {
    val input = "(){},.-+;*"
    val tokens = scan(input)
    val expected = listOf(
        TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN,
        TokenType.LEFT_BRACE, TokenType.RIGHT_BRACE,
        TokenType.COMMA, TokenType.DOT, TokenType.MINUS,
        TokenType.PLUS, TokenType.SEMICOLON, TokenType.STAR,
        TokenType.EOF
    )
    assertEqual(tokens.map { it.type }, expected, input)
}

fun testMultiCharTokens() {
    val input = "! != = == < <= > >="
    val tokens = scan(input)
    val expected = listOf(
        TokenType.BANG, TokenType.BANG_EQUAL,
        TokenType.EQUAL, TokenType.EQUAL_EQUAL,
        TokenType.LESS, TokenType.LESS_EQUAL,
        TokenType.GREATER, TokenType.GREATER_EQUAL,
        TokenType.EOF
    )
    assertEqual(tokens.map { it.type }, expected, input)
}

fun testNumbers() {
    val input = "123 45.67"
    val tokens = scan(input)
    val expected = listOf(TokenType.NUMBER, TokenType.NUMBER, TokenType.EOF)
    assertEqual(tokens.map { it.type }, expected, input)
    assertEqual(tokens[0].literal, 123.0, input, "Number literal mismatch")
    assertEqual(tokens[1].literal, 45.67, input, "Decimal literal mismatch")
}

fun testStrings() {
    val input = "\"lox\" \"hello world\""
    val tokens = scan(input)
    val expected = listOf(TokenType.STRING, TokenType.STRING, TokenType.EOF)
    assertEqual(tokens.map { it.type }, expected, input)
    assertEqual(tokens[0].literal, "lox", input)
    assertEqual(tokens[1].literal, "hello world", input)
}

fun testIdentifiersAndKeywords() {
    val input = "var language = lox print language"
    val tokens = scan(input)
    val expected = listOf(
        TokenType.VAR, TokenType.IDENTIFIER, TokenType.EQUAL,
        TokenType.IDENTIFIER, TokenType.PRINT, TokenType.IDENTIFIER,
        TokenType.EOF
    )
    assertEqual(tokens.map { it.type }, expected, input)
}

fun testComments() {
    val input = "// this is a comment\n123"
    val tokens = scan(input)
    assertEqual(tokens[0].type, TokenType.NUMBER, input)
    assertEqual(tokens[1].type, TokenType.EOF, input)
}
