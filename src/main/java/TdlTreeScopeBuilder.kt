import ast.objects.CallableEntity
import ast.objects.Parameter
import ast.objects.Variable
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTree


class TdlTreeScopeBuilder(private val parser: TdlParser, private val globalScope: Scope) : TdlParserBaseVisitor<Unit>() {
    private val text = parser.inputStream.text

    init {
        globalScope.addType(CallableEntity("String", listOf()))
        globalScope.addType(CallableEntity("Integer", listOf()))
    }


    override fun visitFunctionDeclaration(ctx: TdlParser.FunctionDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val params = ctx.parameters().parameter().map{ Parameter(it.text) }
        val function = CallableEntity(name, params)
        globalScope.addFunction(function)

        val body = ctx.functionBody()
        if (body != null)
            BlockVisitor(globalScope, ctx, text, parser).visitFunctionBody(body)
        super.visitFunctionDeclaration(ctx)
    }

    override fun visitTypeDeclaration(ctx: TdlParser.TypeDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val params = ctx.primaryConstructor().parameters().parameter().map { Parameter(it.text) }

        if (params.isEmpty()) {
            System.err.println("empty type: $name")
            super.visitTypeDeclaration(ctx)
            return
        }

        globalScope.addType(CallableEntity(name, params))

        super.visitTypeDeclaration(ctx)
    }

    override fun visitInvokeOnDeclaration(ctx: TdlParser.InvokeOnDeclarationContext) {
        val name = ctx.simpleIdentifier().Identifier().text
        val type = globalScope.getType(name)
        if (type == null) {
            System.err.println("invoke on can't be earlier than type declaration: $name")
            return super.visitInvokeOnDeclaration(ctx)
        }
        val invokeOn = CallableEntity(name, type.parameterNameList)
        globalScope.addInvokeOn(invokeOn)
        super.visitInvokeOnDeclaration(ctx)
    }

    // collecting global-scoped variables todo
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