package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import latex.Latex;
import parser.Derivation;
import parser.Error;
import parser.Parser;
import token.Token;

import java.io.*;
import java.net.URL;
import java.util.Collection;
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
    private Button qingkong;
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
    private TableView<?> fuhaobiao;
    @FXML
    private Button jiazai;
    @FXML
    private TableView<?> cifabiao;
    @FXML
    private TableView<?> yufabiao;
    @FXML
    private TableView<?> sandizhibiao;
    @FXML
    private TableView<?> cuowubiao;
    @FXML
    private TableColumn<?, ?> fuhaobiaobiao;
    @FXML
    private TitledPane yufashu;
    @FXML
    private AnchorPane yufashuneirong;

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

        String fileInner = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(selectedFile));
            String tempString = "";
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

                ((TableColumn<Token, String>) cifahanghao).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenLine"));
                ((TableColumn<Token, String>) cifaliehao).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenPos"));
                ((TableColumn<Token, String>) token).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenValue"));
                ((TableColumn<Token, String>) leibie).setCellValueFactory(new PropertyValueFactory<Token, String>("tokenType"));

                ((TableColumn<Error, String>) cuowuhanghao).setCellValueFactory(new PropertyValueFactory<Error, String>("line"));
                ((TableColumn<Error, String>) cuowuliehao).setCellValueFactory(new PropertyValueFactory<Error, String>("row"));
                ((TableColumn<Error, String>) cuowuxinxi).setCellValueFactory(new PropertyValueFactory<Error, String>("info"));

                ((TableColumn<Derivation, String>) shiyong).setCellValueFactory(new PropertyValueFactory<Derivation, String>("production"));
                ((TableColumn<Derivation, String>) tuidao).setCellValueFactory(new PropertyValueFactory<Derivation, String>("process"));

                TableView<Token> call_cifaibiao = (TableView<Token>) cifabiao;
                TableView<Error> call_cuowubiao = (TableView<Error>) cuowubiao;
                TableView<Derivation> call_yufabiao = (TableView<Derivation>) yufabiao;

                Parser parser = new Parser(str);
                parser.startParsing();

                for (int i = 0; i < analyze.animatePos.size(); i++) {
                    int tempX = analyze.animatePos.get(i).x;
                    int tempY = analyze.animatePos.get(i).y;
                    yuanwenjianneirong.selectRange(tempX - 1, tempY);

                    call_cifaibiao.setItems(FXCollections.observableArrayList(analyze.tokenList.subList(0, i)));
                    call_yufabiao.setItems(FXCollections.observableArrayList(parser.getDerivationProcess().subList(0, i)));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                call_cuowubiao.setItems(FXCollections.observableArrayList(parser.getErrorList()));
                updateMessage("Finish");
                return null;
            }
        };
        new Thread(progressTask).start();

    }

    @FXML
    private void qingkongfenxijieguo(ActionEvent event) {

        Canvas canvas = new Canvas(500, 620);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        huaJuXing(gc, 0, 0);
        huaJuXing(gc, 1, 0);
        huaJuXing(gc, 1, 1);
        lianXian(gc, 0, 0, 1, 0);
        huaJuXing(gc, 1, 1);
        lianXian(gc, 0, 0, 1, 1);
        huaJuXing(gc, 2, 0);
        lianXian(gc, 1, 0, 2, 0);

        yufashu.setContent(canvas);

        /*Latex analyze = new Latex();
        Parser parser = new Parser();

        TableView<Token> call_cifaibiao = (TableView<Token>) cifabiao;
        TableView<Error> call_cuowubiao = (TableView<Error>) cuowubiao;
        TableView<Derivation> call_yufabiao = (TableView<Derivation>) yufabiao;

        call_cifaibiao.setItems(FXCollections.observableArrayList(analyze.tokenList));
        call_yufabiao.setItems(FXCollections.observableArrayList(parser.getDerivationProcess()));
        call_cuowubiao.setItems(FXCollections.observableArrayList(parser.getErrorList()));
*/
    }

    private GraphicsContext huaJuXing(GraphicsContext gc, int row, int col) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        gc.strokeRect(col * 130, row * 80, 100, 50);
        return gc;
    }

    private GraphicsContext lianXian(GraphicsContext gc, int Frow, int Fcol, int Srow, int Scol) {
        gc.setFill(Color.GREEN);
        gc.setLineWidth(3);
        gc.strokeLine(Fcol * 130 + 50, Frow * 80 + 50, Scol * 130 + 50, Srow * 80);
        return gc;
    }

}
