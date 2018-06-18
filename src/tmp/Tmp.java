package tmp;

import utils.Config;
import utils.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tmp {
    public static void main(String[] args) {
        //记录每一行

        System.out.println( Util.getIndexByProductionString( "  list1  ->   none  " ) );

        String[][] GrammarForSema = new String[][]{
                new String[]{"program", "compoundstmt"},
                new String[]{"stmt", "ifstmt"},
                new String[]{"stmt", "whilestmt"},
                new String[]{"stmt", "assgstmt"},
                new String[]{"stmt", "compoundstmt"},
                new String[]{"compoundstmt", "{", "stmts", "}"},
                new String[]{"stmts", "stmt", "stmts"},
                new String[]{"stmts", "ε"},
                new String[]{"ifstmt", "if", "(", "boolexpr", ")", "then", "stmt", "else", "stmt"},
                new String[]{"whilestmt", "while", "(", "boolexpr", ")", "stmt"},
                new String[]{"assgstmt", "ID", "=", "arithexpr", ";"},
                new String[]{"boolexpr", "arithexpr", "boolop", "arithexpr"},
                new String[]{"boolop", "<"},
                new String[]{"boolop", ">"},
                new String[]{"boolop", "<="},
                new String[]{"boolop", ">="},
                new String[]{"boolop", "=="},
                new String[]{"arithexpr", "multexpr", "arithexprprime"},
                new String[]{"arithexprprime", "+", "multexpr", "arithexprprime"},
                new String[]{"arithexprprime", "-", "multexpr", "arithexprprime"},
                new String[]{"arithexprprime", "ε"},
                new String[]{"multexpr", "simpleexpr", "multexprprime"},
                new String[]{"multexprprime", "*", "simpleexpr", "multexprprime"},
                new String[]{"multexprprime", "/", "simpleexpr", "multexprprime"},
                new String[]{"multexprprime", "ε"},
                new String[]{"simpleexpr", "ID"},
                new String[]{"simpleexpr", "NUM"},
                new String[]{"simpleexpr", "(", "arithexpr", ")"}
        };
    }


    public static void changeProductionFormat() {
        String line = "";
        try {
            File file = new File( Config.PRODUCTIOS_FILE );
            //if file doesn't exists, then create it

            FileReader fr = new FileReader( file );
            BufferedReader br = new BufferedReader( fr );

            FileWriter fw = new FileWriter( new File( Config.PRODUCTIOS_FILE_TMP ) );
            BufferedWriter bw = new BufferedWriter( fw );

            List<String> productions = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                productions.add( line.split( ":" )[1].trim() );
            }


            for (int i = 0; i < productions.size(); i++) {
                fw.write( i + " : " + productions.get( i ) + "\n" );
            }


//            for (Production production1 : productions) {
//                production = production1;
//                for (int j = 0; j < production.select.size(); j++) {
//                    line = new StringBuilder( production.returnLeft() + "#" + production.select.get( j ) + " ->" );
//                    rights = production.returnRights();
//                    for (String right : rights) {
//                        line.append( " " ).append( right );
//                    }
//                    line.append( "\n" );
//                    //写入文件
//                    bw.write( line.toString() );
//                }
//            }

            bw.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
