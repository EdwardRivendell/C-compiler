package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    private Stack<TokenKind> tokenKindStack = new Stack<>();
    private Stack<Token> tokenStack = new Stack<>();
    private SymbolTable symbolTable;
    private Token temp_token;//whenReduce中用到的临时变量
    private TokenKind temp_kind;//whenReduce中用到的临时变量
    private String temp_id;//whenReduce中输出到symboltable的x变量

    @Override
    public void whenAccept(Status currentStatus) {
        // do nothing
    }
    @Override
    public void whenReduce(Status currentStatus, Production production) {

        if (production.body().size()==1 && "int".equals(production.body().get(0).getTermName())) {//D -> int
            tokenKindStack.push(TokenKind.fromString("int"));
        }
        else if (production.body().size()==2 && "id".equals(production.body().get(1).getTermName())){//S->D id
            temp_token=tokenStack.pop();
            temp_id=temp_token.getText();
            if (tokenKindStack.pop()==TokenKind.fromString("int")){
                if (!(symbolTable.has(temp_id))){
                    throw new RuntimeException("Record "+temp_id+" should have been added to the symbol table");
                }
                symbolTable.setIntType(temp_id);
            }

        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {//向tokenStack中添加token，在reduce的时候可以pop出来
        tokenStack.push(currentToken);

    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        this.symbolTable = table;
    }
}

