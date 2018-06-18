package parser;

public class Derivation {
    //用到的产生式 和 推导过程
    private String production;
    private String process;

    public Derivation() {
    }

    public Derivation(String production, String process) {
        this.production = production;
        this.process = process;
    }

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "Derivation{" +
                "production='" + production + '\'' +
                ", process='" + process + '\'' +
                '}';
    }
}
