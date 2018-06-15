package parser;

import latex.Latex;
import token.Token;
import utils.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Parser {
    //预测分析表
    private HashMap<String, String> predictMap;
    //输入字串
    private String tokenList;
    //下推栈
//    private Stack<String> stack;
    private Stack<String> stack;

    //用于打印日志的------
    static Handler fileHandler = null;
    private static final Logger LOGGER = Logger.getLogger( Test.class
            .getClass().getName() );

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

    public Parser(String tokenList) {
        this.tokenList = tokenList;
        this.stack = new Stack<>();
        genPredictMap();
    }

    public Parser() {
        this.tokenList = Config.DEFAULT_CODE;
        this.stack = new Stack<>();
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

        for (int i = 0; i < stackList.size() ; i++) {
            stringBuilder.append( stackList.get( i ) ).append( " " );
        }

        return stringBuilder.toString();
    }


    public void startParsing() {
        //用于打印日志
        loggerInit();

        //获取 token list
        Latex latex = new Latex( tokenList );
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
                tokenList.remove( 0 );
                continue;
            }
            //输入缓冲区与推导符号串第一个字符相等的话，删掉
            try {
                if (tokenList.get( 0 ).getTokenDetailType().equals( stack.peek() )) {
                    LOGGER.info( String.format( "stack is:   %s,   token list is:  %s ", getStackString( stack ), getTokenListString( tokenList ) ) );
                    LOGGER.info( String.format( "栈顶值和token值相等，消！the value is %s \n", stack.peek())  );

                    //移除相同值
                    tokenList.remove( 0 );
                    stack.pop();
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //匹配字符
            leftandinput = stack.peek() + "-" + tokenList.get( 0 ).getTokenDetailType();
            System.out.println( String.format( "下推栈顶和token值不相等,开始匹配并替换非终结符！要查的表项为：[%s, %s] ", stack.peek(),tokenList.get( 0 ).getTokenDetailType() ));
            //在预测表中有结果
            if ((right = predictMap.get( leftandinput )) != null) {

                System.out.println( "表命中了！使用产生式：    " + stack.peek() + " -> " + right );
                LOGGER.info( String.format( "表命中了！替换之前， stack is: %s,token list is: %s  ", getStackString( stack ), getTokenListString( tokenList ) ) );
                LOGGER.info( "表命中了！使用产生式：    " + stack.peek() + " -> " + right);

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
                if (right.equals( "$" )) {
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
                LOGGER.info( String.format( "表命中了！替换之后， stack is: %s,token list is: %s  \n", getStackString( stack ), getTokenListString( tokenList ) ) );
            }
            //否则的话报错
            else {
                System.out.println( "fuck!表没有命中,溜了溜了。" + tokenList.get( 0 ) );


                //重新书写process
//                process = new StringBuilder();
////                for (int i = stack.size() - 1; i > -1; i--) {
////                    process.append( stack.get( i ) ).append( " " );
////                }
////                //tbmodel_lex_result.addRow(new String[]{process, "ERROR!  无法识别的字符"+input_cache.get(0)+"产生式"+leftandinput});
////                DefaultTableModel tableModel = (DefaultTableModel) jtable2.getModel();
////                tableModel.addRow( new Object[]{"无法识别的字符:" + tokenList.get( 0 ), "产生式:" + leftandinput} );
////                jtable4.invalidate();
                tokenList.remove( 0 );
                //todo 调试方便，直接break
                break;
            }
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

    public HashMap<String, String> getPredictMap() {
        return predictMap;
    }

    public void setPredictMap(HashMap<String, String> predictMap) {
        this.predictMap = predictMap;
    }

    public String getInputSequence() {
        return tokenList;
    }

    public void setInputSequence(String tokenList) {
        this.tokenList = tokenList;
    }

    public Stack<String> getStack() {
        return stack;
    }

    public void setStack(Stack<String> stack) {
        this.stack = stack;
    }
}
