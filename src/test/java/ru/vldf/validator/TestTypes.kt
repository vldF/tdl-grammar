package ru.vldf.validator

import ru.vldf.validator.ast.objects.Variable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestTypes {
    @Test
    fun types() {
        val verifier = Verifier("examples/types/")
        verifier.loadAndVerifyFile("types")
        val errorsExpected = setOf(
                EmptyType("A", "global", 1),
                Unresolved("A", "main", 10),
                UnmatchingArgument("B", "main", 13, similarHolder)
        )
        val unusedExpected = setOf(
                UnusedStorage(
                        "main",
                        unusedExemplars = listOf(
                                Variable("b")
                        )
                ),
                UnusedStorage(
                        "B",
                        unusedFields = listOf(
                                Variable("x")
                        )
                ),
                UnusedStorage(
                        "C",
                        unusedFields = listOf(
                                Variable("y"),
                                Variable("z")
                        )
                )
        )

        val errors = verifier.getLastErrors()!!.getAll()
        val unused = verifier.getUnused("types")
        Assertions.assertEquals(errorsExpected, errors.toSet())
        Assertions.assertEquals(unusedExpected, unused?.toSet())
    }

}