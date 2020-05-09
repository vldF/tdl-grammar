package ru.vldf.validator

import ru.vldf.validator.ast.objects.CallableEntity
import ru.vldf.validator.ast.objects.Variable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestDefault {
    @Test
    fun asOperator() {
        val verifier = Verifier("examples/default/")
        verifier.loadAndVerifyFile("triangle")  // point will be verified automatically

        val errorsExpectedPoint = setOf(
                Unresolved("p", "foo", 21),
                UnmatchingArgument("Point", "foo", 27, similarHolder)
        )
        val unusedExpectedPoint = setOf(
                UnusedStorage(
                        "foo",
                        unusedExemplars = listOf(Variable("ss"))
                )
        )
        val unusedExpectedTriangle = setOf(
                UnusedStorage(
                        "bar",
                        unusedVariables = listOf(
                                Variable("triangle"),
                                Variable("result")
                        )
                ),
                UnusedStorage(
                        "global",
                        unusedInvokesOn = listOf(CallableEntity("Triangle", listOf()))
                )
        )

        val errorsPoint = verifier.getErrors("point")
        val errorsTriangle = verifier.getErrors("triangle")
        val unusedPoint = verifier.getUnused("point")
        val unusedTriangle = verifier.getUnused("triangle")

        Assertions.assertEquals(errorsExpectedPoint, errorsPoint?.toSet())
        Assertions.assertEquals(setOf<Result>(), errorsTriangle?.toSet())

        Assertions.assertEquals(unusedExpectedPoint, unusedPoint?.toSet())
        Assertions.assertEquals(unusedExpectedTriangle, unusedTriangle?.toSet())
    }
}