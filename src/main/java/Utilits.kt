import ast.objects.Variable
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
    MEMBER
}


fun getTypedVariableByMemberToken(token: Token, parser: TdlParser): String =
    parser.tokenStream[token.tokenIndex-2].text // -1 is '.', -2 is searching token name


fun getParamsCount(token: Token, text: String): Int {
    val startBracket = token.stopIndex + 1
    var i = startBracket
    var bracketsPairs = 0
    var paramsCount = 0
    var zeroCommaFlag = false

    while (i < text.length) {
        when (text[i]) {
            '(' -> bracketsPairs++
            ')' -> bracketsPairs--
            ',' -> {
                if (bracketsPairs == 1)
                    paramsCount++
            }
            ' ' -> {}
            else -> zeroCommaFlag = true
        }
        if (bracketsPairs == 0) {
            if (zeroCommaFlag && paramsCount == 0)
                paramsCount = 1
            else if (paramsCount != 0)
                paramsCount += 1

            break
        }
        i++
    }

    return paramsCount
}

fun exploreStatement(ctx: ParseTree, scope: Scope, text: String, parentName: String, parser: TdlParser): VisitErrors {
    val visitResults = VisitErrors()

    val leafs = getFlattenLeaf(ctx)
    for (leaf in leafs) {
        val name = leaf.text
        when (getTokenType(leaf, text)) {
            TokenType.CALLABLE -> {
                val paramsCount = getParamsCount(leaf, text)
                if (scope.getCallable(name, paramsCount) == null) {
                    val variants = scope.getCallable(name)
                    if (variants != null)
                        visitResults.add(
                                UnmatchingArgument(name, parentName, leaf.line, variants)
                        )
                    else
                        visitResults.add(
                                Unresolved(name, parentName, leaf.line)
                        )
                }
            }
            TokenType.VARIABLE -> {
                if (scope.getVariable(name) == null)
                    visitResults.add(
                            Unresolved(name, parentName, leaf.line)
                    )
            }
            TokenType.MEMBER -> {
                val exemplar = scope.getExemplar(getTypedVariableByMemberToken(leaf, parser))
                val param = exemplar?.reference?.parameterNameList?.find { it.name == name }
                if (exemplar == null || param == null)
                    visitResults.add(
                            Unresolved(name, parentName, leaf.line)
                    )
                else {
                    param.isUsed = true
                }
            }
            else -> {}
        }
    }

    return visitResults
}

fun exploreAssignment(ctx: TdlParser.AssignmentContext, scope: Scope, text: String, parentName: String, parser: TdlParser): VisitErrors {
    val variableName = ctx.assignableExpression().simpleIdentifier().Identifier().text
    val expression = ctx.expression()
    val visitResult = VisitErrors()
    // if cast (newVar = var as Type)
    if (expression.asExpression() != null) {
        val typeName = expression.asExpression().type().text
        val callableType = scope.getType(typeName)

        if (callableType == null) {
            visitResult.add(
                    Unresolved(typeName, parentName, expression.start.line)
            )
        } else {
            val nameOfVariableOnTheLeft = ctx.expression()
                    .asExpression()
                    .additiveExpression()
                    .text
            val variableOnTheLeft = scope.getVariable(nameOfVariableOnTheLeft)
            if (variableOnTheLeft == null)
                visitResult.add(
                        Unresolved(nameOfVariableOnTheLeft, parentName, expression.start.line)
                )
            else {
                val variable = Variable(variableName)
                variable.fields = callableType.parameterNameList.map { it.name }
                variable.reference = callableType
                if(!scope.addExemplar(variable))
                    visitResult.add(
                            Ambiguity(variableName, parentName, expression.start.line)
                    )
            }
        }
    } else{ // case for all non-as assignments
        val leafs = getFlattenLeaf(expression)
        if (leafs.size == 1 && getTokenType(leafs.first(), text) == TokenType.CALLABLE) {
            // if this is assigment with one entity on the right part
            val leaf = leafs.first()
            val name = leaf.text
            val params = getParamsCount(leaf, text)
            val typeCallable = scope.getType(name, params)

            //todo refactor?
            if (typeCallable != null) {
                // if this is type constructor
                val variable = Variable(variableName)
                variable.fields = typeCallable.parameterNameList.map { it.name }
                variable.reference = typeCallable
                if (!scope.addExemplar(variable))
                    visitResult.add(
                            Ambiguity(variableName, parentName, expression.start.line)
                    )
            } else if (scope.getCallable(name, params) == null) {
                //if this is access to exemplar?
                val reference = scope.getExemplar(name)?.reference
                if (reference == null || scope.getInvokeOn(reference.name) == null) {
                    val variants = scope.getCallable(name)
                    if (variants != null)
                        visitResult.add(
                                UnmatchingArgument(name, parentName, expression.start.line, variants)
                        )
                    else
                        visitResult.add(
                                Unresolved(name, parentName, expression.start.line)
                        )
                }
            }  else {
                // this is function call, invoke on, ...
                val callable = scope.getCallable(name, params)
                if (callable == null)
                    visitResult.add(
                            Unresolved(name, parentName, expression.start.line)
                    )
                val variable = Variable(variableName)
                if (!scope.addVariable(variable))
                    visitResult.add(
                            Ambiguity(variableName, parentName, expression.start.line)
                    )
            }
        } else {
            // if this is assigment of expression
            for (leaf in leafs) {
                val name = leaf.text
                when (getTokenType(leaf, text)) {
                    TokenType.CALLABLE -> {
                        val paramsCount = getParamsCount(leaf, text)
                        if (scope.getCallable(name, paramsCount) == null) {
                            val variants = scope.getCallable(name)
                            if (variants == null)
                                visitResult.add(
                                        Unresolved(name, parentName, expression.start.line)
                                )
                            else
                                visitResult.add(
                                        UnmatchingArgument(name, parentName, expression.start.line, variants)
                                )
                        }
                    }
                    TokenType.VARIABLE -> {
                        if (scope.getVariable(name) == null)
                            visitResult.add(
                                    Unresolved(name, parentName, expression.start.line)
                            )
                    }
                    TokenType.MEMBER -> {
                        val typedVariable = getTypedVariableByMemberToken(leaf, parser)
                        val ref = scope.getExemplar(typedVariable)?.reference
                        if (ref == null || !ref.parameterNameList.any { it.name == name })
                            visitResult.add(
                                    Unresolved(name, parentName, expression.start.line)
                            )
                    }
                    else -> {
                        println("skipped resolve of $name")
                    }
                }
            }

            if(!scope.addVariable(Variable(variableName)))
                visitResult.add(
                        Ambiguity(variableName, parentName, expression.start.line)
                )
        }
    }

    return visitResult
}

fun exploreAsOperator(ctx: TdlParser.AsExpressionContext, scope: Scope, parentName: String, parser: TdlParser): VisitErrors {
    val visitResult = VisitErrors()

    //checking for this is operator (not assignment)
    val tokenStream = parser.tokenStream
    val startTokenIndex = ctx.start.tokenIndex
    var i = startTokenIndex - 1

    while (i > 0) {
        if (tokenStream[i].channel == 0)  // 0 is channel for readable tokens
            if (tokenStream[i].text == "=")
                return visitResult
            else
                break
        i--
    }

    val variableName = ctx.additiveExpression().text
    val typeName = ctx.type().text
    val type = scope.getType(typeName)

    if (type == null) {
        visitResult.add(
                Unresolved(typeName, parentName, ctx.start.line)
        )
        return visitResult
    }

    val variable = Variable(variableName)
    variable.fields = type.parameterNameList.map { it.name }
    variable.reference = type
    if (!scope.addExemplar(variable))
        visitResult.add(
                Ambiguity(variableName, parentName, ctx.type().start.line)
        )

    return visitResult
}