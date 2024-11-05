package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import javax.xml.stream.events.Characters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static cn.edu.hitsz.compiler.lexer.TokenKind.fromString;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;

    private String content;

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    private List<Token> tokens = new ArrayList<>();


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        try{
            content = Files.readString(Paths.get(path));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        String content = this.content;
        int length = content.length();
        int index = 0;

        while(index < length){
            char currentChar = content.charAt(index);

            //识别Space并跳过
            if (Character.isWhitespace(currentChar)){
                index++;
                continue;
            }

            //识别int
            if (content.startsWith("int",index)&&!Character.isLetterOrDigit(content.charAt(index+3))){
                tokens.add(Token.simple("int"));
                index += 3;
                continue;
            }

            //识别return
            if (content.startsWith("return",index)&&!Character.isLetterOrDigit(content.charAt(index+6))){
                tokens.add(Token.simple("return"));
                index += 6;
                continue;
            }

            //识别id
            if (Character.isLetter(currentChar)){
                int start = index;
                while ((index < length) && (Character.isLetterOrDigit(content.charAt(index)))){
                    index++;
                }

                String identifier = content.substring(start, index);

                //如果SymbolTable里没有这个id，则添加这个id；
                if (!(symbolTable.has(identifier))){
                    symbolTable.add(identifier);
                }

                tokens.add(Token.normal(fromString("id"), identifier));
                continue;
            }

            //识别整数常量
            if (Character.isDigit(currentChar)){
                int start = index;
                while ( (index < length) && (Character.isDigit(content.charAt(index)))){
                    index++;
                }

                String intConst = content.substring(start, index);
                tokens.add(Token.normal(fromString("IntConst"), intConst));
                continue;
            }
            //识别特殊符号
            switch(currentChar) {
                case '+':
                    tokens.add(Token.simple("+"));
                    break;

                case '-':
                    tokens.add(Token.simple("-"));
                    break;

                case '*':
                    tokens.add(Token.simple("*"));
                    break;

                case '=':
                    tokens.add(Token.simple("="));
                    break;

                case '(':
                    tokens.add(Token.simple("("));
                    break;

                case ')':
                    tokens.add(Token.simple(")"));
                    break;

                case ';':
                    tokens.add(Token.simple("Semicolon"));
                    break;

                default:
                    throw new RuntimeException("Unexpected character:" + currentChar);
            }

            //前进到下一个字符
            index++;
        }

        //词法分析结束，添加EOF
        tokens.add(Token.simple("$"));
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        return tokens;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }
}
