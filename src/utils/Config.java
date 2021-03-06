package utils;

public class Config {
    public static String GRAMMAR_FILE = "src/grammar.txt";
    public static String PREDICT_TABLE = "src/output/PredictTable.txt";
    public static String PRODUCTIOS_FILE = "src/output/productions.txt";
    public static String PRODUCTIOS_FILE_TMP = "src/output/productions_tmp.txt";


    public static String DEFAULT_CODE1 = "{ \n" +
            "a=b+1;\n"+
            "i=10; \n" +
            "j=100; \n" +
            "n=1; \n" +
            "sum=0; \n" +
            "mult=1; \n" +
            "while (i>0) {n=n+1;i=i-1;123} \n" +
            "if (j>=50) then sum=sum+j; else {mult=mult*(j+1);sum=sum+i;} \n" +
            "if (i<=10) then sum=sum-i; else mult=mult+i/2; \n" +
            "if (i==j) then sum=sum-j; else mult=mult-j/2; \n" +
            "if (n>1) then n=n-1; else n=n+1; \n" +
            "if (n<2) then n=n+2; else n=n-2; //注释了解一下\n" +
            "}";
    public static String DEFAULT_CODE ="{ \n" +
            "i=10; \n" +
            "j=100; \n" +
            "n=1; \n" +
            "sum=0; \n" +
            "mult=1; \n" +
            "}";

    public static String ERROR_CODE = "{ \n" +
            "i=10; \n" +
            "j=100; \n" +
            "n=1; \n" +
            "sum=0; \n" +
            "mult=1; \n" +
            "while (i>0) {n=n+1;i=i-1;;} \n" +
            "if (j>=50) then sum=sum+j; else {mult=mult*(j+1);sum=sum+i;} \n" +
            "if (i<=10) then sum=sum-i; else mult=mult+i/2;n \n" +
            "if (i==j) then sum=sum-j; else mult=mult-j/2;n=123 \n" +
            "if (n>1)  n=n-1;else n=n+1 \n" +
            "if (n<2) then  n=n+2;  n=n--2; \n" +
            "}";


}
