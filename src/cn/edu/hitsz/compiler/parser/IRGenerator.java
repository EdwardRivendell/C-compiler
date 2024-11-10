package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    private SymbolTable symbolTable;
    private Stack<Object> operandStack = new Stack<>();//用来存放a b c IntConst 中间变量
    private Stack<TokenKind> optStack = new Stack<>();//存放操作符
    private List<Instruction> instructions = new ArrayList<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        if (currentToken.getKind()==TokenKind.fromString("id")){
            operandStack.push(currentToken.getText());
        }

        if (currentToken.getKind()==TokenKind.fromString("IntConst")){
            operandStack.push(Integer.parseInt(currentToken.getText()));
        }

        if (currentToken.getKind()==TokenKind.fromString("+")||currentToken.getKind()==TokenKind.fromString("-")||currentToken.getKind()==TokenKind.fromString("*")){
            optStack.push(currentToken.getKind());
        }
    }


    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO

        switch (production.head().getTermName()) {
            case "S":
                if (production.body().size() == 3 && "id".equals(production.body().get(0).getTermName())) {
                    if(!optStack.isEmpty()){
                        throw new RuntimeException("The operator stack is not empty");
                    }
                    Object a = operandStack.pop();
                    IRValue rhs;
                    if (a instanceof Integer){
                        rhs = IRImmediate.of((Integer) a);}
                    else {
                        rhs = IRVariable.named(a.toString());
                    }
                    Object b = operandStack.pop();
                    IRVariable lhs=IRVariable.named(b.toString());
                    instructions.add(Instruction.createMov(lhs, rhs));
                }

                if (production.body().size() == 2 && "return".equals(production.body().get(0).getTermName())) {
                    Object a = operandStack.pop();
                    instructions.add(Instruction.createRet(IRVariable.named(a.toString())));
                }
                break;
            case "E":
                if ((production.body().size()==3)&&("-".equals(production.body().get(1).getTermName())||"+".equals(production.body().get(1).getTermName()))) {//E->E-A, E->E+A
                    TokenKind opt = optStack.pop();
                    Object a = operandStack.pop();
                    IRValue rhs;
                    if (a instanceof Integer){
                        rhs = IRImmediate.of((Integer) a);}
                    else {
                        rhs = IRVariable.named(a.toString());
                    }
                    Object b = operandStack.pop();
                    IRValue lhs;
                    if (b instanceof Integer){
                        lhs = IRImmediate.of((Integer) b);}
                    else {
                        lhs = IRVariable.named(b.toString());
                    }
                    IRVariable result = IRVariable.temp();
                    switch (opt.toString()){
                        case "+":
                            instructions.add(Instruction.createAdd(result, lhs, rhs));
                            break;
                        case "-":
                            instructions.add(Instruction.createSub(result, lhs, rhs));
                            break;
                    }
                    operandStack.push(result);

                }
                break;
            case "A":
                if ((production.body().size()==3)&&("*".equals(production.body().get(1).getTermName()))) {//A->A*B
                    TokenKind opt = optStack.pop();
                    Object a = operandStack.pop();
                    IRValue rhs;
                    if (a instanceof Integer){
                        rhs = IRImmediate.of((Integer) a);}
                    else {
                        rhs = IRVariable.named(a.toString());
                    }
                    Object b = operandStack.pop();
                    IRValue lhs;
                    if (b instanceof Integer){
                        lhs = IRImmediate.of((Integer) b);}
                    else {
                        lhs = IRVariable.named(b.toString());
                    }

                    IRVariable result = IRVariable.temp();
                    switch (opt.toString()){
                        case "*":
                            instructions.add(Instruction.createMul(result, lhs, rhs));
                            break;
                    }
                    operandStack.push(result);
                }
                break;
            case "B":
                break;
            default:
                break;
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // do nothing
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.symbolTable = table;
    }

    public List<Instruction> getIR() {
        // TODO
        return instructions;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

