import ast.objects.*
import ast.objects.expression.Add
import ast.objects.expression.Constant
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TreeTest {
    private lateinit var root: Root
    private lateinit var function1: CallableEntity
    private lateinit var type1: CallableEntity
    private lateinit var var1: Variable

    @BeforeEach
    fun initTree() {
        root = Root()
        function1 = CallableEntity("fun1", listOf(Parameter("a")))
        type1 = CallableEntity("type1", listOf())
        var1 = Variable("var1")

        val var1Func1 = Variable("var1Func1")
        function1.addChild(var1Func1)

        val func1type1 = CallableEntity("func1type1", listOf())
        type1.addChild(func1type1)

        val exp1var1 = Add(Constant(3), Variable("var1"))
        var1.addChild(exp1var1)

        root.addChild(function1)
        root.addChild(type1)
        root.addChild(var1)
    }

    @Test
    fun basicTest() {
        Assertions.assertTrue(root.children.size == 3)
        Assertions.assertTrue(function1.children.size == 1)
        Assertions.assertTrue(type1.children.size == 1)
        Assertions.assertTrue(var1.children.size == 1)
    }

    @Test
    fun scopeTest() {
        val newVar = Variable("testVar")
        function1.addChild(newVar)

        val scope = newVar.getBasicScope()
        Assertions.assertTrue(scope.size == 7)
        Assertions.assertTrue(scope.containsAll(listOf(var1, function1, type1)))
        //Assertions.assertFalse(var1.children.any{scope.contains(it.value)})
        //Assertions.assertFalse(type1.children.any{scope.contains(it.value)})
    }

    @Test
    fun nameResolutionTest() {
        val testingVar = Variable("testingVar")
        val testingInvisibleVar = Variable("invisibleVar")

        val aVarObjA = function1.parameterNameList[0]
        function1.addChild(testingVar)
        type1.addChild(testingInvisibleVar)
        Assertions.assertEquals(aVarObjA, testingVar.getEntityFromScopeByName("a"))
        Assertions.assertNull(testingVar.getEntityFromScopeByName("invisibleVar"))
    }
}
