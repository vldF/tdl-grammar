import ast.objects.CallableEntity
import ast.objects.Variable
import java.lang.Exception

class Scope(name: String, baseScope: Scope?) {
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

    private fun addChild(name: String, scope: Scope) {
        children[name] = scope
    }

    fun getScope(name: String): Scope? = children[name]


    fun getVariable(name: String): Variable? {
        return variables[name] ?: parent?.getVariable(name)
    }

    fun getFunction(name: String, paramsCount: Int): CallableEntity? =
            functions[name]?.find { it.parameterNameList.size == paramsCount } ?: parent?.getFunction(name, paramsCount)

    fun getType(name: String, paramsCount: Int = -1): CallableEntity? {
        return if (types[name] != null && (types[name]?.parameterNameList?.size == paramsCount || paramsCount == -1))
            types[name]
        else
            parent?.getType(name, paramsCount)
    }

    fun getInvokeOn(name: String, paramsCount: Int): CallableEntity? = invokesOn[name] ?: parent?.getInvokeOn(name, paramsCount)

    fun getCallable(name: String, paramsCount: Int): CallableEntity? {
        val res = when {
            functions[name]?.any{ it.parameterNameList.size == paramsCount } == true ->
                functions[name]!!.find { it.parameterNameList.size == paramsCount }

            invokesOn[name]!= null ->
                invokesOn[name]

            types[name]?.parameterNameList?.size == paramsCount ->
                types[name]

            else -> null
        } ?: return parent?.getCallable(name, paramsCount)

        res.isUsed = true
        return res
    }

    fun getExemplar(name: String): Variable? = typesExemplar[name] ?: parent?.getExemplar(name)

    fun addVariable(variable: Variable) {
        if (variables[variable.name] != null) {
            throw Exception("ambiguity")
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
