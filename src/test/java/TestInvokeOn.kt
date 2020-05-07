import ast.objects.CallableEntity
import ast.objects.Variable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestInvokeOn {
    @Test
    fun invokeOns() {
        val verifier = Verifier("examples/invokesOn/")
        verifier.loadAndVerifyFile("invokesOn")

        val errorsExpected = setOf(
                Ambiguity("v", "A", 8),
                Ambiguity("v", "B", 18),
                Unresolved("w", "A", 9),
                Unresolved("nonExisted", "B", 17),
                Unresolved("w", "B", 19),
                Unresolved("unres", "B", 21)
        )
        val unusedExpected = setOf(
                UnusedStorage(
                        "global",
                        unusedInvokesOn = listOf(
                                CallableEntity("A", listOf()),
                                CallableEntity("B", listOf())
                        )
                ),
                UnusedStorage(
                        "A",
                        unusedVariables = listOf(
                                Variable("v"), Variable("t"), Variable("z")
                        )
                ),
                UnusedStorage(
                        "B",
                        unusedVariables = listOf(
                                Variable("y"), Variable("z"), Variable("t"), Variable("j")
                        )
                ),
                UnusedStorage(
                        "B",
                        unusedFields = listOf(
                                Variable("y"), Variable("z")
                        )
                )
        )

        val errors = verifier.getLastErrors()!!.getAll()
        val unused = verifier.getUnused("invokesOn")
        Assertions.assertEquals(errorsExpected, errors.toSet())
        Assertions.assertEquals(unusedExpected, unused?.toSet())
    }

}