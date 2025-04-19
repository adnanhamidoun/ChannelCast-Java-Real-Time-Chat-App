/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.socketchatV02;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {

    private static final int PUERTO = 6666;
    private static  Map<String,ObjectOutputStream> clientes = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        rellenarCanalesServidor();

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("Cliente conectado: " + socketCliente.getInetAddress());
                HiloServidor hiloCliente = new HiloServidor(socketCliente, clientes);
                hiloCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rellenarCanalesServidor() {
        ControladorBaseDeDatos cbd = new ControladorBaseDeDatos();
        cbd.insertarCanales();
    }
}
