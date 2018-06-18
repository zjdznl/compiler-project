package latex;

import token.Token;
import utils.Config;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static token.TokenType.*;


//标识符（符号表），关键字，数字常量，界符，操作符，注释
public class Latex {
    private static String digitalDFA[] = {
            "#d######",
            "#d.#e###",
            "###d####",
            "###de###",
            "#####-+d",
            "#######d",
            "#######d",
            "#######d"
    };
    private static String keyWord[] = {
            "int", "real", "if", "then", "else", "where"
    };
    //操作符或界符
    private static char opOrDeli[] = {'+', '-', '/', '*', '=', '<', '>', '!', ',', ';', '(', ')', '{', '}'};
    public List<Point> animatePos = new ArrayList<>();
    private String text ;
    //private String text ="int i=3";
    private HashMap<String, Integer> symbolTable = new HashMap<>();
    public List<Token> tokenList = new ArrayList<>();
    private int symbolPos;

    public Latex(String str) {
        text = str;
    }

    public Latex() {
        text = Config.DEFAULT_CODE;
    }

    public static void main(String[] args) {
        Latex test = new Latex( "19\nif(a==1)i=2;\n //test" );
        test.LetexAnalyze();
        for (Token token : test.tokenList) {
            System.out.println(token);
        }
//        test.GetAnimatePos();
    }

    public static String[] getDigitalDFA() {
        return digitalDFA;
    }

    public static void setDigitalDFA(String[] digitalDFA) {
        Latex.digitalDFA = digitalDFA;
    }

    public static String[] getKeyWord() {
        return keyWord;
    }

    public static void setKeyWord(String[] keyWord) {
        Latex.keyWord = keyWord;
    }

    public static char[] getOpOrDeli() {
        return opOrDeli;
    }

    public static void setOpOrDeli(char[] opOrDeli) {
        Latex.opOrDeli = opOrDeli;
    }

    public void GetAnimatePos() {
        String tempText[] = text.split( "\n" );
        int lineStartPos[] = new int[tempText.length + 1];
        for (int line = 0; line <= tempText.length; line++) lineStartPos[line] = 0;
        for (int line = 2; line <= tempText.length; line++)
            lineStartPos[line] = lineStartPos[line - 1] + tempText[line - 2].length() + 1;
        for (int i = 1; i <= tempText.length; i++) {
            //System.out.println(lineStartPos[i]);
        }
        for (int i = 0; i < tokenList.size(); i++) {
            int tempX = lineStartPos[tokenList.get( i ).getTokenLine()] + tokenList.get( i ).getTokenPos();
            int tempY = tempX + tokenList.get( i ).getTokenName().length() - 1;
            //System.out.println(tempX+"-"+tempY);
            animatePos.add( new Point( tempX, tempY ) );
        }
    }

    public void LetexAnalyze() {
        symbolTable.clear();
        tokenList.clear();
        symbolPos = 0;
        String tempText[] = text.split( "\n" );
        for (int line = 0; line < tempText.length; line++) {
            if (tempText[line] == "") continue;
            else {
                char[] lineText = tempText[line].toCharArray();
                for (int pos = 0; pos < lineText.length; pos++) {
                    String token = "";
                    char ch = lineText[pos];

                    //读入关键字或标识符字符
                    if (IsAlpha( ch )) {
                        do {
                            token += ch;
                            pos++;
                            if (pos == lineText.length) break;
                            ch = lineText[pos];
                        } while (IsAlpha( ch ) || IsNumble( ch ));
                        //识别关键字
                        if (IskeyWord( token )) {
                            System.out.println( "Keyword:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                            tokenList.add( new Token( token, Keyword, line + 1, pos - token.length() + 1 ) );
                        }
                        //识别标识符
                        else {
                            if (!(symbolTable.isEmpty() || symbolTable.containsKey( token ))) {
                                symbolTable.put( token, symbolPos );
                                symbolPos++;
                            }
                            System.out.println( "Identifier:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                            tokenList.add( new Token( token, Identifier, line + 1, pos - token.length() + 1 ) );
                        }
                        pos--;
                    }
                    //识别注释
                    else if (ch == '/' && pos + 1 < lineText.length && lineText[pos + 1] == '/') {
                        while (pos < lineText.length) token += lineText[pos++];
                        System.out.println( "Comment:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                        tokenList.add( new Token( token, Comment, line + 1, pos - token.length() + 1 ) );
                    }
                    //识别数字
                    else if (IsNumble( ch )) {
                        int state = 0;
                        int goToState;
                        boolean hasError = false;
                        boolean isFloat = false;
                        while (ch == '.' || ch == 'e' || ch == '-' || ch == '+' || IsNumble( ch )) {
                            if (ch == '.' || ch == 'e') isFloat = true;
                            char checkList[] = digitalDFA[state].toCharArray();
                            for (goToState = 0; goToState < digitalDFA.length; goToState++) {
                                if (InNumDFA( ch, checkList[goToState] )) {
                                    token += ch;
                                    state = goToState;
                                    break;
                                }
                            }
                            if (goToState == digitalDFA.length) break;
                            pos++;
                            if (pos == lineText.length) break;
                            ch = lineText[pos];
                        }
                        //错误发现(非终结状态退出或当前字符不是运算符和边界符的符号)
                        if (state == 2 || state == 4 || state == 5 || state == 6) hasError = true;
                        if (!IsNumble( ch ) && (!IsOpOrDeli( ch ) || ch == '.')&&ch!=' ') hasError = true;
                        //错误处理
                        if (hasError) {
                            //处理到可以截断的字符
                            while (ch != ',' && ch != '}' && ch != ' ' && ch != '\0' && ch != ';') {
                                token += ch;
                                pos++;
                                if (pos == lineText.length) break;
                                ch = lineText[pos];
                            }
                            System.out.println( "Check the Input Number:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                        }
                        //判断整数浮点数
                        else {
                            if (isFloat) {
                                System.out.println( "Float:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                                tokenList.add( new Token( token, Constant, line + 1, pos - token.length() + 1 ) );
                            } else {
                                System.out.println( "Int:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                                tokenList.add( new Token( token, Constant, line + 1, pos - token.length() + 1 ) );
                            }
                        }
                        pos--;//注意要回退
                        token = "";
                    }
                    //识别运算符和界符
                    else if (IsOpOrDeli( ch )) {
                        token += ch;
                        pos++;
                        if (pos == lineText.length) {
                            if (IsDlimiter( ch )) {
                                System.out.println( "Dlimiter:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                                tokenList.add( new Token( token, Keyword, line + 1, pos - token.length() + 1 ) );
                                break;
                            } else {
                                System.out.println( "此处不应该有运算符" + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                                break;
                            }
                        }
                        //双字符运算符
                        if (lineText[pos] == '=' && (ch == '>' || ch == '<' || ch == '=' || ch == '!')) {
                            token += '=';
                            pos++;
                            System.out.println( "Operator:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                            tokenList.add( new Token( token, Keyword, line + 1, pos - token.length() + 1 ) );
                        }
                        //界符
                        else if (IsDlimiter( ch )) {
                            System.out.println( "Dlimiter:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                            tokenList.add( new Token( token, Keyword, line + 1, pos - token.length() + 1 ) );
                        }
                        //单字符运算符
                        else {
                            System.out.println( "Operator:" + token + " at line:" + (line + 1) + " pos:" + (pos - token.length() + 1) );
                            tokenList.add( new Token( token, Keyword, line + 1, pos - token.length() + 1 ) );
                        }
                        pos--;
                    }
                }
            }
        }


    }

    private boolean IsAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    private boolean IsNumble(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean IsOpOrDeli(char ch) {
        for (int i = 0; i < opOrDeli.length; i++) if (ch == opOrDeli[i]) return true;
        return false;
    }

    private boolean IsDlimiter(char ch) {
        return ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ',' || ch == ';';
    }

    private boolean IskeyWord(String str) {
        for (int i = 0; i < keyWord.length; i++) if (str.equals( keyWord[i] )) return true;
        return false;
    }

    private boolean InNumDFA(char ch, char check) {
        if (check == 'd') {
            if (IsNumble( ch )) return true;
            else return false;
        } else {
            if (ch == check) return true;
            else return false;
        }


    }

    //getter and setter
   /* public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public HashMap<String, Integer> getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(HashMap<String, Integer> symbolTable) {
        this.symbolTable = symbolTable;
    }
*/
    public List<Token> getTokenList() {
        return tokenList;
    }
/*
    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public int getSymbolPos() {
        return symbolPos;
    }

    public void setSymbolPos(int symbolPos) {
        this.symbolPos = symbolPos;
    }

    public List<Point> getAnimatePos() {
        return animatePos;
    }

    public void setAnimatePos(List<Point> animatePos) {
        this.animatePos = animatePos;
    }*/
}
