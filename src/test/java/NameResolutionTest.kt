import ast.NameResolution
import ast.objects.*
import ast.objects.expression.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NameResolutionTest {
    @Test
    fun basicTest() {
        val root = Root()
        val function1 = CallableEntity("fun1", listOf(Parameter("a")))
        val type1 = CallableEntity("type1", listOf())

        val var1Func1 = Variable("var1Func1")
        function1.addChild(var1Func1)

        val func1type1 = CallableEntity("func1type1", listOf())
        type1.addChild(func1type1)

        val exp1 = Expression("exp1")
        val exp1holder1 = CallableHolder("fun1", listOf(Parameter("a")))
        exp1.addChild(exp1holder1)

        root.addChild(function1)
        root.addChild(type1)
        root.addChild(exp1)

        CalculateScope().calculateScopeForNode(root, hashMapOf())

        val error = NameResolution().resolve(root)

        Assertions.assertTrue(error.isEmpty())
        Assertions.assertTrue((exp1.children["fun1"]!![0] as Holder).obj == function1)
    }
}