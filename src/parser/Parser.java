package parser;

import latex.Latex;
import semantic.ParserTree;
import token.Token;
import utils.Config;
import utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static token.TokenType.Keyword;

public class Parser {
    //预测分析表
    private HashMap<String, String> predictMap;
    //输入字串
    private String inputSequence;
    //下推栈
    private Stack<String> stack;

    //错误信息和推导过程
    private List<Error> errorList = new ArrayList<>();
    private List<Derivation> derivationProcess = new ArrayList<>();


    //用于打印日志的------
    private static Handler fileHandler = null;
    private static final Logger LOGGER = Logger.getLogger( Test.class
            .getClass().getName() );

    //当前 token 的上一个
    private Token preToken;
    private ParserTree parserTree = new ParserTree();


    public Parser(String inputSequence) {
        this.inputSequence = inputSequence;
        this.stack = new Stack<>();
        genPredictMap();
    }

    public Parser() {
        //不传参，DEFAULT_CODE
        this.inputSequence = Config.DEFAULT_CODE;
        this.stack = new Stack<>();
        genPredictMap();
    }

    public void startParsing() {
        //是否为代码块开头节点
        boolean isRoot = true;
        //用于打印日志
        loggerInit();

        //获取 token list
        Latex latex = new Latex( inputSequence );
        latex.LetexAnalyze();
        List<Token> tokenList = latex.getTokenList();
//        printTokenList( tokenList );

        //# 初始符号压入栈
        stack.push( "program" );

        String right;
        String leftandinput;

        //当栈非空，输入缓冲区存在
        while (stack.size() > 0 && tokenList.size() > 0) {
            //第一个 Token 和 栈顶元素
            Token firstToken = tokenList.get( 0 );

            //如果是注释
            if (firstToken.getTokenDetailType().equals( "Comment" )) {
                preToken = tokenList.remove( 0 );
                continue;
            }
            //输入缓冲区与推导符号串第一个字符相等的话，删掉
            try {
                if (firstToken.getTokenDetailType().equals( stack.peek() )) {
                    LOGGER.info( String.format( "stack is:   %s,   token list is:  %s ", getStackString( stack ), getTokenListString( tokenList ) ) );
                    LOGGER.info( String.format( "栈顶值和token值相等，消！the value is %s \n", stack.peek() ) );

                    //代码块语法分析完成进行语法数计算和三地址代码生成
                    if (stack.peek().equals( ";" )) {
                        isRoot = true;
                    }

                    String usedProduction = "消去终结符: " + stack.peek();
                    //移除相同值
                    preToken = tokenList.remove( 0 );
                    stack.pop();
                    String stackAfterReplace = getStackStringBottomUp( stack );
                    this.derivationProcess.add( new Derivation( usedProduction, stackAfterReplace ));
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //匹配字符
            leftandinput = stack.peek() + "-" + firstToken.getTokenDetailType();
            LOGGER.info( String.format( "下推栈顶和token值不相等,开始匹配并替换非终结符！要查的表项为：[%s, %s] ", stack.peek(), firstToken.getTokenDetailType() ) );
            //在预测表中有结果
            if ((right = predictMap.get( leftandinput )) != null) {
                LOGGER.info( String.format( "表命中了！替换之前， stack is: %s,token list is: %s  ", getStackString( stack ), getTokenListString( tokenList ) ) );
                //命中后使用的产生式
                String usedProduction = stack.peek() + " -> " + right;
                LOGGER.info( "表命中了！使用产生式：    " + usedProduction );

                //构建语法树
                int pos = Util.getIndexByProductionString( stack.peek() + " -> " + right );
//                parserTree.BuildParserTree(tokenList.get(0),pos,isRoot);
                isRoot = false;
                stack.pop();
                //todo
                if (!right.equals( "none" )) {
                    //压入后序字符
                    String[] args = right.split( "\\s+" );
                    for (int i = args.length - 1; i > -1; i--) {
                        //反向压入堆栈
                        if (!args[i].equals( "none" )) {
                            stack.push( args[i] );
                        }
                    }
                }

                String stackAfterReplace = getStackStringBottomUp( stack );
                this.derivationProcess.add(new Derivation( usedProduction, stackAfterReplace ));
                LOGGER.info( String.format( "表命中了！替换之后， stack is: %s,token list is: %s  \n", getStackString( stack ), getTokenListString( tokenList ) ) );
            }
            //否则的话报错
            else {
                if (stack.peek().equals( "=" )) {
                    LOGGER.info( "Error, 不能单独输入 ID, 请删除 多余的ID 或 补全。    " + "\n" );
                    Error error = new Error( "单独输入 ID, 请删除 多余的ID 或 补全", preToken );
                    errorList.add( error );
                    while ((right = predictMap.get( leftandinput )) == null) {
                        stack.pop();
                        if (stack.size() > 0) {
                            leftandinput = stack.peek() + "-" + firstToken.getTokenDetailType();
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                List<String> absenceList = Arrays.asList( ";", "else", "then" );
                if (absenceList.contains( stack.peek() )) {
                    String absenceString = stack.peek();
                    LOGGER.info( String.format( "Error, 缺少 %s 符号。    " + "\n", absenceString ) );
                    Error error = new Error( String.format( "缺少 %s 符号。", absenceString ), firstToken );
                    errorList.add( error );
                    tokenList.add( 0, new Token( absenceString, Keyword, firstToken.getTokenLine(), firstToken.getTokenLine() ) );
                    continue;
                }

                //确保丢失的分号可以被发现
                if (stack.size() > 0) {
                    String tokenValue = firstToken.getTokenValue();
                    if (stack.peek().equals( "arithexprprime" ) && !(tokenValue.equals( "+" ) || tokenValue.equals( "+" ))) {
                        stack.pop();
                        continue;
                    } else if (stack.peek().equals( "multexprprime" ) && !(tokenValue.equals( "*" ) || tokenValue.equals( "/" ))) {
                        stack.pop();
                        continue;
                    }
                }

                LOGGER.info( "Error,多余 token：    " + leftandinput + "  ,token info: " + firstToken + "\n" );
                Error error = new Error( "多余 token " + firstToken.getTokenValue(), firstToken );
                errorList.add( error );
                //恐慌模式
                preToken = tokenList.remove( 0 );

            }
        }

        //分别对应 stack 和 tokenList 还有剩余的情况
        if (stack.size() > 0) {
            Error error = new Error( "输入代码缺少，请补全。", preToken.getTokenLine(), preToken.getTokenPos() );
            errorList.add( error );
            LOGGER.info( error.toString() );
        } else if (tokenList.size() > 0) {
            Error error = new Error( "输入代码多余，请删除。", preToken.getTokenLine(), preToken.getTokenPos() );
            errorList.add( error );
            LOGGER.info( error.toString() );
        }
    }

    public void genPredictMap() {
        //生成预测文件
        new AnalyzeGrammer().init();
        //todo
        String text_line;
        String left;
        String symbol;
        String right;
        try {
            // 初始化
            predictMap = new HashMap<>();
            File file = new File( Config.PREDICT_TABLE );
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            while ((text_line = reader.readLine()) != null) {
                left = text_line.split( "#" )[0];
                symbol = (text_line.split( "#" )[1]).split( "->" )[0].trim();
                right = (text_line.split( "#" )[1]).split( "->" )[1].trim();
                predictMap.put( left + "-" + symbol, right );
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }


    }

    public static void loggerInit() {

        try {
            fileHandler = new FileHandler( "src/log/logfile.log" );//file
            SimpleFormatter simple = new SimpleFormatter();
            fileHandler.setFormatter( simple );
            LOGGER.addHandler( fileHandler );//adding Handler for file

        } catch (IOException e) {
            e.printStackTrace();
            // TODO Auto-generated catch block
        }

    }

    //打印 token list
    public static void printTokenList(List<Token> tokenList) {
        for (Token token : tokenList) {
            System.out.println( token );
        }
    }

    //获取 token list
    public static String getTokenListString(List<Token> tokenList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Token token : tokenList) {
            stringBuilder.append( token.getTokenValue() ).append( " " );
        }
        return stringBuilder.toString();
    }

    public static String getStackString(Stack stack) {
        StringBuilder stringBuilder = new StringBuilder();
        List stackList = Arrays.asList( stack.toArray() );

        for (int i = 0; i < stackList.size(); i++) {
            stringBuilder.append( stackList.get( i ) ).append( " " );
        }

        return stringBuilder.toString();
    }

    public static String getStackStringBottomUp(Stack stack) {
        StringBuilder stringBuilder = new StringBuilder();
        List stackList = Arrays.asList( stack.toArray() );

        for (int i = stackList.size() - 1; i >= 0; i--) {
            stringBuilder.append( stackList.get( i ) ).append( " " );
        }

        return stringBuilder.toString();
    }

    public HashMap<String, String> getPredictMap() {
        return predictMap;
    }

    public void setPredictMap(HashMap<String, String> predictMap) {
        this.predictMap = predictMap;
    }


    public Stack<String> getStack() {
        return stack;
    }

    public void setStack(Stack<String> stack) {
        this.stack = stack;
    }

    public String getInputSequence() {
        return inputSequence;
    }

    public void setInputSequence(String inputSequence) {
        this.inputSequence = inputSequence;
    }

    public List<Error> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<Error> errorList) {
        this.errorList = errorList;
    }


    public List<Derivation> getDerivationProcess() {
        return derivationProcess;
    }

    public void setDerivationProcess(List<Derivation> derivationProcess) {
        this.derivationProcess = derivationProcess;
    }
}
