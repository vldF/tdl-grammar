import ast.objects.CallableEntity
import ast.objects.Parameter
import ast.objects.Variable
import org.antlr.v4.runtime.tree.ParseTree

class TdlTreeScopeBuilder(parser: TdlParser, private val globalScope: Scope) : TdlParserBaseVisitor<Unit>() {
    private val text = parser.inputStream.text


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
            when (getTokenType(leaf, text)) {
                TokenType.VARIABLE_DECLARATION -> {
                    localScope.addVariable(Variable(leaf.text))
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