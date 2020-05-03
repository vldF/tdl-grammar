import TokenType.*
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
        val paramNames = ctx.parameters().parameter().map{ it.text }
        val params = paramNames.map{ Parameter(it) }

        val function = CallableEntity(name, params)
        globalScope.addFunction(function)

        val body = ctx.functionBody()
        if (body != null)
            BlockVisitor(globalScope, name, paramNames, text, parser).visitFunctionBody(body)
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
        val type = globalScope.getType(name) ?: throw Exception("invoke on earlier than type declaration: $name")
        val paramsList = type.parameterNameList.map { it.name }

        val invokeOn = CallableEntity(name, listOf())
        globalScope.addInvokeOn(invokeOn)

        if(ctx.functionBody() != null)
            BlockVisitor(globalScope, name, paramsList, text, parser).visitFunctionBody(ctx.functionBody())

        super.visitInvokeOnDeclaration(ctx)
    }

    // collecting global-scoped variables todo
    override fun visitTopLevelObject(ctx: TdlParser.TopLevelObjectContext) {
        // adding variable to global scope
        val variableName = (ctx.children.find {
            it is TdlParser.DeclarationContext && it.assignment() != null
        } as TdlParser.DeclarationContext?)?.assignment()?.children?.get(0)?.text


        if (variableName != null) {
            val leafs = getFlattenLeaf(ctx.declaration().assignment().expression())
            if (leafs.size == 1) {
                for (leaf in leafs) {
                    val name = leaf.text
                    when (getTokenType(leaf, text)) {
                        CALLABLE -> {
                            val params = getParamsCount(leaf, text)
                            val callable = globalScope.getInvokeOn(name, params)
                            if (callable == null)
                                System.err.println("unresolved callable: $name")
                        }
                        VARIABLE -> {
                            val variable = globalScope.getVariable(name)
                            if (variable == null)
                                System.err.println("unresolved variable: $name")
                        }
                        VARIABLE_DECLARATION -> TODO()
                        TYPE -> TODO()
                        MEMBER -> TODO()
                        CAST -> TODO()
                    }
                }
            }
            for (leaf in leafs) {
                val name = leaf.text
                when (getTokenType(leaf, text)) {
                    VARIABLE -> {
                        if (globalScope.getVariable(name) == null)
                            System.err.println("unresolved global variable: $name")
                    }
                    MEMBER -> {
                        val exemplarName = getTypedVariableByMemberToken(leaf, text)
                        val exemplar = globalScope.getExemplar(exemplarName)
                        val reference = exemplar?.reference

                        if (exemplar == null)
                            System.err.println("unresolved type exemplar $exemplarName")
                        else if (reference?.parameterNameList?.find { it.name == name } == null)
                            System.err.println("unresolved global member $name of type $exemplarName")
                    }
                    CALLABLE -> {
                        val paramsCount = getParamsCount(leaf, text)
                        val callable = globalScope.getCallable(name, paramsCount)

                        if (callable == null) {
                            if (globalScope.getCallable(name) == null)
                                System.err.println("unresolved callable: $name")
                            else
                                System.err.println("unmatching arguments: $name")
                        }
                    }
                    CAST -> {
                        val asExpression = ctx
                                .declaration()
                                .assignment()
                                .expression()
                                .asExpression()
                        val leftName = asExpression.additiveExpression().text
                        val typeName = asExpression.type().text

                        if (globalScope.getVariable(leftName) == null)
                            System.err.println("unresolved global variable: $leftName")
                        if (globalScope.getType(typeName) == null)
                            System.err.println("unresolved type: $leftName")
                    }
                    TYPE -> {
                        val ref = globalScope.getType(name)
                        if (ref == null)
                            System.err.println("unresolved type: $name")
                        else {
                            val exemplar = Variable(variableName)
                            exemplar.reference = ref
                            exemplar.fields = ref.parameterNameList.map { it.name }
                            globalScope.addExemplar(exemplar)
                        }
                    }
                    else -> println("skipped global $name")
                }
            }

        }
        super.visitTopLevelObject(ctx)
    }
}