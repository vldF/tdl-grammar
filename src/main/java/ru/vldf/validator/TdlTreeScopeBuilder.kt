package ru.vldf.validator

import TdlParser
import TdlParserBaseVisitor
import ru.vldf.validator.ast.objects.CallableEntity
import ru.vldf.validator.ast.objects.Variable
import java.lang.IllegalStateException

/**
 * Scope builder. Visits all parts of code and calls BlockVisitor for every non-null block.
 * Use `getVisitResult()` for getting results
 *
 * @param parser TdlParser
 * @param globalScope global scope
 *
 * @throws IllegalStateException if invoke on's declaration earlier than type's declaration
 */
internal class TdlTreeScopeBuilder(private val parser: TdlParser, private val globalScope: Scope) : TdlParserBaseVisitor<Unit>() {
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
            BlockVisitor(localScope, name, paramNames, localVisitResult, parser).visitFunctionBody(body)
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
        val type = globalScope.getType(name) ?: throw IllegalStateException("invoke on earlier than type declaration: $name")
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
            BlockVisitor(localScope, name, paramsList, localVisitResult, parser, importParamsFromScope = true)
                    .visitFunctionBody(ctx.functionBody())
        }

        super.visitInvokeOnDeclaration(ctx)
    }

    // collecting global-scoped variables
    override fun visitTopLevelObject(ctx: TdlParser.TopLevelObjectContext) {
        // adding variable to global scope
        val assignment = ctx.declaration().assignment()
        if (assignment != null) {
            //if this is assignment
            val localVisitResult = exploreAssignment(assignment, globalScope, "global", parser)
            visitResult.addChild(localVisitResult)
        }

        super.visitTopLevelObject(ctx)
    }

    fun getVisitResult() = visitResult
}