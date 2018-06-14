package parser;

import java.util.ArrayList;

public class Production {
    /**
     * ����ʽ��ʵ����
     * ���� stmts -> stmt stmts
     * left ��Ӧ stmts right ��Ӧ stmt stmts
     */
    private String left;
    private String[] right;

    //select��
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
