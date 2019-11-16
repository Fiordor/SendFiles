/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sendfiles;

import java.io.File;

/**
 *
 * @author fiordor
 */
public class ListFileSyn {
    
    private File[] listFile;
    private int length;
    private int pointer;
    
    public ListFileSyn(File[] listFile) {
        this.listFile = listFile;
        length = listFile.length;
        pointer = -1;
    }
    
    public int length() { return length; }
    
    public synchronized int pointer() { return pointer; }
    
    public synchronized File get() {
        pointer++;
        return (pointer < length) ? listFile[pointer] : null;
    }
    
    public synchronized File get(int n) {
        if (n >= length || n < 0) return null;
        else return listFile[n];
    }
    
    public synchronized void reset() { pointer = -1; }
}
