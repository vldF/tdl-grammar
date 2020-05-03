import ast.objects.Variable

class BlockVisitor(private val globalScope: Scope,
                   name: String,
                   paramNames: List<String>,
                   private val text: String,
                   private val parser: TdlParser
) : TdlParserBaseVisitor<Unit>() {
    private val localScope: Scope = Scope(name, globalScope)

    init {
        paramNames.forEach { localScope.addVariable(Variable(it)) }
    }

    override fun visitStatement(ctx: TdlParser.StatementContext) {
        if (ctx.assignment() != null || ctx.expression()?.asExpression() != null) {
            // this is assignment or as operator; for this case there is another way
            super.visitStatement(ctx)
            return
        }

        val leafs = getFlattenLeaf(ctx)
        for (leaf in leafs) {
            val name = leaf.text
            when (getTokenType(leaf, text)) {
                TokenType.CALLABLE -> {
                    val paramsCount = getParamsCount(leaf, text)
                    if (localScope.getCallable(name, paramsCount) == null)
                        if (localScope.getCallable(name) != null)
                            System.err.println("unmatching arguments: $name")
                        else
                            System.err.println("unresolved: $name")
                }
                TokenType.VARIABLE -> {
                    if (localScope.getVariable(name) == null)
                        System.err.println("unresolved: $name")
                }
                TokenType.MEMBER -> {
                    val exemplar = localScope.getExemplar(getTypedVariableByMemberToken(leaf, text))
                    if (exemplar == null)
                        System.err.println("unresolved: $name")
                }
                else -> {
                    println("ignored statement: $name")
                }
            }
        }

        super.visitStatement(ctx)
    }

    override fun visitAssignment(ctx: TdlParser.AssignmentContext) {
        val variableName = ctx.assignableExpression().simpleIdentifier().Identifier().text
        val expression = ctx.expression()
        // if cast (newVar = var as Type)
        if (expression.asExpression() != null) {
            val typeName = expression.asExpression().type().text
            val callableType = globalScope.getType(typeName)

            if (callableType == null) {
                System.err.println("type $typeName doesn't exist")
            } else {
                val nameOfVariableOnTheLeft = ctx.expression()
                        .asExpression()
                        .additiveExpression()
                        .text
                val variableOnTheLeft = localScope.getVariable(nameOfVariableOnTheLeft)
                if (variableOnTheLeft == null)
                    System.err.println("unresolved: $nameOfVariableOnTheLeft")
                else {
                    val variable = Variable(variableName)
                    variable.fields = callableType.parameterNameList.map { it.name }
                    variable.reference = callableType
                    localScope.addExemplar(variable)
                }
            }
        } else{ // all non-as assignments
            val leafs = getFlattenLeaf(expression)
            if (leafs.size == 1 && getTokenType(leafs.first(), text) == TokenType.CALLABLE) {
                // if this is assigment with one entity on the right part
                val leaf = leafs.first()
                val name = leaf.text
                val params = getParamsCount(leaf, text)
                val typeCallable = localScope.getType(name, params)

                //todo refactor?
                if (typeCallable != null) {
                    // if this is type constructor
                    val variable = Variable(variableName)
                    variable.fields = typeCallable.parameterNameList.map { it.name }
                    variable.reference = typeCallable
                    localScope.addExemplar(variable)
                } else if (localScope.getCallable(name, params) == null) {
                    //if this is access to exemplar?
                    val reference = localScope.getExemplar(name)?.reference
                    if (reference == null || localScope.getInvokeOn(reference.name) == null)
                        if (localScope.getCallable(name) != null)
                            System.err.println("unmatching arguments: $name")
                        else
                            System.err.println("unresolved callable: $name")
                }  else {
                    // this is function call, invoke on, ...
                    val callable = localScope.getCallable(name, params)
                    if (callable == null)
                        System.err.println("unresolved: $name")
                    val variable = Variable(variableName)
                    localScope.addVariable(variable)
                }
            } else {
                // if this is assigment of expression
                for (leaf in leafs) {
                    val name = leaf.text
                    when (getTokenType(leaf, text)) {
                        TokenType.CALLABLE -> {
                            val paramsCount = getParamsCount(leaf, text)
                            if (localScope.getCallable(name, paramsCount) == null) {
                                if (localScope.getCallable(name) == null)
                                    System.err.println("unresolved: $name")
                                else
                                    System.err.println("unmatching arguments: $name")
                            }
                        }
                        TokenType.VARIABLE -> {
                            if (localScope.getVariable(name) == null) {
                                System.err.println("unresolved: $name")
                            }
                        }
                        TokenType.MEMBER -> {
                            val typedVariable = getTypedVariableByMemberToken(leaf, text)
                            if (localScope.getVariable(typedVariable) == null) {
                                System.err.println("unresolved: $name")
                            }
                        }
                        else -> {
                            println("skipped resolve of $name")
                        }
                    }
                }
            }
        }

        super.visitAssignment(ctx)
    }

    override fun visitAsExpression(ctx: TdlParser.AsExpressionContext) {
        //todo 0 index problem
        val stream = parser.tokenStream
        var i = ctx.start.tokenIndex
        while (i >= 0) {
            val prevToken = stream[i]
            if (prevToken.type == 15) { //todo; 15 is SPACE type
                super.visitAsExpression(ctx)
                return
            } else if (prevToken.type == parser.getTokenType("NL")) {
                break
            }
            i--
        }

        if (i == 0) {
            super.visitAsExpression(ctx)
            return
        }

        val variableName = ctx.additiveExpression().text
        val typeName = ctx.type().text
        val type = localScope.getType(typeName)

        if (type == null) {
            System.err.println("unresolved type: $typeName")
            super.visitAsExpression(ctx)
            return
        }

        val variable = Variable(variableName)
        variable.fields = type.parameterNameList.map { it.name }
        variable.reference = type
        localScope.addExemplar(variable)
        super.visitAsExpression(ctx)
    }
}