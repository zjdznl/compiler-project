package parser;

import java.util.ArrayList;

public class Production {
    /**
     * 产生式的实体类
     * 比如 stmts -> stmt stmts
     * left 对应 stmts right 对应 stmt stmts
     */
    private String left;
    private String[] right;

    //select集
    ArrayList<String> select = new ArrayList<>();

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
}
