import ast.objects.CallableEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestImport {
    @Test
    fun import() {
        val verifier = Verifier("examples/import/")
        verifier.loadAndVerifyFile("second")  // file `first` must be verified automatically

        val errorsExpectedSecond = setOf(
                UnmatchingArgument("foo", "bar", 9, similarHolder),
                Unresolved("bad", "IMPORTS", 0)
        )

        val unusedExpectedFirst = setOf(
                UnusedStorage(
                        "global",
                        unusedFunctions = listOf(
                                CallableEntity("baz", listOf())
                        )
                )
        )

        val errorsSecond = verifier.getErrors("second")
        val errorsFirst = verifier.getErrors("first")
        val unusedFirst = verifier.getUnused("first")
        val unusedSecond = verifier.getUnused("second")

        Assertions.assertEquals(listOf<Result>(), errorsFirst)
        Assertions.assertEquals(errorsExpectedSecond, errorsSecond?.toSet())

        Assertions.assertEquals(unusedExpectedFirst, unusedFirst?.toSet())
        Assertions.assertEquals(listOf<UnusedStorage>(), unusedSecond)
    }
}