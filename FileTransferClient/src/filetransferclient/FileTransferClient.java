/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransferclient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author xeio
 */
public class FileTransferClient {

    private Socket s;

    private static final int TIME_TO_SLEEEP = 600000; // 10 minutos
    private static final String FILE_FOLDER = "/home/xeio/boletos/server/";
    private static final int SERVER_SOCKET = 6666;
    private boolean connected = false;

    public FileTransferClient(String host, int port, File file) throws Exception {
        try {
            s = new Socket(host, port);
            if (s.isConnected()) {
                connected = true;
            }
            //sendFile(file);
        } catch (Exception e) {
            throw new Exception("No existe conexion con el Servidor de Archivos. Inicie el Servidor");
            //e.printStackTrace();
        }
    }

    public static boolean validIp(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if (i < 0 || i > 255) {
                    return false;
                }
            }

            if (ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException ex) {
            return false;
        }

    }

    public void sendFile(File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        dos.writeUTF(file.getName());

        int read;
        while ((read = fis.read(buffer)) > 0) {
            dos.write(buffer, 0, read);
        }

        fis.close();
        dos.close();
    }

    public static void main(String[] args) {
        while (true) {
            try {

                if (args[0] == null) {
                    System.out.println("Debe incluir una IP en el comando");
                    return;
                }

                if (args[1] == null) {
                    System.out.println("Debe incluir un puerto 6661 para amadeus 6662 para sabre");
                    return;
                }

                if (args[2] == null) {
                    System.out.println("Debe incluir una carpeta por ejemplo C:/AIR");
                    return;

                }

                String ipServer = args[0];

                boolean isValid = FileTransferClient.validIp(ipServer);

                if (!isValid) {
                    System.out.println("La ip ingresada no es valida");
                }
                int socket = Integer.parseInt(args[1]);

                String path = args[2];

                System.out.println("Conectandose con : %s".replace("%s", ipServer));

                File folderPath = new File(path);
                File backupFolder = new File(path+ "/backup/" );
                
                if (!backupFolder.exists()){
                    backupFolder.mkdir();
                }

                for (File f : folderPath.listFiles()) {

                    if (!f.isDirectory() && !f.getName().contains(".jar") && !f.getName().contains(".bat")) {
                        FileTransferClient fc = new FileTransferClient(ipServer, socket, f);
                        if (fc.connected) {
                            try {
                                fc.sendFile(f);
                                Path from = Paths.get(f.getAbsolutePath());
                                Path to = Paths.get(backupFolder.getAbsolutePath() + "/" + f.getName());
                                Files.move(from, to);
                            } catch (IOException ex) {
                                Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                }

                Thread.sleep(TIME_TO_SLEEEP);
            } catch (InterruptedException ex) {

                Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(0);
            }

        }
    }

}
