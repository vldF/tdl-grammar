import ast.objects.CallableEntity
import ast.objects.Parameter
import ast.objects.Variable
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTree


class TdlTreeScopeBuilder(parser: TdlParser, private val globalScope: Scope) : TdlParserBaseVisitor<Unit>() {
    private val text = parser.inputStream.text

    init {
        globalScope.addType(CallableEntity("String", listOf()))
        globalScope.addType(CallableEntity("Integer", listOf()))
    }

    override fun visitFunctionBody(ctx: TdlParser.FunctionBodyContext?) {
        val leafs = getFlattenLeaf(ctx as ParseTree)
        val name =
                if (ctx.parent is TdlParser.FunctionDeclarationContext)
                    (ctx.parent as TdlParser.FunctionDeclarationContext).simpleIdentifier().Identifier().text
                else {
                    super.visitFunctionBody(ctx)
                    return
                }

        val localScope = globalScope.getScope(name) ?: throw Exception("function's body visited earlier than declaration")

        for (leaf in leafs) {
            val name = leaf.text
            when (getTokenType(leaf, text)) {
                TokenType.VARIABLE_DECLARATION -> {
                    val referenceType = getVariableAssigmentType(leaf)
                    if (referenceType == null)
                        localScope.addVariable(Variable(name))
                    else{
                        val exemplar = Variable(name)
                        val paramList = localScope.getType(referenceType)?.parameterNameList?.map { it.name }
                        if (paramList == null)
                            System.err.println("type not found: $referenceType")
                        else
                            exemplar.fields = paramList
                        localScope.addExemplar(exemplar)
                    }
                }
            }
        }

        super.visitFunctionBody(ctx)
    }

    override fun visitFunctionDeclaration(ctx: TdlParser.FunctionDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val localScope = Scope(name, globalScope)
        val params = getParametersNames(ctx.parameters())

        params.forEach { localScope.addVariable(Variable(it)) }
        globalScope.addFunction(CallableEntity(name, params.map { Parameter(it) }))

        super.visitFunctionDeclaration(ctx)
    }

    private fun getParametersNames(ctx: TdlParser.ParametersContext): List<String> {
        return ctx.children.filterIsInstance<TdlParser.ParameterContext>().map { it.text }
    }

    private fun getVariableAssigmentType(token: Token): String? {
        val definitionStart = token.stopIndex + 1
        val definitionEnd = text.indexOf(";", startIndex = definitionStart)
        val definition = text.slice(definitionStart until definitionEnd)
        if (definition.contains(" as ")) {
            // casting var to type
            val type = definition.split(" as ")[1]
            return type.replace(" ", "")
        }
        return null
    }

    override fun visitTypeDeclaration(ctx: TdlParser.TypeDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val params = ctx.primaryConstructor().parameters().parameter().map { Parameter(it.text) }

        globalScope.addType(CallableEntity(name, params))

        super.visitTypeDeclaration(ctx)
    }

    // collecting global-scoped variables
    override fun visitTopLevelObject(ctx: TdlParser.TopLevelObjectContext) {
        val name = (ctx.children.find {
            it is TdlParser.DeclarationContext && it.assignment() != null
        } as TdlParser.DeclarationContext?)?.assignment()?.children?.get(0)?.text

        if (name != null) {
            globalScope.addVariable(Variable(name))
        }
        super.visitTopLevelObject(ctx)
    }
}