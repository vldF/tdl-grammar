import ast.UnusedStorage
import ast.objects.CallableEntity
import ast.objects.EntityBase
import ast.objects.Variable
import java.lang.Exception

class Scope(val name: String, baseScope: Scope?) {
    constructor(): this("root", null)

    init {
        baseScope?.addChild(name, this)
    }

    private val parent: Scope? = baseScope
    private val variables = mutableMapOf<String, Variable>()
    private val functions = mutableMapOf<String,MutableList<CallableEntity>>()
    private val types = mutableMapOf<String, CallableEntity>()
    private val typesExemplar = mutableMapOf<String, Variable>()
    private val invokesOn = mutableMapOf<String, CallableEntity>()
    private val children = mutableMapOf<String, Scope>()

    fun getUnused(): List<UnusedStorage> {
        val unusedVariables = variables.values.filter { !it.isUsed }
        val unusedFunctions = functions.values.map { it.filter { f -> !f.isUsed } }.flatten()
        val unusedTypes = types.values.filter {
            !it.isUsed && (parent != null || it.name != "String" && it.name != "Integer")
        }
        val unusedExemplars = typesExemplar.values.filter { !it.isUsed }
        val unusedInvokesOn = invokesOn.values.filter { !it.isUsed }

        val storage = UnusedStorage(
                name,
                unusedVariables,
                unusedFunctions,
                unusedTypes,
                unusedExemplars,
                unusedInvokesOn
        )

        val unusedInChildren = children.values.flatMap { it.getUnused() }
        val res = mutableListOf<UnusedStorage>()
        res.add(storage)
        res.addAll(unusedInChildren)

        return res
    }

    private fun addChild(name: String, scope: Scope) {
        children[name] = scope
    }

    fun getScope(name: String): Scope? = children[name]


    fun getVariable(name: String): Variable? {
        val variable = variables[name] ?: typesExemplar[name] ?: parent?.getVariable(name)
        variable?.isUsed = true
        return variable
    }

    fun getType(name: String, paramsCount: Int = -1): CallableEntity? {
        val type = if (types[name] != null && (types[name]?.parameterNameList?.size == paramsCount || paramsCount == -1))
            types[name]
        else
            parent?.getType(name, paramsCount)
        type?.isUsed = true
        return type
    }

    fun getInvokeOn(name: String): CallableEntity? {
        val invokeOn = invokesOn[name] ?: parent?.getInvokeOn(name)
        invokeOn?.isUsed = true
        return invokeOn
    }

    fun getCallable(name: String, paramsCount: Int): CallableEntity? {
        val res = when {
            functions[name]?.any{ it.parameterNameList.size == paramsCount } == true -> {
                functions[name]!!.find { it.parameterNameList.size == paramsCount }
            }

            typesExemplar[name]?.reference != null -> {
                val refName = typesExemplar[name]!!.reference!!.name
                this.getInvokeOn(refName)?.isUsed = true
                typesExemplar[name]?.isUsed = true
                typesExemplar[name]?.reference
            }


            types[name]?.parameterNameList?.size == paramsCount ->
                types[name]

            else -> null
        } ?: return parent?.getCallable(name, paramsCount)

        res.isUsed = true
        return res
    }

    fun getCallable(name: String): CallableEntity? = functions[name]?.first()
            ?: typesExemplar[name]?.reference
            ?: types[name]
            ?: parent?.getCallable(name)

    fun getExemplar(name: String): Variable? {
        val exemplar = typesExemplar[name] ?: parent?.getExemplar(name)
        exemplar?.isUsed = true
        invokesOn[exemplar?.reference?.name]?.isUsed = true
        return exemplar
    }

    fun addVariable(variable: Variable) {
        if (variables[variable.name] != null) {
            throw Exception("ambiguity ${variable.name}")
        }
        variables[variable.name] = variable
    }

    fun addFunction(func: CallableEntity) {
        when {
            functions[func.name]?.any { it.parameterNameList.size == func.parameterNameList.size } == true -> {
                throw Exception("ambiguity")
            }
            functions[func.name] != null -> {
                functions[func.name]!!.add(func)
            }
            else -> {
                functions[func.name] = mutableListOf(func)
            }
        }

    }

    fun addType(type: CallableEntity) {
        if (types[type.name] != null) {
            throw Exception("ambiguity")
        }
        types[type.name] = type
    }

    fun addInvokeOn(invoke: CallableEntity) {
        if (invokesOn[invoke.name] != null) {
            throw Exception("ambiguity")
        }
        invokesOn[invoke.name] = invoke

    }

    fun addExemplar(variable: Variable) {
        if (typesExemplar[variable.name] != null) {
            throw Exception("ambiguity") // todo
        }
        typesExemplar[variable.name] = variable
    }
}
