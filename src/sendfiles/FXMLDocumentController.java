/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sendfiles;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

/**
 *
 * @author fiordor
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private VBox vboxMain;
    @FXML
    private HBox hboxMenu;
    @FXML
    private Button btCodificar;
    @FXML
    private Button btDecodificar;
    @FXML
    private Button btEnviar;
    @FXML
    private Button btRecibir;
    @FXML
    private HBox hboxInput;
    @FXML
    private Button btInput;
    @FXML
    private Label lbInput;
    @FXML
    private HBox hboxOutput;
    @FXML
    private Button btOutput;
    @FXML
    private Label lbOutput;
    @FXML
    private HBox hboxIp;
    @FXML
    private TextField tfIp0;
    @FXML
    private TextField tfIp1;
    @FXML
    private TextField tfIp2;
    @FXML
    private TextField tfIp3;
    @FXML
    private HBox hboxPort;
    @FXML
    private TextField tfPort;
    @FXML
    private HBox hboxLanzar;
    @FXML
    private Button btLanzar;
    @FXML
    private HBox hboxEstado;
    @FXML
    private Label lbEstado;
    
    private VBox[] vboxEstados;
    private Label[] lbEstados;
    private ProgressBar[] pbEstados;
    
    private File[] inputDir;
    private File outputDir;
    
    private int cores;
    
    private Thread[] tManagement;
    private FileManagement[] fManagement;
    
    private AtomicInteger pointer;
    //private ListFileSyn synFiles;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        vboxEstados = null; lbEstados = null; pbEstados = null;
        
        inputDir = null; outputDir = null;
        
        tfIp0.setText(""); tfIp1.setText(""); tfIp2.setText(""); tfIp3.setText("");
        
        tfPort.setText("");
        
        cores = Runtime.getRuntime().availableProcessors();
        
        Thread[] tManagment = null;
        FileManagement[] fManagment = null;
        
        pointer = null;
        //synFiles = null;
        
        int[] n = {4,5,6,7,8,9,10};
        reset(n);
        
    }
    
    private void reset(int[] n) {
        
        if (vboxEstados != null) {
            for (int i = 0; i < vboxEstados.length; i++) {
                btLanzar.disableProperty().unbind();
                lbEstados[i].textProperty().unbind();
                pbEstados[i].progressProperty().unbind();
                vboxMain.disableProperty().unbind();
                vboxEstados[i].getChildren().remove(lbEstados[i]);
                vboxEstados[i].getChildren().remove(pbEstados[i]);
                vboxMain.getChildren().remove(vboxEstados[i]);
            }
        }
        
        vboxEstados = null; lbEstados = null; pbEstados = null;
        
        inputDir = null; outputDir = null;
        
        lbInput.setText("Seleccionar carpeta contenedora");
        lbOutput.setText("carpeta de salida");
        
        tfIp0.setText(""); tfIp1.setText(""); tfIp2.setText(""); tfIp3.setText("");
        
        tfPort.setText("");        
        
        lbEstado.setText("Estado");
        
        cores = Runtime.getRuntime().availableProcessors();

        Thread[] tManagment = null;
        FileManagement[] fManagment = null;
        
        pointer = null;
        //synFiles = null;
        
        btCodificar.setDisable(false);
        btDecodificar.setDisable(false);
        btEnviar.setDisable(false);
        btRecibir.setDisable(false);
        
        hboxInput.setDisable(false);
        hboxOutput.setDisable(false);
        hboxIp.setDisable(false);
        hboxPort.setDisable(false);
        hboxLanzar.setDisable(false);
        hboxEstado.setDisable(false);
        
        for (int i : n) {
            
            switch (i) {
                case 0 : btCodificar.setDisable(true);   break;
                case 1 : btDecodificar.setDisable(true); break;
                case 2 : btEnviar.setDisable(true);      break;
                case 3 : btRecibir.setDisable(true);     break;
                case 4 : hboxInput.setDisable(true);     break;
                case 5 : hboxOutput.setDisable(true);    break;
                case 6 : hboxIp.setDisable(true);        break;
                case 7 : hboxPort.setDisable(true);      break;
                case 8 : hboxLanzar.setDisable(true);    break;
                case 9 : hboxEstado.setDisable(true);    break;
            }
        }
    }
    
    private void chooserFileManagement(boolean input) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Test");
        
        File dir = dirChooser.showDialog(null);
        if (dir != null) {
            
            if (input) {
                if (btDecodificar.isDisable()) {
                    
                    inputDir = dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return !pathname.isDirectory() && pathname.getName().endsWith(".b64");
                        }
                    });
                    lbInput.setText(inputDir.length + " archicos seleccionados");                    
                    
                } else {
                    
                    inputDir = dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) { return !pathname.isDirectory(); }
                    });
                    lbInput.setText(inputDir.length + " archicos seleccionados");
                    
                }
                
                //synFiles = new ListFileSyn(inputDir);
                
            } else {
                outputDir = dir;
                lbOutput.setText(File.separator + dir.getName());
            }
        } else {
            lbEstado.setText("Error al seleccionar la carpeta");
        }
    }
    
    private void testAndRun() {
        
        lbEstado.setText("");
        
        if (btCodificar.isDisable() || btDecodificar.isDisable()) {
            
            if (inputDir != null && outputDir != null) {
                
                if (btCodificar.isDisable()) { runThreads(FileManagement.ENCODE_BASE64); }
                if (btDecodificar.isDisable()) { runThreads(FileManagement.DECODE_BASE64); }
                
            } else {
                
                if (inputDir == null) { btInput.requestFocus(); lbEstado.setText("Selecciona carpeta contenedora de los archivos"); }
                if (outputDir == null) { btOutput.requestFocus(); lbEstado.setText("Selecciona carpeta de salida"); }
                
            }
            
        } else if (btEnviar.isDisable()) {
            
            boolean pass = true;
            
            if (inputDir == null) {
                
                btInput.requestFocus();
                lbEstado.setText("Selecciona carpeta contenedora de los archivos");
                pass = false;
            }
            
            TextField[] tf = new TextField[4];
            tf[0] = tfIp0; tf[1] = tfIp1; tf[2] = tfIp2; tf[3] = tfIp3;
            
            for (TextField tfAux : tf) {
                
                String ip = tfAux.getText().trim();

                try {
                    
                    int n = Integer.parseInt(ip);
                    if (n > 255 || n < 0) throw new NumberFormatException();
                    
                } catch (NumberFormatException e) {
                    
                    tfAux.requestFocus();
                    lbEstado.setText("Error en el formato de la IP");
                    pass = false;
                    
                }
            }
            
            try {
                
                int n = Integer.parseInt(tfPort.getText().trim());
                
                if (n < 0) throw new NumberFormatException();
                if (n < 1024) { tfPort.requestFocus(); lbEstado.setText("Puerto reservado para servicios"); pass = false; }
                if (n > 65534) { tfPort.requestFocus(); lbEstado.setText("Puerto fuera de rango"); pass = false; }
                
            } catch (NumberFormatException e) {
                
                tfPort.requestFocus();
                lbEstado.setText("Error en el formato del Port");
                pass = false;
            }
            
            if (pass) { runThreads(FileManagement.CLIENT_MODE); }
            
        } else if (btRecibir.isDisable()) {
            
            boolean pass = true;
            
            if (outputDir == null) {
                
                btOutput.requestFocus();
                lbEstado.setText("Selecciona carpeta de salida");
                pass = false;
            }
            
            if (!tfPort.getText().equals("")) {
                
                try {
                    
                    int n = Integer.parseInt(tfPort.getText().trim());
                    
                    if (n < 0) throw new NumberFormatException();
                    if (n < 1024) { tfPort.requestFocus(); lbEstado.setText("Puerto reservado para servicios"); pass = false; }
                    if (n > 65534) { tfPort.requestFocus(); lbEstado.setText("Puerto fuera de rango"); pass = false; }
                    
                    try { new ServerSocket(n).close(); }
                    catch (IOException e) {
                        
                        tfPort.requestFocus();
                        lbEstado.setText("Este puerto ya esta siendo usado");
                        pass = false;                        
                    }
                } catch (NumberFormatException e) {
                    
                    tfPort.requestFocus();
                    lbEstado.setText("Error en el formato del Port");
                    pass = false;
                }
                
                if (pass) { runThreads(FileManagement.SERVER_MODE); }
            }            
        }
    }
    
    private void runThreads(int option) {
        
        btLanzar.setDisable(true);
        
        switch (option) {
            case FileManagement.ENCODE_BASE64 :
                
                prepareThreads(FileManagement.ENCODE_BASE64);
                lbEstado.setText("Codificando en base64 ...");
                
                for (int i = 0; i < tManagement.length; i++) { tManagement[i].start(); }
                
                break;
                
            case FileManagement.DECODE_BASE64 :
                
                prepareThreads(FileManagement.DECODE_BASE64);
                lbEstado.setText("Decodificando base64 ...");
                
                for (int i = 0; i < tManagement.length; i++) { tManagement[i].start(); }
                
                break;
                
            case FileManagement.CLIENT_MODE : 
                
                prepareThreads(FileManagement.CLIENT_MODE);
                lbEstado.setText("Enviado archivos ...");
                
                for (int i = 0; i < tManagement.length; i++) { tManagement[i].start(); }    
                
                break;
                
            case FileManagement.SERVER_MODE : 
                
                prepareThreads(FileManagement.SERVER_MODE);
                lbEstado.setText("Esperando archivos ...");
                
                ServerSocket server = null;
                
                try {
                    server = new ServerSocket(Integer.parseInt(tfPort.getText()));
                } catch (IOException e) {
                    lbEstado.setText(e.getMessage());
                }
                
                int connected = 0;
                while (connected < cores) {

                    try {
                        
                        Socket client = server.accept();
                        
                        if (connected < cores) {
                            fManagement[connected] = new FileManagement(outputDir, client, option);
                            bindThread(connected);
                            tManagement[connected].start();
                        } else {
                            client.close();
                        }
                    } catch (IOException e) {
                        lbEstado.setText(e.getMessage());
                    }
                    connected++;
                }
                break;
        }
    }
    
    private boolean prepareThreads(int option) {
        
        tManagement = new Thread[cores];
        fManagement = new FileManagement[cores];
        
        vboxEstados = new VBox[cores];
        lbEstados = new Label[cores];
        pbEstados = new ProgressBar[cores];
        
        for (int i = 0; i < pbEstados.length; i++) {
            
            vboxEstados[i] = new VBox();
            
            lbEstados[i] = new Label("Core " + i);
            
            pbEstados[i] = new ProgressBar();
            pbEstados[i].setMaxWidth(Double.MAX_VALUE);
            pbEstados[i].setProgress(0);
            
            
            vboxEstados[i].getChildren().add(lbEstados[i]);
            vboxEstados[i].getChildren().add(pbEstados[i]);
            
            vboxMain.getChildren().add(vboxEstados[i]);
        }
        
        if (option != FileManagement.SERVER_MODE) {
            
            pointer = new AtomicInteger();

            for (int i = 0; i < cores; i++) {
                if (option == FileManagement.CLIENT_MODE) {
                    String ip = tfIp0.getText().trim() + "." +
                                tfIp1.getText().trim() + "." +
                                tfIp2.getText().trim() + "." +
                                tfIp3.getText().trim();
                    int port = Integer.parseInt(tfPort.getText());
                    
                    fManagement[i] = new FileManagement(inputDir, pointer, ip, port, option);
                } else {
                    fManagement[i] = new FileManagement(inputDir, outputDir, pointer, option);
                }
                bindThread(i);
            }            
        }
        return true;
    }
    
    private void bindThread(int i) {
        
        tManagement[i] = new Thread(fManagement[i]);
        
        lbEstados[i].textProperty().bind(fManagement[i].messageProperty());
        pbEstados[i].progressProperty().bind(fManagement[i].progressProperty());
        hboxMenu.disableProperty().bind(Bindings.notEqual(pbEstados[i].progressProperty(), new SimpleDoubleProperty(1)));
        tManagement[i].setDaemon(true);        
    }

    @FXML
    private void btCodificarOnAction(ActionEvent event) { int[] n = {0, 6, 7}; reset(n); }

    @FXML
    private void btDecodificarOnAtion(ActionEvent event) { int[] n = {1, 6, 7}; reset(n); }

    @FXML
    private void btEnviarOnAction(ActionEvent event) { int[] n = {2, 5}; reset(n); }

    @FXML
    private void btRecibirOnAction(ActionEvent event) { int[] n = {3, 4, 6}; reset(n); }

    @FXML
    private void btInputOnAction(ActionEvent event) { chooserFileManagement(true); }

    @FXML
    private void btOutputOnAction(ActionEvent event) { chooserFileManagement(false); }

    @FXML
    private void btLanzarOnAction(ActionEvent event) { testAndRun(); }
    
}
