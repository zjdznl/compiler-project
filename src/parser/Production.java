package parser;

import java.util.ArrayList;

public class Production {
    //select集
    ArrayList<String> select = new ArrayList<>();
    /**
     * 产生式的实体类
     * 比如 stmts -> stmt stmts
     * left 对应 stmts right 对应 stmt stmts
     */
    private String left;
    private String[] right;

    public Production(String left, String[] right) {
        this.left = left;
        this.right = right;
    }

    public String[] returnRights() {
        return right;
    }

    public String returnLeft() {
        return left;
    }

    @Override
    public String toString() {
        return left + " -> " + String.join( " ", right );
    }
}
