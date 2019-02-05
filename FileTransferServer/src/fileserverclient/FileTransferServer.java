/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileserverclient;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author xeio
 */
public class FileTransferServer extends Thread {

    private ServerSocket ss;
    private static final int SERVER_SOCKET = 6666 ;

    public FileTransferServer(int port) {
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(e.getCause());
        }
    }

    public void run() {
        while (true) {
            try {
                Socket clientSock = ss.accept();
                saveFile(clientSock);
            } catch (IOException e) {
                System.err.println(e.getCause());
            }
        }
    }

    private void saveFile(Socket clientSock) throws IOException {
        DataInputStream dis = new DataInputStream(clientSock.getInputStream());

        String filename = dis.readUTF();

        File f = new File(filename);
        if (f.exists()) {
            System.out.println("El archivo %s ya existe".replace("%s", filename));

        } else {
            FileOutputStream fos = new FileOutputStream(filename);
            byte[] buffer = new byte[4096];

            System.out.println("Transfiriendo: %s".replace("%s",filename));

            int filesize = 15123; // Send file size in separate msg
            int read = 0;
            int totalRead = 0;
            int remaining = filesize;
            while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                System.out.println("read " + totalRead + " bytes.");
                fos.write(buffer, 0, read);
            }
            fos.close();
        }
        dis.close();
    }

    public static void main(String[] args) {
        FileTransferServer fs = new FileTransferServer(SERVER_SOCKET);
        fs.start();
    }

}
