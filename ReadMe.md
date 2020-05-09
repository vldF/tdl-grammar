# TDL validator

Simple Test Dynamic Languagle checker. It use antlr4 and allows:
1. Parse, build AST and validate TDL code from file
2. Name resolution (finding all unresolved objects)
3. Check unused objects (variables, fields, functions, types, invokes on) and get list of its
4. Find all errors (`unmatching arguments` for function and types, `unresolved` for variables, types, invokes on, `ambiguity`, `empty type`)

##smple of usage##
```
fun main() {
    val verifier = Verifier("examples/default/")  // set path to directory with .tdl files 
                                                  // it dir contains two file: point.tdl and triangle.tdl
    verifier.loadAndVerifyFile("triangle")  // load triangle.tdl; file point.tdl will be loaded automatically cuz triangle.tdl imports it

    val errorsPoint = verifier.getErrors("point")  // get errors (unresolved, ambiguity, empty type and unmatching arguments) from point
    val errorsTriangle = verifier.getErrors("triangle") // get errors from triangle
    val unusedPoint = verifier.getUnused("point")  // get unused from point
    val unusedTriangle = verifier.getUnused("triangle")  // get unused from triangle
}
```

`Verifier.getErrors(name: String)` returns list of errors `List<Result>` for file `name.tdl`. 
`Result` is an abstract class with fields:
1. `entityName: String`. It is name of entity that throws error
2. `parentName: String`. It is name op parent of entity
3. `line: Int`. It is line when entity starts

Error's class may be one of this data class: 
1. `Unresolved(entityName: String, parentName: String, line: Int)`
2. `UnmathingArgument(entityName: String, parentName: String, line: Int, similar: EntityBase)`. Similar -- function/type/invoke on that has name `entityName`, but different arguments
3. `Ambiguity(entityName: String, parentName: String, line: Int)`
4. `EmptyType(entityName: String, parentName: String, line: Int)`
All of they has `Result` as supertype.

`verifier.getUnused(name: String)` returns `List<UnusedStorage>`. 
`UnusedStorage` is an data class with fields:
1. `scopeName: String` -- name of scope ("global" for global scope or name of the function/invoke on/type.

2-7: `unusedVariables: List<Variable>`, `unusedFunctions: List<CallableEntity>`, `unusedTypes: List<CallableEntity>`, `unusedExemplars: List<Variable>`, `unusedInvokesOn: List<CallableEntity>`, `unusedFields: List<Variable>`. Any of this fields may be empyu list.




