# TDL validator

Simple Test Dynamic Languagle checker. It uses antlr4 and allows:
1. Parse, build AST and validate TDL code from files
2. Name resolution (finding all unresolved objects)
3. Check unused objects (variables, fields, functions, types, invokes on) and get list of its
4. Find all errors (`unmatching arguments` for function and types, `unresolved` for variables, types, invokes on, `ambiguity`, `empty type`)

## Example of usage
```
val verifier = Verifier("examples/default/")  // set path to directory with .tdl files 
                                              // it dir contains two file: point.tdl and triangle.tdl
verifier.loadAndVerifyFile("triangle")  // load triangle.tdl; file point.tdl will be loaded automatically cuz triangle.tdl imports it

val errorsPoint = verifier.getErrors("point")  // get errors (unresolved, ambiguity, empty type and unmatching arguments) from point
val errorsTriangle = verifier.getErrors("triangle") // get errors from triangle
val unusedPoint = verifier.getUnused("point")  // get unused from point
val unusedTriangle = verifier.getUnused("triangle")  // get unused from triangle
```

## Simple doc
`Verifier.getErrors(name: String)` returns list of errors `List<Result>` for file `name.tdl`. 
`Result` is an abstract class with fields:
1. `entityName: String`. It is name of entity that creates error
2. `parentName: String`. It is name of parent of entity
3. `line: Int`. It is line when entity starts

Error's class may be one of this data class: 
1. `Unresolved(entityName: String, parentName: String, line: Int)`
2. `UnmathingArgument(entityName: String, parentName: String, line: Int, similar: EntityBase)`. Similar -- function/type/invoke on that has name `entityName`, but different arguments
3. `Ambiguity(entityName: String, parentName: String, line: Int)`
4. `EmptyType(entityName: String, parentName: String, line: Int)`

All of they has `Result` as supertype.

`Verifier.getUnused(name: String): List<UnusedStorage>`. 
`UnusedStorage` is a data class with fields:
1. `scopeName: String` -- name of scope ("global" for global scope or name of the function/invoke on/type.
2. `unusedVariables: List<Variable>`
3. `unusedFunctions: List<CallableEntity>`
4. `unusedTypes: List<CallableEntity>`
5. `unusedExemplars: List<Variable>`
6. `unusedInvokesOn: List<CallableEntity>`
7. `unusedFields: List<Variable>`

Any of this fields may be empty lists.

`EntityBase` is abstract class for describing all object in `Scope`. It has fields `name: String` and `isUsed: Boolean = false` (flag for marking entities that used in scope; all entity with `isUsed=false` will be returned by Verifier.getUnused())

`CallableEntity` is a data class that describes any entity which can be called. Contains `name: String` and `parameterNameList: List<Variable>`

`Variable` is a data class with fields `fields: List<String>?` and `reference: CallableEntity?` (if this variable is an exemplar of type, `fields` discribes available members of it, `reference` in this case is object of type of this exemplar)

