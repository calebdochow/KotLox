package kotlox

fun main() {
    println("Testbed running...\n")

    runTest("Single Character Tokens", ::testSingleCharTokens)
    runTest("Multi Character Tokens", ::testMultiCharTokens)
    runTest("Numbers", ::testNumbers)
    runTest("Strings", ::testStrings)
    runTest("Identifiers and Keywords", ::testIdentifiersAndKeywords)
    runTest("Comments", ::testComments)

    println("\nScanner tests passed successfully!")
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
