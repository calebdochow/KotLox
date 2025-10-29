package kotlox

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken(){
        val c = advance()
        when (c) {
             '(' -> addToken(TokenType.LPAREN)
            ')' -> addToken(TokenType.RPAREN)
            '{' -> addToken(TokenType.LBRACE)
            '}' -> addToken(TokenType.RBRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '/' -> addToken(TokenType.SLASH)
             '\n' -> line++
             ' ', '\r', '\t' -> {}
            else -> Lox.error(line, "Unexpected character.")
        }
    }

    private fun advance(): Char = source[current++]
    private fun isAtEnd(): Boolean = current >= source.length
    private fun addToken(type: TokenType) = addToken(type, null)
    private fun addToken(type: TokenType, literal: Any?){
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}