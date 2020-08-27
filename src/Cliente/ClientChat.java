/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.System.exit;
import java.net.Socket;
import java.util.Scanner;

public class ClientChat {

    public static void main(String[] args) throws IOException {

        ClientChat m = new ClientChat();
        m.connect();
    }

    public void connect() throws IOException {
        //Se declara el Scanner para mensajes
        Scanner teclado = new Scanner(System.in);

        // localhost ip
        String ip = "127.0.0.1";
        int puerto = 4444;
        Socket socket = null;

        System.out.print("Escriba su nombre: ");
        String name = teclado.nextLine();

        try {

            //connect
            socket = new Socket(ip, puerto);

            //streams
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //se inicia una thread que escucha por informacion
            new ReceiveMessage(in).start();

            // se envia informacion al servidor
            out.writeUTF(name);

            while (true) {
                //Donde se escriben los mensajes y se mandan
                String message = teclado.nextLine();
                out.writeUTF(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }

    class ReceiveMessage extends Thread {

        DataInputStream in;

        ReceiveMessage(DataInputStream in) {
            this.in = in;
        }

        public void run() {
            String message;
            while (true) {
                try {
                    message = in.readUTF();
                    System.out.println(message);

                } catch (IOException e) {
                
                    exit(0);
                }

            }
        }

    }

}
