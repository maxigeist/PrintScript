package ingsis.interpreter.interpretStatement

import ingsis.components.statement.Declaration
import ingsis.components.statement.Statement
import ingsis.components.statement.StatementType
import ingsis.utils.Result

class DeclarationInterpreter : StatementInterpreter {
    override fun canHandle(statement: Statement): Boolean = statement.getStatementType() == StatementType.DECLARATION

    override fun interpret(
        statement: Statement,
        previousState: HashMap<String, Result>,
    ): Pair<HashMap<String, Result>, String?> {
        val declaration = statement as Declaration
        val variable = declaration.getVariable()
        val type = declaration.getType()
        previousState[variable.getName()] = Result(type, null)
        return Pair(previousState, null)
    }
}
