package kotlox

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    private val keywords: Map<String, TokenType> = mapOf(
        "and" to TokenType.AND,
        "class" to TokenType.CLASS,
        "else" to TokenType.ELSE,
        "false" to TokenType.FALSE,
        "for" to TokenType.FOR,
        "fun" to TokenType.FUN,
        "if" to TokenType.IF,
        "nil" to TokenType.NIL,
        "or" to TokenType.OR,
        "print" to TokenType.PRINT,
        "return" to TokenType.RETURN,
        "super" to TokenType.SUPER,
        "this" to TokenType.THIS,
        "true" to TokenType.TRUE,
        "var" to TokenType.VAR,
        "while" to TokenType.WHILE
    )

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
             '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)

             '\n' -> line++
             ' ', '\r', '\t' -> {}

            '!' -> addToken(if(match('=')){TokenType.BANG_EQUAL}else{TokenType.BANG})
            '=' -> addToken(if(match('=')){TokenType.EQUAL_EQUAL}else{TokenType.EQUAL})
            '<' -> addToken(if(match('=')){TokenType.LESS_EQUAL}else{TokenType.LESS})
            '>' -> addToken(if(match('=')){TokenType.GREATER_EQUAL}else{TokenType.GREATER})
            '/' -> slash()
            '"' -> string()
            in '0'..'9' -> number()

            else -> {
                if (isAlpha(c)) identifier()
                else Lox.error(line, "Unexpected character '$c'.")
            }
        }
    }

    private fun match(expected: Char): Boolean {
        if(isAtEnd()) return false
        if(source[current] != expected) return false
        current++
        return true
    }

    private fun slash(){
        if(match('/')){
            while(peek() != '\n' && !isAtEnd()) advance()
        } else {
            addToken(TokenType.SLASH)
        }
    }

    private fun peek(): Char {
        if(isAtEnd()) return '\u0000' //unicode escape sequece: '\0'
        return source[current]
    }

    private fun peekNext(): Char {
        if(current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++
            advance()
        }

        // Unterminated string
        if(isAtEnd()){
            Lox.error(line, "Unterminated string.")
            return
        }

        //closing ".
        advance()

        //Trim the surrounding quotes
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun number(){
        while (isDigit(peek())) advance()

        // Look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance()

            while (isDigit(peek())) advance()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        // See if the identifier is a reserved word.
        val text = source.substring(start, current)

        var type = keywords[text]
        if (type == null) type = TokenType.IDENTIFIER
        addToken(type)
    }

    private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isDigit(c: Char): Boolean = c in '0'..'9'
    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)
    private fun advance(): Char = source[current++]
    private fun isAtEnd(): Boolean = current >= source.length
    private fun addToken(type: TokenType) = addToken(type, null)

    private fun addToken(type: TokenType, literal: Any?){
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}