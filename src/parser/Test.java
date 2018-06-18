package parser;

import utils.Config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Test {
    public static void main(String[] args) {
        testParser();
//        testStack();
    }

    public static void testAnalyzeGrammer() {
        AnalyzeGrammer analyzeGrammer = new AnalyzeGrammer();
        analyzeGrammer.init();
        analyzeGrammer.output();
//        System.out.println(analyzeGrammer.isCanBeNull( "none" ));
    }

    public static void testParser() {
        Parser parser = new Parser(Config.DEFAULT_CODE);
        parser.genPredictMap();

        for (Map.Entry<String, String> entry : parser.getPredictMap().entrySet()) {
            System.out.println( entry.getKey() + " : " + entry.getValue() );
        }

        parser.startParsing();

        for (Error error : parser.getErrorList()) {
            System.out.println(error);
        }
    }

    public static void testStack() {
        Stack<String> stack = new Stack<>();
        stack.push( "a" );
        stack.push( "b" );
        stack.push( "c" );

//        System.out.println( stack.peek() );
        System.out.println( Arrays.toString(stack.toArray()) );
        System.out.println( stack.toString().replaceAll("\\[", "").replaceAll("]", ""));
        printStack( stack );
        System.out.println(getStackString( stack ));
    }

    public static void printStack(Stack stack) {
        List list = Arrays.asList( stack.toArray() );

        for (int i = list.size() - 1; i >= 0; i--) {
            System.out.print(list.get( i ) + " ");
        }
    }

    public static String getStackString(Stack stack) {
        StringBuilder stringBuilder = new StringBuilder();
        List stackList = Arrays.asList( stack.toArray() );

        for (int i = stackList.size() - 1; i >= 0; i--) {
            stringBuilder.append( stackList.get( i ) ).append( " " );
        }

        return stringBuilder.toString();
    }


}
