/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.socketchatV02;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


/**
 * Representa un cliente dentro del sistema de mensajería.
 * Puede registrarse o iniciar sesión, enviar y recibir mensajes mediante sockets.
 */
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreUsuario;
    private String password;
    private boolean registro;

    public Cliente(String nombreUsuario, String password) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    public Cliente(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public boolean getRegistro() {
        return registro;
    }

    public void setRegistro(boolean registro) {
        this.registro = registro;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    @Override
    public String toString() {
        return nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {
            Cliente cliente = null;
            Mensaje mensajeServidor = null;

            try (
                    Socket socket = new Socket("localhost", 6666);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
            ) {

                do {
                    boolean esRegistro = mostrarInterfazLogin(sc);
                    System.out.print("Introduce un nickname: ");
                    String nickname = sc.nextLine();
                    System.out.print("Introduce una contraseña: ");
                    String pwd = sc.nextLine();

                    cliente = new Cliente(nickname, pwd);
                    cliente.setRegistro(esRegistro);


                    oos.writeObject(new Mensaje(cliente, ""));
                    oos.flush();


                    mensajeServidor = (Mensaje) ois.readObject();
                    System.out.println(mensajeServidor.getTexto());
                } while (!mensajeServidor.getTexto().startsWith("Bienvenido"));

                final Cliente clientFinal = cliente;

                Thread hiloEnviar = new Thread(() -> {
                    try {
                        String texto;
                        System.out.println("Escribe un mensaje (/help para información de comandos):");
                        do {
                            System.out.print("> ");
                            texto = sc.nextLine();
                            oos.writeObject(new Mensaje(clientFinal, texto));
                            oos.flush();
                        } while (!texto.equals("*"));
                    } catch (Exception e) {
                        System.err.println("Error enviando mensaje.");
                        e.printStackTrace();
                    }
                });

                Thread hiloRecibir = new Thread(() -> {
                    try {
                        Object obj;
                        while ((obj = ois.readObject()) != null) {
                            System.out.println(obj);
                            System.out.print("> ");
                        }
                    } catch (Exception e) {
                        System.err.println("Desconectado del servidor.");
                        e.printStackTrace();
                    }
                });

                hiloEnviar.start();
                hiloRecibir.start();

                hiloEnviar.join();

            } catch (Exception e) {
                System.err.println("Error de conexión: " + e.getMessage());
            }
        }
    }


    private static boolean mostrarInterfazLogin(Scanner sc) {
        String opcion;
        do {
            System.out.println("1 - Registrarse");
            System.out.println("2 - Loguearse");
            opcion = sc.nextLine();
        } while (!opcion.equalsIgnoreCase("1") && !opcion.equalsIgnoreCase("2"));

        return opcion.equalsIgnoreCase("1");
    }
}