package parser;

import java.util.ArrayList;

public class Production {
    //select��
    ArrayList<String> select = new ArrayList<>();
    /**
     * ����ʽ��ʵ����
     * ���� stmts -> stmt stmts
     * left ��Ӧ stmts right ��Ӧ stmt stmts
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
