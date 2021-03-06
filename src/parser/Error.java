package parser;

import token.Token;

public class Error {
    private String info;//错误信息
    private int line;//行号
    private int row;
    private Token token;

    public Error() {

    }

    //public Error(int id,String info,int line){
//	this.id=id;
//	this.info=info;
//	this.line=line;
//}
    public Error(String info, Token token) {
        this.info = info;
        this.token = token;
        this.line = token.getTokenLine();
        this.row = token.getTokenPos();
    }

    public Error(String info, int line, int row) {
        this.info = info;
        this.line = line;
        this.row = row;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }


    @Override
    public String toString() {
        return "Error{" +
                "info='" + info + '\'' +
                ", line=" + line +
                ", row=" + row +
                "}";
    }
}
