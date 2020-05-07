import ast.objects.CallableEntity
import ast.objects.Variable


class TdlTreeScopeBuilder(private val parser: TdlParser, private val globalScope: Scope) : TdlParserBaseVisitor<Unit>() {
    private val text = parser.inputStream.text
    private val visitResult = VisitErrors()

    init {
        globalScope.addType(CallableEntity("String", listOf()))
        globalScope.addType(CallableEntity("Integer", listOf()))
    }

    override fun visitFunctionDeclaration(ctx: TdlParser.FunctionDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val paramNames = ctx.parameters().parameter().map{ it.text }
        val params = paramNames.map{ Variable(it) }

        val function = CallableEntity(name, params)
        if (!globalScope.addFunction(function)) {
            val result = Ambiguity(name, "global", ctx.getStart().line)
            visitResult.add(result)
            // end of parsing this tree
            return
        }

        val body = ctx.functionBody()
        if (body != null) {
            val localVisitResult = VisitErrors()
            visitResult.addChild(localVisitResult)
            val localScope = Scope(name, globalScope)
            BlockVisitor(localScope, name, paramNames, text, localVisitResult, parser).visitFunctionBody(body)
        }
        super.visitFunctionDeclaration(ctx)
    }

    override fun visitTypeDeclaration(ctx: TdlParser.TypeDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val params = ctx.primaryConstructor().parameters().parameter().map { Variable(it.text) }

        if (params.isEmpty()) {
            visitResult.add(
                    EmptyType(name, "global", ctx.start.line)
            )
            super.visitTypeDeclaration(ctx)
            return
        }

        if(!globalScope.addType(CallableEntity(name, params)))
            visitResult.add(
                    Ambiguity(name, "global", ctx.start.line)
            )

        super.visitTypeDeclaration(ctx)
    }

    override fun visitInvokeOnDeclaration(ctx: TdlParser.InvokeOnDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val type = globalScope.getType(name) ?: throw Exception("invoke on earlier than type declaration: $name")
        val paramsList = type.parameterNameList.map { it.name }

        val invokeOn = CallableEntity(name, listOf())
        if(!globalScope.addInvokeOn(invokeOn))
            visitResult.add(
                    Ambiguity(name, "global", ctx.start.line)
            )

        if(ctx.functionBody() != null) {
            val localVisitResult = VisitErrors()
            visitResult.addChild(localVisitResult)
            val localScope = Scope(name, globalScope)
            val thisVariable = Variable("this")
            thisVariable.reference = type
            localScope.addExemplar(thisVariable)
            BlockVisitor(localScope, name, paramsList, text, localVisitResult, parser, importParamsFromScope = true)
                    .visitFunctionBody(ctx.functionBody())
        }

        super.visitInvokeOnDeclaration(ctx)
    }

    // collecting global-scoped variables todo
    override fun visitTopLevelObject(ctx: TdlParser.TopLevelObjectContext) {
        // adding variable to global scope

        val assignment = ctx.declaration().assignment()
        if (assignment != null) {
            //if this is assignment
            val localVisitResult = exploreAssignment(assignment, globalScope, text, "global", parser)
            visitResult.addChild(localVisitResult)
        }

        super.visitTopLevelObject(ctx)
    }

    fun getVisitResult() = visitResult
}