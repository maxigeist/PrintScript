import components.*
import components.ast.AST
import components.ast.ASTInterface
import kotlin.math.abs

class Parser : ParserInterface {
    private val typeComparator = ComparatorTokenType()
    private val valueComparator = ComparatorTokenValue()
    private val doesntMatterTypes = listOf(TokenType.PARENTHESIS, TokenType.KEYWORD)

    override fun parse(tokens: List<Token>): ASTInterface {
        var ast: ASTInterface = getEmptyAST()
        if(tokens.size > 1)
            for (token in tokens.subList(1, tokens.size)) {
                ast = add(token, ast)
            }
        return ast
    }

    fun transformDeclaration(tokens: List<Token>): ASTInterface {
        val decl = tokens[2]
        val identifier = tokens[1]
        val value = tokens[3]
        return AST(decl, listOf(getLeaf(identifier), getLeaf(value)))
    }

    fun transformAssignation(tokens: List<Token>): ASTInterface {
        val assignIndex : Int = findAssignIndex(tokens)
        val ident : ASTInterface = transformIdent(tokens.subList(0, assignIndex))
        val assign = tokens[assignIndex]
        val value = transformValue(tokens.subList(assignIndex+1, tokens.size-1))
        return AST(assign, listOf(ident, value))
    }
    fun transformFunction(tokens: List<Token>): ASTInterface {
        val function = tokens[0]
        val value = transformValue(tokens.subList(1, tokens.size-1))
        return AST(function, listOf(value))
    }

    private fun transformIdent(tokens: List<Token>): ASTInterface {
        if(tokens.size == 1) return getLeaf(tokens[0])
        return transformDeclaration(tokens)
    }

    private fun findAssignIndex(tokens: List<Token>): Int {
        for(i in 0..tokens.size){
            if(tokens[i].type == TokenType.ASSIGNATION) return i
        }
        return 0
    }

    private fun transformValue(tokens: List<Token>): ASTInterface {
        if(tokens.size == 1) return getLeaf(tokens[0])
        return getTree(tokens)
    }

    private fun getTree(tokens: List<Token>): ASTInterface {
        var ast = getEmptyAST()
        for (token in tokens) {
            ast = add(token, ast)
        }
        return ast
    }

    private fun getEmptyAST(): ASTInterface {
        return AST()
    }

    private fun getLeaf(token: Token): ASTInterface = AST(token)


    // lo que podes hacer es chequear con los hijos
    // sabes que si hay un identifier, un type o un const (o value) esos si o si
    // tienen que ser hoja, por lo que si se encuentran con otro, lo mandas como hermano
    private fun add(token: Token, ast: ASTInterface): ASTInterface {
        if(token.type in doesntMatterTypes) return ast
        if(ast.isEmpty()) return ast.addChildren(getLeaf(token))
        val compareTokens = compareValueAndType(token, ast)
        return if (rootIsBigger(compareTokens)) compWChildren(token, ast)
        else if (compareTokens == 1) AST(token, ast)
        else if (abs(compareTokens) == 2) ast
        else ast.addChildren(getLeaf(token))
    }

    private fun compareValueAndType(token: Token, ast: ASTInterface): Int {
        val compValue = valueComparator.compare(token, ast.token)
        val compToken = typeComparator.compare(token.type, ast.token!!.type)
        return if(compToken != 0) compToken
        else compValue
    }

    private fun compWChildren(token: Token, ast: ASTInterface) : ASTInterface {
        var tokEqChildren = false
        var tokGreaterChildren = false
        for(child in ast.children){
            val comp = compareValueAndType(token, child)
//            a chequear para que se mantenga el orden
            when (comp) {
                -1 -> return ast.removeChildren(child).addChildren(add(token, child))
                0 -> tokEqChildren = true
                else -> tokGreaterChildren = true
            }
        }
        if(tokGreaterChildren) return removeLastChild(ast, token)
        else if (tokEqChildren) return ast.addChildren(getLeaf(token))
        //ver que pasaria si tokEqChildren && tokGreaterChildren
        return ast.addChildren(getLeaf(token))
    }

    private fun removeLastChild(ast: ASTInterface, token: Token): ASTInterface {
        val children = ast.children
        val lastChild = children.last()
        return ast.removeChildren(lastChild).addChildren(add(token, lastChild))
    }

    private fun rootIsBigger(compareTokens: Int) = compareTokens == -1
}