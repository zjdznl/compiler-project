package parser;

import utils.Config;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class AnalyzeGrammer {
    //产生式集,终结符集,非终结符集,FIRST集,FOLLOW集
    private ArrayList<Production> productions;
    private ArrayList<String> terminals;
    private ArrayList<String> nonterminals;
    private HashMap<String, ArrayList<String>> firsts;
    private HashMap<String, ArrayList<String>> follows;


    //构造方法
    public AnalyzeGrammer() {
        productions = new ArrayList<>();
        terminals = new ArrayList<>();
        nonterminals = new ArrayList<>();
        firsts = new HashMap<>();
        follows = new HashMap<>();

        //初始化上面的变量
//        init();
    }

    public void init() {
        //主要逻辑，生成预测分析表
        //从文件中读取产生式
        setProductions();
        setNonTerminals();
        setTerminals();
        getFirst();
        getFollow();
        getSelect();

        createPredict();
    }

    public void output() {
        outputProductions();
        System.out.println();

        outputNonterminals();
        System.out.println();

        outputTerminals();
        System.out.println();

        outputFirst();
        System.out.println();

        outputFollow();
        System.out.println();

        outputSelect();
        System.out.println();

//        outPutProductionsTofile();
    }


    //从文件中读取产生式
    private void setProductions() {
        try {
            File file = new File( Config.GRAMMAR_FILE );
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line;
            String left;
            List<String> rights;
            Production production;
            while ((line = reader.readLine()) != null) {
                left = line.split( "->" )[0].trim();
                rights = Arrays.asList( line.split( "->" )[1].trim().split( "\\|" ) );

                for (String right : rights) {
                    production = new Production( left, right.trim().split( "\\s+" ) );
                    productions.add( production );
                }
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    //获得非终结符集
    private void setNonTerminals() {
        try {
            File file = new File( Config.GRAMMAR_FILE );
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line;
            String left;
            while ((line = reader.readLine()) != null) {
                left = line.split( "->" )[0].trim();
                if (!nonterminals.contains( left ))
                    nonterminals.add( left );
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获得终结符集,依赖于获得产生式函数
    //todo $ 和 none 等价么？
    public void setTerminals() {
        //遍历所有的产生式
        String[] rights;
        for (Production production : productions) {
            rights = production.returnRights();
            //从右侧寻找终结符
            for (String right : rights) {
                if (!(nonterminals.contains( right ) || terminals.contains( right ) || right.equals( "none" )))
                    terminals.add( right );
            }
        }
    }

    //使用已经消除左递归的文法，指导书上的文法实在不好弄。
    //获取First集
    private void getFirst() {
        //终结符全部求出first集
        ArrayList<String> first;
        //终结符的 first 集合包括自己
        for (String terminal : terminals) {
            first = new ArrayList<>();
            first.add( terminal );
            firsts.put( terminal, first );
        }

        //给所有非终结符注册一下
        for (String nonterminal : nonterminals) {
            first = new ArrayList<>();
            firsts.put( nonterminal, first );
        }


        boolean flag;
        do {
            flag = true;
            String left;
            String right;
            String[] rights;
            for (Production production : productions) {
                //遍历每一个产生式
                left = production.returnLeft();
                rights = production.returnRights();
                //是否在rights中添加none
                boolean isAddNone = true;
                for (String right1 : rights) {
                    //遍历每个产生式右部的每个元素
                    right = right1;
                    //right是否存在，遇到空怎么办
                    //如果right不为空
                    if (!right.equals( "none" )) {
                        for (int l = 0; l < firsts.get( right ).size(); l++) {
                            if (!firsts.get( left ).contains( firsts.get( right ).get( l ) )) {
                                firsts.get( left ).add( firsts.get( right ).get( l ) );
                                flag = false;
                            }
                        }
                    }

                    Set<String> set = new HashSet<>();

                    //如果右部可以为空
                    if (!isCanBeNull( right )) {
                        isAddNone = false;
                        break;
                    }
                }
                if (isAddNone) {
                    if (!firsts.get( left ).contains( "none" ))
                        firsts.get( left ).add( "none" );
                }
            }
        } while (!flag);
        //非终结符的first集
    }

    //判断是否产生none
    public boolean isCanBeNull(String symbol) {
        if (symbol.equals( "none" )) {
            return true;
        }

        String[] rights;
        for (Production production : productions) {
            //找到产生式
            if (production.returnLeft().equals( symbol )) {
                rights = production.returnRights();
                for (String right : rights) {
                    if (right.equals( "none" )) {
                        return true;
                    }
                }
//                if (rights[0].equals( "none" )) {
//                    return true;
//                }
            }
        }
        return false;
    }

    //获得Follow集
    public void getFollow() {
        //所有非终结符的follow集初始化一下
        ArrayList<String> follow;
        for (String nonterminal : nonterminals) {
            follow = new ArrayList<String>();
            follows.put( nonterminal, follow );
        }
        //将#加入到follow(program)中, program 是文法的起始符号
        follows.get( "program" ).add( "#" );

        boolean flag;
        boolean fab;
        do {
            flag = true;
            //循环，遍历所有产生式
            for (Production production : productions) {
                String left;
                String right;
                String[] rights;
                rights = production.returnRights();
                //遍历产生式右部
                for (int j = 0; j < rights.length; j++) {
                    right = rights[j];

                    //非终结符
                    if (nonterminals.contains( right )) {
                        fab = true;
                        for (int k = j + 1; k < rights.length; k++) {
                            //查找first集
                            for (int v = 0; v < firsts.get( rights[k] ).size(); v++) {
                                //将后一个元素的first集加入到前一个元素的follow集中
                                if (!follows.get( right ).contains( firsts.get( rights[k] ).get( v ) )) {
                                    follows.get( right ).add( firsts.get( rights[k] ).get( v ) );
                                    flag = false;
                                }
                            }
                            if (!isCanBeNull( rights[k] )) {
                                fab = false;
                                break;
                            }
                        }
                        //如果存在一个产生式A→αB，或存在产生式A→αBβ且FIRST(β) 包含ε，
                        //那么 FOLLOW(A)中的所有符号都在FOLLOW(B)中
                        if (fab) {
                            left = production.returnLeft();
                            for (int p = 0; p < follows.get( left ).size(); p++) {
                                if (!follows.get( right ).contains( follows.get( left ).get( p ) )) {
                                    follows.get( right ).add( follows.get( left ).get( p ) );
                                    flag = false;
                                }
                            }
                        }
                    }
                }
            }
            //全部处理后跳出循环
        } while (!flag);
        //清除follow集中的#
////        String left;
////        for (String nonterminal : nonterminals) {
////            left = nonterminal;
////            for (int v = 0; v < follows.get( left ).size(); v++) {
////                if (follows.get( left ).get( v ).equals( "#" ))
////                    follows.get( left ).remove( v );
////            }
////        }

//        清除follow集中的none
        String left;
        for (String nonterminal : nonterminals) {
            left = nonterminal;
            for (int v = 0; v < follows.get( left ).size(); v++) {
                if (follows.get( left ).get( v ).equals( "none" ))
                    follows.get( left ).remove( v );
            }
        }
    }

    //获取Select集
    public void getSelect() {
        String left;
        String right;
        String[] rights;
        ArrayList<String> follow = new ArrayList<String>();
        ArrayList<String> first = new ArrayList<String>();

        for (Production production : productions) {
            left = production.returnLeft();
            rights = production.returnRights();
            if (rights[0].equals( "none" )) {
                // select(i) = follow(A)
                follow = follows.get( left );
                for (String aFollow : follow) {
                    if (!production.select.contains( aFollow )) {
                        production.select.add( aFollow );
                    }
                }
            }
            //如果文法G的第i个产生式为A→aβ，则定义
            //SELECT(i)={a}
            else {
                boolean flag = true;
                for (String right1 : rights) {
                    right = right1;
                    first = firsts.get( right );
                    for (String aFirst : first) {
                        if (!production.select.contains( aFirst )) {
                            production.select.add( aFirst );
                        }
                    }
                    if (!isCanBeNull( right )) {
                        flag = false;
                        break;
                    }
                }
                //First集中有空
                if (flag) {
                    follow = follows.get( left );
                    for (String aFollow : follow) {
                        if (!production.select.contains( aFollow )) {
                            production.select.add( aFollow );
                        }
                    }
                }
            }
        }
    }

    //生成产生式
    private void createPredict() {
        Production production;
        StringBuilder line;
        String[] rights;
        try {
            File file = new File( Config.PREDICT_TABLE );
            //if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter( file.getAbsoluteFile() );
            BufferedWriter bw = new BufferedWriter( fw );

            for (Production production1 : productions) {
                production = production1;
                for (int j = 0; j < production.select.size(); j++) {
                    line = new StringBuilder( production.returnLeft() + "#" + production.select.get( j ) + " ->" );
                    rights = production.returnRights();
                    for (String right : rights) {
                        line.append( " " ).append( right );
                    }
                    line.append( "\n" );
                    //写入文件
                    bw.write( line.toString() );
                }
            }
            bw.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    //遍历产生式Select
    public void outputSelect() {
        //todo 生成预测分析表
        for (Production production : productions) {
            System.out.print( production + "  " );
            System.out.println( production.select );
        }
    }

    //遍历firsts集
    public void outputFollow() {
        for (Entry<String, ArrayList<String>> entry : follows.entrySet()) {

            System.out.print( entry.getKey() );
            System.out.print( " -> " );
            System.out.print( entry.getValue() );
            System.out.println();
        }
    }

    //遍历firsts集
    public void outputFirst() {
        for (Entry<String, ArrayList<String>> entry : firsts.entrySet()) {
            System.out.print( entry.getKey() );
            System.out.print( " -> " );
            System.out.print( entry.getValue() );
            System.out.println();
        }
    }


    //遍历产生式Production
    public void outputProductions() {
        for (Production production : productions) {
            System.out.println( production );
        }
    }

    public void outPutProductionsTofile() {
        /*
          将产生式输出到文件，每个产生式对应一个序号
         */
        try {
            File file = new File( Config.PRODUCTIOS_FILE );
            //if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter( file.getAbsoluteFile() );
            BufferedWriter bw = new BufferedWriter( fw );

            int j = 0;
            for (Production production : productions) {

                //写入文件
                //j是产生式对应的序号
                bw.write( j + " : " + production + "\n" );
                j++;
            }
            bw.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    //遍历非终结符集
    public void outputNonterminals() {
        for (String nonterminal : nonterminals) {
            System.out.println( nonterminal );
        }
    }

    //遍历非终结符集
    public void outputTerminals() {
        for (String terminal : terminals) {
            System.out.println( terminal );
        }
    }

    public ArrayList<Production> getProductions() {
        return productions;
    }

    public void setProductions(ArrayList<Production> productions) {
        this.productions = productions;
    }

    public ArrayList<String> getTerminals() {
        return terminals;
    }

    public void setTerminals(ArrayList<String> terminals) {
        this.terminals = terminals;
    }

    public ArrayList<String> getNonterminals() {
        return nonterminals;
    }

    public void setNonterminals(ArrayList<String> nonterminals) {
        this.nonterminals = nonterminals;
    }

    public HashMap<String, ArrayList<String>> getFirsts() {
        return firsts;
    }

    public void setFirsts(HashMap<String, ArrayList<String>> firsts) {
        this.firsts = firsts;
    }

    public HashMap<String, ArrayList<String>> getFollows() {
        return follows;
    }

    public void setFollows(HashMap<String, ArrayList<String>> follows) {
        this.follows = follows;
    }

}
