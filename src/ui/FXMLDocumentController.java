package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import latex.Latex;
import parser.Error;
import parser.Parser;
import token.Token;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Title:  FXMLDocumentController
 * Description:
 *
 * @Author: Gwon NyeongJin
 * @Date: 2018/6/18 下午4:52
 * @Version: 1.0
 **/

public class FXMLDocumentController implements Initializable {

    private Label label;
    @FXML
    private Button baocun;
    @FXML
    private Button zhixing;
    @FXML
    private Button fenxixiayige;
    @FXML
    private Button fenxizhijieshu;
    @FXML
    private Button qingkong;
    @FXML
    private Button chongxinfenxi;
    @FXML
    private TitledPane yuanwenjian;
    @FXML
    private TextArea yuanwenjianneirong;
    @FXML
    private TitledPane cifa;
    @FXML
    private TableColumn<?, ?> cifahanghao;
    @FXML
    private TableColumn<?, ?> cifaliehao;
    @FXML
    private TableColumn<?, ?> token;
    @FXML
    private TableColumn<?, ?> leibie;
    @FXML
    private TitledPane yufa;
    @FXML
    private TableColumn<?, ?> shiyong;
    @FXML
    private TableColumn<?, ?> tuidao;
    @FXML
    private TableColumn<?, ?> daima;
    @FXML
    private TitledPane cuowu;
    @FXML
    private TableColumn<?, ?> cuowuhanghao;
    @FXML
    private TableColumn<?, ?> cuowuliehao;
    @FXML
    private TableColumn<?, ?> cuowuxinxi;
    @FXML
    private TitledPane fuhao;
    @FXML
    private TableColumn<?, ?> pos;
    @FXML
    private TableColumn<?, ?> fuhaobiaobiao;
    @FXML
    private TableView<?> fuhaobiao;
    @FXML
    private Button jiazai;
    @FXML
    private Button zhizuo;
    @FXML
    private Button chengyuan;
    @FXML
    private TableView<?> cifabiao;
    @FXML
    private TableView<?> yufabiao;
    @FXML
    private TableView<?> sandizhibiao;
    @FXML
    private TableView<?> cuowubiao;

    private void handleButtonAction(ActionEvent event) {
        //
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {


    }

    @FXML
    private void jiazaiwenjian(ActionEvent event) {

        Stage mainStage = null;
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(mainStage);

        String fileInner = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(selectedFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                fileInner += tempString + "\n";
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        yuanwenjianneirong.setText(fileInner);

    }

    @FXML
    private void baocunyuanwenjian(ActionEvent event) {

        Stage mainStage = null;
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("编译原理_导出文件");
        File selectedFile = fileChooser.showSaveDialog(mainStage);
        if (selectedFile == null) {
            return;
        }
        if (selectedFile.exists()) {
            selectedFile.delete();
        }
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(selectedFile);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(yuanwenjianneirong.getText());
            bw.close();
        } catch (IOException ex) {
            ex.getStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        String exportFilePath = selectedFile.getAbsolutePath();
        System.out.println("导出文件的路径" + exportFilePath);

    }

    @FXML
    private void zhixingdangqianyuanwenjian(ActionEvent event) {
        final String str = yuanwenjianneirong.getText();
        yuanwenjianneirong.setStyle("-fx-highlight-fill: #d3ca47; -fx-highlight-text-fill: firebrick;");
        //动画线程
        Task<Void> progressTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                Latex analyze = new Latex(str);
                analyze.LetexAnalyze();
                analyze.GetAnimatePos();
                Parser parser = new Parser(str);
                parser.startParsing();

                //语法
                //List<Error> errorList = parser.getErrorList();
                //List<String[]> derivationProcess = parser.getDerivationProcess();

                ((TableColumn<Token, String>) cifahanghao).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenLine"));
                ((TableColumn<Token, String>) cifaliehao).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenPos"));
                ((TableColumn<Token, String>) token).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenValue"));
                ((TableColumn<Token, String>) leibie).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenType"));

                ((TableColumn<Error, String>) cuowuhanghao).setCellValueFactory(new PropertyValueFactory<Error, String>("line"));
                ((TableColumn<Error, String>) cuowuliehao).setCellValueFactory(new PropertyValueFactory<Error, String>("row"));
                ((TableColumn<Error, String>) cuowuxinxi).setCellValueFactory(new PropertyValueFactory<Error, String>("info"));

                TableView<Token> call_cifaibiao = (TableView<Token>) cifabiao;
                TableView<Error> call_cuowubiao = (TableView<Error>) cuowubiao;

                for (int i = 0; i <= analyze.animatePos.size(); i++) {
                    int tempX = analyze.animatePos.get(i).x;
                    int tempY = analyze.animatePos.get(i).y;
                    yuanwenjianneirong.selectRange(tempX - 1, tempY);

                    //derivationProcess.get(i)[0];
                    //derivationProcess.get(i)[1];
                    call_cifaibiao.setItems(FXCollections.observableArrayList(analyze.tokenList.subList(0,i)));
                    call_cuowubiao.setItems(FXCollections.observableArrayList(parser.getErrorList().subList(0,i)));

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                updateMessage("Finish");
                return null;
            }
        };
        new Thread(progressTask).start();

    }

    @FXML
    private void fenxixiayigefun(ActionEvent event) {
    }

    @FXML
    private void fenxizhijieshufun(ActionEvent event) {
    }

    @FXML
    private void qingkongfenxijieguo(ActionEvent event) {
    }

    @FXML
    private void chongxinfenxifun(ActionEvent event) {
    }

    @FXML
    private void zhizuozhemingdan(ActionEvent event) {
    }

    @FXML
    private void chengyuanfengong(ActionEvent event) {
    }

}
