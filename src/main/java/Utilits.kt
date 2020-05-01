import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode


fun getFlattenLeaf(tree: ParseTree): MutableList<Token> {
    val result = mutableListOf<Token>()
    for (i in 0 until tree.childCount) {
        val child = tree.getChild(i)
        if (child is TerminalNode) {
            val token = child.symbol
            if (token.type != 27) // 27 is type of variables
                continue
            result.add(token)
        } else {
            result.addAll(getFlattenLeaf(child))
        }
    }

    return result
}


/**
 * returns type of token
 * If after last char of token (, this token is `TokenType.CALLABLE`
 * If after last char of token ., this token is `TokenType.VARIABLE`
 * If before token word "as", this is `TokenType.TYPE`
 * In other cases, `TokenType.VARIABLE` will be returned
 *
 * Works bad with wrong spaces ("type. member", for example)
 */

fun getTokenType(token: Token, text: String): TokenType {
    val endPosition = token.stopIndex
    val startPosition = token.startIndex
    if (endPosition + 1 >= text.length)
        throw Exception("error")
    if (endPosition + 2 < text.length && text[endPosition + 1] == ' ' && text[endPosition + 2] == '=') { // todo
        return TokenType.VARIABLE_DECLARATION
    }
    when (text[endPosition + 1]) {
        '(' -> return TokenType.CALLABLE
        '.' -> return TokenType.VARIABLE
    }

    if (startPosition <= 2) // 2 is some spaces in start of file
        return TokenType.VARIABLE

    if (text.slice((startPosition-3..startPosition-2)) == "as") {
        return TokenType.TYPE
    }

    if (text[startPosition-1] == '.') {
        return TokenType.MEMBER
    }
    return TokenType.VARIABLE
}

enum class TokenType {
    CALLABLE,
    VARIABLE,
    VARIABLE_DECLARATION,
    TYPE,
    MEMBER,
    CAST
}


fun getTypedVariableByMemberToken(token: Token, text: String): String {
    var i = token.startIndex - 2 // -2= -1 - 1, where first -1 points to dot, second -1 points to end of var
    val res = StringBuffer()
    while (i >= 0 && text[i] !in listOf('.', ',', ' ', '(', ')', '\n')) { // todo: add other token separators
        res.append(text[i])
        i--
    }

    return res.reverse().toString()
}

fun getParametersNames(ctx: TdlParser.ParametersContext): List<String> {
    return ctx.children.filterIsInstance<TdlParser.ParameterContext>().map { it.text }
}

fun getParamsCount(token: Token, text: String): Int {
    val startBracket = token.stopIndex + 1
    val stopBracket = text.slice(startBracket until text.length).indexOfFirst { it == ')' } + startBracket
    val split = text.slice(startBracket..stopBracket)
    val charCount = split.count { it != ' ' }
    return if (charCount == 2) { // empty brackets
        0
    } else {
        1 + split.count { it == ',' }
    }
}