import ast.objects.*
import ast.objects.expression.Add
import ast.objects.expression.Constant
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScopeTest {
    private lateinit var root: Root
    private lateinit var function1: ParametredEntity
    private lateinit var type1: ParametredEntity
    private lateinit var var1: Variable

    @BeforeEach
    fun initTree() {
        root = Root()
        function1 = ParametredEntity("fun1", listOf(Parameter("a")))
        type1 = ParametredEntity("type1", listOf())
        var1 = Variable("var1")

        val var1Func1 = Variable("var1Func1")
        function1.addChild(var1Func1)

        val func1type1 = ParametredEntity("func1type1", listOf())
        type1.addChild(func1type1)

        val exp1var1 = Add(Constant(3), Constant(2))
        var1.addChild(exp1var1)

        root.addChild(function1)
        root.addChild(type1)
        root.addChild(var1)
    }

    @Test
    fun calcAllScope() {
        CalculateScope().calculateScopeForNode(root, HashMap())

        Assertions.assertEquals(3, root.scope.size)
        Assertions.assertEquals(5, function1.scope.size)
        Assertions.assertEquals(4, type1.scope.size)
        Assertions.assertEquals(4, var1.scope.size)

        Assertions.assertTrue(root.scope["fun1"]?.size == 1 && root.scope["fun1"]?.first() == function1)
        Assertions.assertTrue(root.scope["type1"]?.size == 1 && root.scope["type1"]?.first() == type1)
        Assertions.assertTrue(root.scope["var1"]?.size == 1 && root.scope["var1"]?.first() == var1)
    }
}