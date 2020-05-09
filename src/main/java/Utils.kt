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
 */

fun getTokenType(token: Token, parser: TdlParser): TokenType {
    val idx = token.tokenIndex
    val stream = parser.tokenStream

    var nextToken: Token? = null
    var prevToken: Token? = null

    //getting next and previous tokens
    var i = idx + 1
    while (i < stream.size()) {
        if (stream[i].channel == 0) { // 0 is channel for unhidden tokens
            nextToken = stream[i]
            break
        }
        i++
    }
    i = idx - 1
    while (i > 0) {
        if (stream[i].channel == 0 && stream[i].type != 3) { // token is visible
            prevToken = stream[i]
            break
        }
        i--
    }

    val prevText = prevToken?.text

    when (nextToken?.text) {
        "=" -> return TokenType.VARIABLE_DECLARATION
        "(" -> return TokenType.CALLABLE
        "." -> return TokenType.VARIABLE
    }

    when (prevText) {
        "as" -> return TokenType.TYPE
        "." -> return TokenType.MEMBER
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


fun getTypedVariableByMemberToken(token: Token, parser: TdlParser): String {
    val tokenStream = parser.tokenStream
    val memberTokenStart = token.tokenIndex
    var i = memberTokenStart - 1
    var dotTrigger = false

    while (i > 0) {
        val text = tokenStream[i].text
        if (tokenStream[i].channel == 0 && tokenStream[i].type != 3) {  // visible token
            if (dotTrigger)
                return tokenStream[i]!!.text
            if (text == ".")
                dotTrigger = true
        }
        i--
    }

    return ""
}


fun getParamsCount(token: Token, parser: TdlParser): Int {
    val startBracket = token.tokenIndex + 1
    var i = startBracket
    val tokenStream = parser.tokenStream
    var bracketsPairs = 0
    var paramsCount = 0
    var zeroCommaFlag = false

    while (i < tokenStream.size()) {
        when (tokenStream[i].text) {
            "(" -> bracketsPairs++
            ")" -> bracketsPairs--
            "," -> {
                if (bracketsPairs == 1)
                    paramsCount++
            }
            " " -> {}
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

fun exploreStatement(ctx: ParseTree, scope: Scope, parentName: String, parser: TdlParser): VisitErrors {
    val visitResults = VisitErrors()

    val leafs = getFlattenLeaf(ctx)
    for (leaf in leafs) {
        val name = leaf.text
        when (getTokenType(leaf, parser)) {
            TokenType.CALLABLE -> {
                val paramsCount = getParamsCount(leaf, parser)
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

fun exploreAssignment(ctx: TdlParser.AssignmentContext, scope: Scope, parentName: String, parser: TdlParser): VisitErrors {
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
        if (leafs.size == 1 && getTokenType(leafs.first(), parser) == TokenType.CALLABLE) {
            // if this is assigment with one entity on the right part
            val leaf = leafs.first()
            val name = leaf.text
            val params = getParamsCount(leaf, parser)
            val typeCallable = scope.getType(name, params)

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
                when (getTokenType(leaf, parser)) {
                    TokenType.CALLABLE -> {
                        val paramsCount = getParamsCount(leaf, parser)
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