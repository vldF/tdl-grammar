package ru.vldf.validator

import TdlParser
import TdlParserBaseVisitor
import ru.vldf.validator.ast.objects.Variable

/**
 * Visitor for blocks. When method visitBlock is called, add statements, expressions and assignments
 * will be visited
 *
 * @param localScope scope for storing entities of this block
 * @param name the name of this block (e.g. function's name)
 * @param paramNames list of params of function/invoke on has
 * @param visitErrors all errors will be added to this
 * @param parser TdlParser
 * @param importParamsFromScope if this is true, all fields of type with
 * name `name` will be added to `localScope`
 */
internal class BlockVisitor(private val localScope: Scope,
                            private val name: String,
                            paramNames: List<String>,
                            private val visitErrors: VisitErrors,
                            private val parser: TdlParser,
                            importParamsFromScope: Boolean = false
) : TdlParserBaseVisitor<Unit>() {

    init {
        if (importParamsFromScope) {
            val ref = localScope.getType(name)
            ref!!.parameterNameList.forEach {
                localScope.addVariable(it)
            }
        } else {
            paramNames.forEach { localScope.addVariable(Variable(it)) }
        }
    }

    override fun visitStatement(ctx: TdlParser.StatementContext) {
        if (ctx.assignment() != null || ctx.expression()?.asExpression() != null) {
            // this is assignment or as operator; for this case there is another way
            super.visitStatement(ctx)
            return
        }

        val results = exploreStatement(ctx, localScope, name, parser)
        visitErrors.addChild(results)
        super.visitStatement(ctx)

    }

    override fun visitAssignment(ctx: TdlParser.AssignmentContext) {
        val results = exploreAssignment(ctx, localScope, name, parser)
        visitErrors.addChild(results)
        super.visitAssignment(ctx)
    }

    override fun visitAsExpression(ctx: TdlParser.AsExpressionContext) {
        val results = exploreAsOperator(ctx, localScope, name, parser)
        visitErrors.addChild(results)
        super.visitAsExpression(ctx)
    }
}