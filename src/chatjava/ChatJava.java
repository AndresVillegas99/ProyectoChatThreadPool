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

    public void clienteSalio(String name, String cuarto) throws IOException  { //Metodo que se llama cuando un cliente sale de un cuarto
        if (cuarto.equals("Reunion de clientes")) {
            IntercambioDeUsuarios(name);
        } else {
            for (int c = 0; c < clientesEsperando.length; c++) { // Transforma cualquier usuario que escribio salir en la lista de espera a null
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
    
    private void IntercambioDeUsuarios(String name) throws IOException
    {
        for (int c = 0; c < clientesConectados.length; c++) {
            try {
                if (name.equals(clientesConectados[c].clientName)) {    // En caso de que un cliente en el cuarto de reuniones salga
                    clientesEnReunion = clientesEnReunion - 1;  //Disminuye el contador de clientes en el cuarto
                    for (int e = 0; e < clientesEsperando.length; e++) {
                        if (clientesEsperando[e] != null) {     //Revisa si hay alguna persona en el cuarto de espera
                            clientesEsperando[e].clientesConectados = clientesConectados[c].clientesConectados; //Copia la lista de el cliente actual al cliente que esta esperando para que esten en el mismo cuarto 
                            clientesConectados[c] = clientesEsperando[e]; //El cliente en espera ahora esta en el cuarto de Reunion
                            clientesConectados[c].room = "Reunion de clientes"; // Se cambia el nombre de la habitacion
                            clientesEnReunion = clientesEnReunion + 1; // Ya que habia alguien en el cuarto de espera se le vuelve a sumar 1 a los clientes en Reunion
                            clientesEsperando[e].actualizarCuartosReunion(clientesConectados); // Los siguientes metodos actualizan las listas de clientesEsperando que estan conectados en la clase ClientThread
                            clientesEsperando[e].actualizarCuartosEspera(clientesEsperando);
                            clientesEsperando[e].clientesEsperando = null; // Elimina la lista de clientesEsperando al usuario que se acaba de conectar
                            
                            try {
                                //En caso de que el usuario este de ultimo en la lista de espera, esto es para que no haya salga fuera del rango de la lista
                                clientesEsperando[e] = clientesEsperando[e + 1];
                            } catch (Exception z) {
                                
                            }
 
                            
                            if (clientesEsperando[e] != null) { // en caso de que no haya nadie en la lista de espera, que no actualize.
                                clientesEsperando[e].actualizarCuartosEspera(clientesEsperando);
                                clientesEsperando[e] = null;
                                break;
                            }
                            clientesEsperando[e] = null;
                            break; // Los breaks son para que no siga una vez que encuentre un usuario.
                        }
                    }
                    clientesConectados[c].actualizarCuartosReunion(clientesConectados); //Actualiza la lista del cliente que ahora esta conectado para que reconozca que estan en la misma clase
                    clientesConectados[c].actualizarCuartosEspera(clientesEsperando);
                    if (name.equals(clientesConectados[c].clientName)) { // En caso de que si se hubiera movido alguien de la lista de espera a la de conectados, este if es para que no se les ponga null
                        clientesConectados[c] = null;
                        
                    }else{
                    clientesConectados[c].sendCambio();} //Se le envia un mensaje al usuario que fue movido de cuarto
                    break;
                }
            } catch (Exception e) {
            }
                
            }
    }

}
