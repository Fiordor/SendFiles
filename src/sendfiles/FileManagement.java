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
import java.util.concurrent.atomic.AtomicInteger;
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
    
    public static final int MAX_FILE_SIZE = 100000;//100KB
    
    private File[] listFiles;
    private AtomicInteger pointer;
    private File dir;
    private String ip;
    private int port;
    private Socket client;
    
    private int option;

    public FileManagement(File[] listFiles, File dir, AtomicInteger pointer, int option) {
        this.listFiles = listFiles;
        this.pointer = pointer;
        this.dir = dir;
        ip = null; port = -1; client = null;
        this.option = option;
    }
    
    public FileManagement(File[] listFiles, AtomicInteger pointer, String ip, int port, int option) {
        this.listFiles = listFiles;
        this.pointer = pointer;
        dir = null;
        this.ip = ip; this.port = port; client = null;
        this.option = option;
    }
    
    public FileManagement(File dir, Socket client, int option) {
        listFiles = null;
        pointer = null;
        this.dir = dir;
        ip = null; port = -1; this.client = client;
        this.option = option;
    }
    
    private void startEncodeBase64() {
        
        int encoded = 0;
        File f = null;
        int pointerValue = 0;
        while ((pointerValue = pointer.getAndIncrement()) < listFiles.length) {
            
            f = listFiles[pointerValue];

            try {
                
                updateMessage(String.format("%d / %d > %s", encoded, listFiles.length, f.getName()));
                
                encodeBase64(f);
            } catch (IOException e) {
                System.err.println(e);
                updateMessage(String.format("Error en el archivo %s", f.getName()));
            }
            encoded++;
        }
        
        updateProgress(1, 1);
        updateMessage("Completado");
    }

    private void encodeBase64(File f) throws IOException, FileNotFoundException {

        String fName = f.getName().substring(0, f.getName().lastIndexOf('.'));
        
        PrintWriter pwWriter = new PrintWriter(
                new File(dir.getAbsolutePath() + File.separator + fName + ".b64"));
        
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
                updateProgress(readBytes, fileBytes.length);
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
    
    private void startDecodeBase64() {
        
        int decoded = 0;
        File f = null;
        int pointerValue = 0;
        while ((pointerValue = pointer.getAndIncrement()) < listFiles.length) {
            
            f = listFiles[pointerValue];
            
            try {
                
                updateMessage(String.format("%d / %d > %s", decoded, listFiles.length, f.getName()));
                
                decodeBase64(f);
            } catch (IOException e) {
                System.err.println(e);
                updateMessage(String.format("Error en el archivo %s", f.getName()));
            }
            decoded++;
        }
        
        updateProgress(1, 1);
        updateMessage("Completado");
    }

    private void decodeBase64(File f) throws IOException, FileNotFoundException {
        
        FileInputStream fisReader = new FileInputStream(f);
        Scanner sc = new Scanner(fisReader);
        
        String filename = sc.nextLine();

        File outputFile = new File(dir.getAbsolutePath() + File.separator + filename);
        
        outputFile.createNewFile();
        
        FileOutputStream fosWriter = new FileOutputStream(outputFile);
        
        int readBytes = 0;
        do {
            
            String base64 = sc.nextLine();
            
            byte[] decodedFile = Base64.getDecoder().decode(base64);
            
            readBytes += base64.length();
            updateMessage(String.format("%s > %.1f MB / %.1f MB", f.getName(), readBytes / 1000000.0, f.length() / 1000000.0));            
            updateProgress(readBytes, f.length());
            
            fosWriter.write(decodedFile);            
            
        } while(sc.hasNextLine());
        
        fosWriter.close();
        fisReader.close();
        sc.close();
    }
    
    private void startClientMode() {
        
        try {
            
            client = new Socket(ip, port);
        
            PrintWriter pwWriter = new PrintWriter(client.getOutputStream(), true);            
            
            File f = null;
            int pointerValue = 0;
            while ((pointerValue = pointer.getAndIncrement()) < listFiles.length) {
                
                f = listFiles[pointerValue];
                
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
                        updateProgress(readBytes, fileBytes.length);
                    }
                }

                String encodedfile = writeEncodeBase64(fileBytes, readBytes, fileBytes.length - readBytes, fisReader);
                pwWriter.print(encodedfile);

                pwWriter.close();
                fisReader.close();            
            }
        } catch (IOException e) {
            updateMessage(e.getMessage());
        }
    }
    
    public void startServerMode() {

        try {
            
            Scanner receive = new Scanner(client.getInputStream());
            File fAux = new File("temp");
            String data = "";
            File file = null;
            PrintWriter pwWriter = new PrintWriter(fAux);

            do {

                data = receive.nextLine();

                if (data.contains(".")) {

                    pwWriter.close();

                    file = new File(dir.getAbsolutePath() + File.separator + data);
                    
                    updateMessage(String.format("%s", file.getName()));
                    updateProgress(0, 0);
                    
                    try { pwWriter = new PrintWriter(file); }
                    catch (FileNotFoundException e) { updateMessage(e.getMessage()); }

                } else {
                    updateMessage(String.format("%s > %d", file.getName(), file.length()));
                    updateProgress(0, 0);
                    
                    pwWriter.print(data);
                }

            } while (receive.hasNextLine());

            pwWriter.close();
            fAux.delete();
            
        } catch (IOException e) {
            updateMessage(e.getMessage());
        }
    }

    @Override
    protected Void call() throws Exception {
        
        long t = System.currentTimeMillis();
        
        switch (option) {
            case ENCODE_BASE64 : startEncodeBase64(); break;
            case DECODE_BASE64 : startDecodeBase64(); break;
            case CLIENT_MODE   : startClientMode();   break;
            case SERVER_MODE   : startServerMode();   break;
        }
        
        t = System.currentTimeMillis() - t;
        
        long m = (t / 1000) / 60;
        long s = (t / 1000) - (m * 60);
        t = t - (m * 60 * 1000) - (s * 1000);
        
        updateMessage(String.format("Tiempo: %d m %d s %d ms", m, s, t));
        updateProgress(1, 1);
        
        return null;
    }
    
}
