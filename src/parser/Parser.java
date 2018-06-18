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
//    private Stack<String> stack;
    private Stack<String> stack;

    //分别对应
    private List<Error> errorList = new ArrayList<>();
    private List<String[]> derivationProcess = new ArrayList<>(  );


    //用于打印日志的------
    static Handler fileHandler = null;
    private static final Logger LOGGER = Logger.getLogger( Test.class
            .getClass().getName() );

    //当前 token 的上一个
    private Token preToken;
    private ParserTree parserTree =new ParserTree();

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
    /*
    usage:
    loggerInit();//calling to the file content
    LOGGER.info("------------------START--------------------");
     */
    /// end of logging -----

    public Parser(String inputSequence) {
        this.inputSequence = inputSequence;
        this.stack = new Stack<>();
        genPredictMap();
    }

    public Parser() {
        this.inputSequence = Config.DEFAULT_CODE;
        genPredictMap();
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

        for (int i = stackList.size() - 1; i >=0 ; i--) {
            stringBuilder.append( stackList.get( i ) ).append( " " );
        }

        return stringBuilder.toString();
    }


    public void startParsing() {
        //是否为代码块开头节点
        boolean isRoot=true;
        //用于打印日志
        loggerInit();

        //获取 token list
        Latex latex = new Latex( inputSequence );
        latex.LetexAnalyze();
        List<Token> tokenList = latex.getTokenList();
        printTokenList( tokenList );

        //# 初始符号压入栈
        stack.push( "program" );

        String right;
        String leftandinput;
        StringBuilder process = new StringBuilder();
        //当栈非空，输入缓冲区存在
        while (stack.size() > 0 && tokenList.size() > 0) {

            if (tokenList.get( 0 ).getTokenDetailType().equals( "Comment" )) {
                preToken = tokenList.remove( 0 );
                continue;
            }
            //输入缓冲区与推导符号串第一个字符相等的话，删掉
            try {
                if (tokenList.get( 0 ).getTokenDetailType().equals( stack.peek() )) {
                    LOGGER.info( String.format( "stack is:   %s,   token list is:  %s ", getStackString( stack ), getTokenListString( tokenList ) ) );
                    LOGGER.info( String.format( "栈顶值和token值相等，消！the value is %s \n", stack.peek() ) );

                    //代码块语法分析完成进行语法数计算和三地址代码生成
                    if(stack.peek().equals(";")){
                        isRoot=true;
                    }

                    String usedProduction = "消去终结符: " + stack.peek();
                    //移除相同值
                    preToken = tokenList.remove( 0 );
                    stack.pop();
                    String stackAfterReplace = getStackStringBottomUp( stack );
                    this.derivationProcess.add( new String[]{usedProduction, stackAfterReplace} );
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //匹配字符
            leftandinput = stack.peek() + "-" + tokenList.get( 0 ).getTokenDetailType();
            LOGGER.info( String.format( "下推栈顶和token值不相等,开始匹配并替换非终结符！要查的表项为：[%s, %s] ", stack.peek(), tokenList.get( 0 ).getTokenDetailType() ) );
            //在预测表中有结果
            if ((right = predictMap.get( leftandinput )) != null) {
                LOGGER.info( String.format( "表命中了！替换之前， stack is: %s,token list is: %s  ", getStackString( stack ), getTokenListString( tokenList ) ) );
                //命中后使用的产生式
                String usedProduction = stack.peek() + " -> " + right;
                LOGGER.info( "表命中了！使用产生式：    " + usedProduction );

                //构建语法树
                int pos=Util.getIndexByProductionString(stack.peek() + " -> " + right);
                parserTree.BuildParserTree(tokenList.get(0),pos,isRoot);
                isRoot=false;
                //输出产生式和推导过程
//                process = new StringBuilder();
//                for (int i = stack.size() - 1; i > -1; i--) {
//                    process.append( stack.get( i ) ).append( " " );
//                }
//                //输出
//                DefaultTableModel tableModel = (DefaultTableModel) jtable4.getModel();
//                tableModel.addRow( new Object[]{stack.get( stack.size() - 1 ) + " -> " + right, process.toString()} );
//                jtable4.invalidate();
//                //删掉产生的字符，压入堆栈
//                stack.remove( stack.size() - 1 );
                stack.pop();
                //todo
                if (right.equals( "none" )) {
                    //只弹不压
                }
                //压入后序字符
                else {
                    String[] args = right.split( "\\s+" );
                    for (int i = args.length - 1; i > -1; i--) {
                        //反向压入堆栈
                        if (!args[i].equals( "none" )) {
                            stack.push( args[i] );
                        }
                    }
                }

                String stackAfterReplace = getStackStringBottomUp( stack );
                this.derivationProcess.add( new String[]{usedProduction, stackAfterReplace} );
                LOGGER.info( String.format( "表命中了！替换之后， stack is: %s,token list is: %s  \n", getStackString( stack ), getTokenListString( tokenList ) ) );
            }
            //否则的话报错
            else {

//                tokenList.get( 0 ).getTokenDetailType().equals( stack.peek() );
//                (right = predictMap.get( leftandinput )) != null
                if (stack.peek().equals( "=" )) {
                    LOGGER.info( "Error, 不能单独输入 ID, 请删除 多余的ID 或 补全。    " + "\n" );
                    Error error = new Error( "单独输入 ID, 请删除 多余的ID 或 补全", preToken );
                    errorList.add( error );
                    //todo 可以这样做么
                    while ((right = predictMap.get( leftandinput )) == null) {
                        stack.pop();
                        if (stack.size() > 0) {
                            leftandinput = stack.peek() + "-" + tokenList.get( 0 ).getTokenDetailType();
                        } else {
                            break;
                        }
                    }
                    continue;
                }

                //todo 笨方法
                List<String> absenceList = Arrays.asList( ";", "else", "then" );


                //todo tokenList.get( 0 ) or preToken
                if (absenceList.contains( stack.peek() ) ) {
                    String absenceString = stack.peek();
                    LOGGER.info( String.format( "Error, 缺少 %s 符号。    " + "\n", absenceString));
                    Error error = new Error( String.format( "缺少 %s 符号。", absenceString), tokenList.get( 0 ) );
                    errorList.add( error );

                    //todo 这里应该向 tokenList增加一个 token
                    tokenList.add( 0, new Token( absenceString, Keyword, tokenList.get( 0 ).getTokenLine(), tokenList.get( 0 ).getTokenLine() ) );
//                    stack.pop();

//                    while ((right = predictMap.get( leftandinput )) == null) {
//                        stack.pop();
//                        leftandinput = stack.peek() + "-" + tokenList.get( 0 ).getTokenDetailType();
//                    }
                    continue;
                }

                //确保丢失的分号可以被发现
                if (stack.size() > 0) {
                    String tokenValue = tokenList.get( 0 ).getTokenValue();
                    if (stack.peek().equals( "arithexprprime" ) && !(tokenValue.equals( "+" ) || tokenValue.equals( "+" ))) {
                        stack.pop();
                        continue;
                    } else if (stack.peek().equals( "multexprprime" ) && !(tokenValue.equals( "*" ) || tokenValue.equals( "/" ))) {
                        stack.pop();
                        continue;
                    }
                }

//                if (stack.peek().equals( ";" )) {
//                    LOGGER.info( "Error, 缺少 ; 符号, 请删除语句 或 补全 ;。    " + "\n" );
//                    Error error = new Error( "缺少 ; 符号, 请删除语句 或 补全 ;。", preToken );
//                    errorList.add( error );
//
//                    //todo 这里应该可以pop ,因为 ; 已经是结束了
//                    stack.pop();
////                    while ((right = predictMap.get( leftandinput )) == null) {
////                        stack.pop();
////                        leftandinput = stack.peek() + "-" + tokenList.get( 0 ).getTokenDetailType();
////                    }
//                    continue;
//                }
//
//                //todo tokenList.get( 0 ) or preToken
//                if (stack.peek().equals( "else" )) {
//                    LOGGER.info( "Error, 缺少 else 符号。    " + "\n" );
//                    Error error = new Error( "Error, 缺少 else 符号。", tokenList.get( 0 ) );
//                    errorList.add( error );
//
//                    //todo 这里应该向 tokenList增加一个 else token
//                    tokenList.add( 0, new Token( "else", Keyword, tokenList.get( 0 ).getTokenLine(), tokenList.get( 0 ).getTokenLine() ) );
////                    stack.pop();
//
////                    while ((right = predictMap.get( leftandinput )) == null) {
////                        stack.pop();
////                        leftandinput = stack.peek() + "-" + tokenList.get( 0 ).getTokenDetailType();
////                    }
//                    continue;
//                }


//                LOGGER.info( "Error, 不存在该表项, 恐慌模式将删除该token：    " + leftandinput + "  ,token info: " + tokenList.get( 0 ) + "\n" );
//                Error error = new Error( "Error, 不存在该表项, 恐慌模式将删除该token", tokenList.get( 0 ) );
                LOGGER.info( "Error,多余 token：    " + leftandinput + "  ,token info: " + tokenList.get( 0 ) + "\n" );
                Error error = new Error( "多余 token", tokenList.get( 0 ) );
                errorList.add( error );
                //恐慌模式
                preToken = tokenList.remove( 0 );
                //todo 调试方便，直接break
//                break;


            }
        }
        //todo 分析某部分还有剩余情况的问题
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

    public List<String[]> getDerivationProcess() {
        return derivationProcess;
    }
}
