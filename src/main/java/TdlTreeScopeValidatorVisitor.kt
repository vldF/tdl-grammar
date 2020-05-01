import org.antlr.v4.runtime.Token

class TdlTreeScopeValidatorVisitor(parser: TdlParser, private val globalScope: Scope) : TdlParserBaseVisitor<Unit>() {
    private val text: String = parser.inputStream.text

    override fun visitFunctionBody(ctx: TdlParser.FunctionBodyContext) {
        val name =
                if (ctx.parent is TdlParser.FunctionDeclarationContext)
                    (ctx.parent as TdlParser.FunctionDeclarationContext).simpleIdentifier().Identifier().text
                else {
                    super.visitFunctionBody(ctx)
                    return
                }
        val localScope = globalScope.getScope(name) ?: throw Exception("scope doesn't exist")

        for (leaf in getFlattenLeaf(ctx)) {
            val leafName = leaf.text
            when(getTokenType(leaf, text)) {
                TokenType.VARIABLE -> localScope.getVariable(leafName) ?: System.err.println("can't resolve variable ${leaf.text}")
                TokenType.CALLABLE -> {
                    val paramsCount = getParamsCount(leaf, text)
                    localScope.getCallable(leafName, paramsCount) ?: System.err.println("can't resolve callable ${leaf.text}")
                }
                TokenType.MEMBER -> {
                    val variableName = getTypedVariableByMemberToken(leaf, text)
                    val variable = localScope.getExemplar(variableName)
                    if (variable == null) {
                        System.err.println("can't resolve variable $variableName for member $leafName")
                    }
                }
                else -> println("$leafName skipped")
            }
        }

        super.visitFunctionBody(ctx)
    }



}