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
    private val invokesOn = mutableMapOf<String, CallableEntity>()
    private val children = mutableMapOf<String, Scope>()

    private fun addChild(name: String, scope: Scope) {
        children[name] = scope
    }

    fun getScope(name: String): Scope? = children[name]


    fun getVariable(name: String): Variable? {
        if(variables[name] == null && parent != null)
            return parent.getVariable(name)

        variables[name]?.isUsed = true
        return variables[name]
    }

    fun getFunction(name: String, paramsCount: Int) =
            functions[name]?.find { it.parameterNameList.size == paramsCount }

    fun getType(name: String, paramsCount: Int) = types[name]

    fun getInvokeOn(name: String, paramsCount: Int) = invokesOn[name]

    fun getCallable(name: String, paramsCount: Int): CallableEntity? {
        val res = when {
            functions[name]?.any{ it.parameterNameList.size == paramsCount } == true ->
                functions[name]!!.find { it.parameterNameList.size == paramsCount }

            invokesOn[name]!= null ->
                invokesOn[name]

            types[name]?.parameterNameList?.size == paramsCount ->
                types[name]

            else -> null
        }

        res?.isUsed = true

        return res
    }

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
}