import ast.objects.Variable

class BlockVisitor(private val localScope: Scope,
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