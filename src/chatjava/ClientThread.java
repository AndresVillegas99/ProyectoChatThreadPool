/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatjava;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import sun.nio.ch.ThreadPool;

public class ClientThread implements Runnable {

    public ClientThread[] clientesConectados;
    public ClientThread[] clientesEsperando;
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    public String clientName = null;
    public String room = null;
    private boolean running = true;

    //Constructor
    public ClientThread(Socket socket, ClientThread[] clientesConectados, String room) {
        this.socket = socket;
        if (room.contains("Reunion")) { //Para separar a la gente que va a estar en la reunion y a la gente que va al cuarto de espera.
            this.clientesConectados = clientesConectados;
        } else {
            this.clientesEsperando = clientesConectados;
        }
        this.room = room;
    }

    public void actualizarCuartosReunion(ClientThread[] clientesConectadosAct) { //Actualiza la lista de clientesConectados
        this.clientesConectados = clientesConectadosAct;

    }

    public void actualizarCuartosEspera(ClientThread[] clientesEsperandoAct) {//Actualiza la lista de clientesEsperando

        this.clientesEsperando = clientesEsperandoAct;
    }

    @Override
    public void run() {
        try {
            // Streams 
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            String message = null;
            out.writeUTF(room);
            out.writeUTF("Escriba la palabra 'Salir' para salir del chat"); //Mensaje para guiar al usuario
            clientName = in.readUTF();

            while (true) {
                message = in.readUTF();

                if (message.equals("Salir")) { // En caso de que el usuario quiera salir

                    ChatJava CJ = new ChatJava();

                    if (room.contains("Reunion")) { //Se llama el metodo con el nombre del usuario  y el cuarto para que sepa de donde sacar al usuario
                        CJ.clienteSalio( this.clientName);
                    } else {
                        CJ.clienteSalio( this.clientName);
                    }

                    this.clientesEsperando = null;
                    this.clientesConectados = null;
                    in.close();
                    continue;
                } else {
                    if (room.contains("Reunion")) { // Se utiliza para identificar a cual cuarto se manda cual mensaje.
                        for (int c = 0; c < clientesConectados.length; c++) {
                            if (clientesConectados[c] != null && clientesConectados[c].clientName != this.clientName) { //Para que uno no se mande un mensaje a si mismo
                                clientesConectados[c].sendMessage(message, clientName); //Hace un loop a traves de toda la lista y llama a los objetos del metodo sendMessage
                            }
                        }
                    } else {
                        for (int c = 0; c < clientesEsperando.length; c++) {
                            if (clientesEsperando[c] != null && clientesEsperando[c].clientName != this.clientName) { //Para que uno no se mande un mensaje a si mismo
                                clientesEsperando[c].sendMessage(message, clientName); // Hace un loop a traves de toda la lista y llama a los objetos del metodo sendMessage
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado!");
            this.clientesEsperando = null;
            this.clientesConectados = null;
        }
    }

    // Cada instancia de esta clase(el cliente) usa este metodo
    private void sendMessage(String mess, String name) {
        try {
            out.writeUTF(name + " dijo: " + mess);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendCambio() throws IOException{
    out.writeUTF("Ha sido movido al chat de clientes");
    }
    

}
