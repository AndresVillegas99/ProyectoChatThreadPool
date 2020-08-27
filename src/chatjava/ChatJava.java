/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatjava;

/**
 *
 * @author User
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.logging.Handler;

public class ChatJava {

    private static int clientesEnReunion = 0;
    private static int MAXCLIENTES = 2;
    private static int MAXESPERANDO = 3;
    private static int puerto = 4444;

    private static ServerSocket server = null;
    public static Socket clientSocket = null;
    // Un array de clientesConectados y de clientes esperando y sus limites
    public static ClientThread[] clientesConectados = new ClientThread[MAXCLIENTES];
    public static ClientThread[] clientesEsperando = new ClientThread[MAXESPERANDO];
    private static ExecutorService pool;

    public static void main(String[] args) throws IOException {
        try {
            server = new ServerSocket(puerto);
            pool = Executors.newFixedThreadPool(MAXCLIENTES + MAXESPERANDO);

            System.out.println("Escuchando al puerto: " + puerto);
        } catch (IOException e) {
            e.printStackTrace();

        }

        while (true) {
            try {
                clientSocket = server.accept();  //Este espera a que un socket con un socket abierto y espera ha que alguien lo utilice.

            } catch (IOException e) {
                e.printStackTrace();
                if (!server.isClosed()) {
                    server.close();
                }
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            }

            System.out.println("Client connected!");
            if (clientesEnReunion < clientesConectados.length) {
                for (int c = 0; c < clientesConectados.length; c++) {
                    //Crea un nuevo thread donde los usuarios o clientes en esta conexion van a estar hablando en la sala de "Reunion de clientes"
                    if (clientesConectados[c] == null) {
                        pool.execute(clientesConectados[c] = new ClientThread(clientSocket, clientesConectados, "Reunion de clientes"));
                        clientesEnReunion++;
                        
                        break;
                    }
                }
            } else {
                //Crea un nuevo thread donde los usuarios o clientes en esta conexion van a estar hablando en la sala de espera
                for (int c = 0; c < clientesEsperando.length; c++) {
                    if (clientesEsperando[c] == null) {
                        pool.execute(clientesEsperando[c] = new ClientThread(clientSocket, clientesEsperando, "Cuarto de espera"));
                        break;
                    }

                }

            }
        }
    }

    public void clienteSalio(ClientThread[] ct, String name) throws IOException {

        for (int c = 0; c < clientesConectados.length; c++) {
            if (name.equals(clientesConectados[c].clientName)) {

                clientesEnReunion = clientesEnReunion - 1;
                for (int e = 0; e < clientesEsperando.length; e++) {
                    if (clientesEsperando[e] != null) {
                        clientesEsperando[e].clientesConectados = clientesConectados[c].clientesConectados;
                        clientesConectados[c] = clientesEsperando[e];
                        clientesConectados[c].room = "Reunion de clientes";
                        clientesEnReunion = clientesEnReunion + 1;
                        clientesEsperando[e].actualizarCuartosReunion(clientesConectados);
                        clientesEsperando[e].actualizarCuartosEspera(clientesEsperando);
                        clientesEsperando[e].clientesEsperando = null;
                        clientesEsperando[e] = clientesEsperando[e + 1];
                        if (clientesEsperando[e] != null) {
                            clientesEsperando[e].actualizarCuartosEspera(clientesEsperando);
                            break;
                        }
                        break;
                    }
                }
                clientesConectados[c].actualizarCuartosReunion(clientesConectados);
                clientesConectados[c].actualizarCuartosEspera(clientesEsperando);
                if (name.equals(clientesConectados[c].clientName)){
                clientesConectados[c] = null;
                }
                clientesConectados[c].sendCambio();
                break;
            }
        }
        for (int c = 0; c < clientesEsperando.length; c++) {
            if (clientesEsperando[c] != null) {
                if (name.equals(clientesEsperando[c].clientName)) {
                    
                    clientesEsperando[c].actualizarCuartosReunion(clientesConectados);
                    clientesEsperando[c].actualizarCuartosEspera(clientesEsperando);
                    clientesEsperando[c] = null;
                    break;
                }
            }
        }
        
    }

}
