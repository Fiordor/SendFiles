/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sendfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;
import javafx.concurrent.Task;

/**
 *
 * @author fiordor
 */
public class FileManagement extends Task<Void> {
    
    public static final int ENCODE_BASE64 = 0;
    public static final int DECODE_BASE64 = 1;
    public static final int CLIENT_MODE = 2;
    public static final int SERVER_MODE = 3;
    
    public static final int MAX_FILE_SIZE = 20000000;//20MB
    
    private File[] lisftOfFiles;
    private File outputDir;
    private String ip;
    private int port;
    private Socket client;
    
    private int option;
    
    public FileManagement(File outputDir, File[] listOfFiles, int option) {
        this.outputDir = outputDir; this.lisftOfFiles = listOfFiles;
        ip = null; port = -1; client = null;
        this.option = option;
    }
    
    private void startEncodeBase64() {
        
        for (int i = 0; i < lisftOfFiles.length; i++) {
            
            try {
                
                updateProgress(i, lisftOfFiles.length);
                updateMessage(String.format("%d / %d > %s", i + 1, lisftOfFiles.length, lisftOfFiles[i].getName()));
                
                encodeBase64(lisftOfFiles[i]);
            }
            catch (IOException e) {
                System.err.println(e);
                updateMessage(String.format("Error en el archivo %s", lisftOfFiles[i].getName()));
            }
        }
        updateProgress(1, 1);
        updateMessage("Completado");
    }

    private void encodeBase64(File f) throws IOException, FileNotFoundException {

        String fName = f.getName().substring(0, f.getName().lastIndexOf('.'));
        
        PrintWriter pwWriter = new PrintWriter(
                new File(outputDir.getAbsolutePath() + File.separator + fName + ".b64"));
        
        pwWriter.println(f.getName());
        
        FileInputStream fisReader = new FileInputStream(f);
        
        byte[] fileBytes = new byte[(int) f.length()];
        
        int readBytes = 0;
        if (fileBytes.length > MAX_FILE_SIZE) {
            while ((readBytes + MAX_FILE_SIZE) < fileBytes.length) {
                
                String encodedfile = writeEncodeBase64(fileBytes, readBytes, MAX_FILE_SIZE, fisReader);
                pwWriter.println(encodedfile);
                
                readBytes += MAX_FILE_SIZE;
                updateMessage(String.format("%s > %.1f MB / %.1f MB", f.getName(), readBytes / 1000000.0, fileBytes.length / 1000000.0));
            }
        }
                
        String encodedfile = writeEncodeBase64(fileBytes, readBytes, fileBytes.length - readBytes, fisReader);
        pwWriter.print(encodedfile);        
        
        pwWriter.close();
        fisReader.close();
    }
    
    private String writeEncodeBase64
        (byte[] fileBytes, int readBytes, int buff, FileInputStream fisReader)
        throws IOException, FileNotFoundException {
            
        byte[] auxFileBytes = new byte[buff];
        System.arraycopy(fileBytes, readBytes, auxFileBytes, 0, buff);

        fisReader.read(auxFileBytes);

        return Base64.getEncoder().encodeToString(auxFileBytes);
    }
    
    private void encodeBase64_v1(File f) throws IOException, FileNotFoundException {

        FileInputStream fisReader = new FileInputStream(f);
        
        byte[] fileBytes = new byte[(int) f.length()];
        fisReader.read(fileBytes);
        
        String encodedfile = Base64.getEncoder().encodeToString(fileBytes);
        
        String fName = f.getName().substring(0, f.getName().lastIndexOf('.'));
        
        PrintWriter pwWriter = new PrintWriter(
                new File(outputDir.getAbsolutePath() + File.separator + fName + ".b64"));
        
        pwWriter.println(f.getName());
        pwWriter.print(encodedfile);
        
        pwWriter.close();
        fisReader.close();
    }
    
    private void startDecodeBase64() {
        
        for (int i = 0; i < lisftOfFiles.length; i++) {
            
            try {
                
                updateProgress(i, lisftOfFiles.length);
                updateMessage(String.format("%d / %d > %s", i + 1, lisftOfFiles.length, lisftOfFiles[i].getName()));
                
                decodeBase64(lisftOfFiles[i]);
            }
            catch (IOException e) {
                System.err.println(e);
                updateMessage(String.format("Error en el archivo %s", lisftOfFiles[i].getName()));
            }
        }
        updateProgress(1, 1);
        updateMessage("Completado");
    }

    private void decodeBase64(File f) throws IOException, FileNotFoundException {
        
        FileInputStream fisReader = new FileInputStream(f);
        Scanner sc = new Scanner(fisReader);
        
        String filename = sc.nextLine();

        File outputFile = new File(outputDir.getAbsolutePath() + File.separator + filename);
        
        outputFile.createNewFile();
        
        FileOutputStream fosWriter = new FileOutputStream(outputFile);
        
        do {
            
            String base64 = sc.nextLine();
            
            byte[] decodedFile = Base64.getDecoder().decode(base64);

            fosWriter.write(decodedFile);
            
        } while(sc.hasNextLine());
        
        fosWriter.close();
        fisReader.close();
        sc.close();
    }
    
    private void decodeBase64_v1(File f) throws IOException, FileNotFoundException {
        
        FileInputStream fisReader = new FileInputStream(f);
        Scanner sc = new Scanner(fisReader);

        String filename = sc.nextLine();
        String base64 = "";
        do base64 += sc.nextLine(); while(sc.hasNextLine());
        
        byte[] decodedFile = Base64.getDecoder().decode(base64);
        
        File outputFile = new File(outputDir.getAbsolutePath() + File.separator + filename);
        
        outputFile.createNewFile();
        
        FileOutputStream fosWriter = new FileOutputStream(outputFile);
        
        fosWriter.write(decodedFile);

        fosWriter.close();
        fisReader.close();
        sc.close();
    }
    

    @Override
    protected Void call() throws Exception {
        
        switch (option) {
            case ENCODE_BASE64 : startEncodeBase64(); break;
            case DECODE_BASE64 : startDecodeBase64(); break;
        }
        
        return null;
    }
    
}
