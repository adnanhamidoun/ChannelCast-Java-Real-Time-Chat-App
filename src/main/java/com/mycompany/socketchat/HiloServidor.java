/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.socketchatV02;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class HiloServidor extends Thread {

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final Map<String, ObjectOutputStream> clientes;
    private Mensaje mensaje;
    private Cliente cliente = new Cliente("");
    private boolean loginExitoso = false;
    private DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private ControladorBaseDeDatos cbd = new ControladorBaseDeDatos();
    private Apis apis = new Apis();

    public HiloServidor(Socket socket, Map<String, ObjectOutputStream> clientes) {
        this.socket = socket;
        this.clientes = clientes;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            do {
                mensaje = (Mensaje) ois.readObject();
                if (!mensaje.getCliente().getRegistro()) {
                    loginExitoso = procesarLogin(mensaje.getCliente());
                } else {
                    procesarRegistro(mensaje.getCliente());
                }
            } while (!loginExitoso);

            clientes.put(mensaje.getCliente().getNombreUsuario(), oos);
            cliente = mensaje.getCliente();

            int contador = cbd.countMensajesNuevos();
            if (contador > 0) {
                enviarMensaje("Mensajes nuevos -> " + contador + "  Escribe /entradas para verlos.", oos);
            }

            while ((mensaje = (Mensaje) ois.readObject()) != null) {
                if ("*".equals(mensaje.getTexto())) {
                    System.out.println("Cierre moderado");
                    break;
                }
                tratarMensaje();
            }
        } catch (Exception e) {
            System.err.println("Cierre abrupto.");
            e.printStackTrace();
        } finally {
            cbd.setUsuarioOffline(cliente.getNombreUsuario());
            // Eliminar al usuario por su nickname, no por el stream.
            clientes.remove(cliente.getNombreUsuario());
            cerrarConexiones();
        }
    }

    private boolean procesarLogin(Cliente cliente) {
        boolean loginExitoso = cbd.loginUsuario(cliente.getNombreUsuario(), cliente.getPassword());
        if (loginExitoso) {
            cbd.setUsuarioOnline(cliente.getNombreUsuario());
        }
        String respuesta = loginExitoso
                ? "Bienvenido " + cliente.getNombreUsuario()
                : "Nickname o contraseña incorrectos, inténtelo de nuevo";
        enviarMensaje(respuesta, oos);
        return loginExitoso;
    }

    private void procesarRegistro(Cliente cliente) {
        boolean clienteExistente = cbd.verificarNickName(cliente.getNombreUsuario());
        String respuesta = clienteExistente
                ? "El nickname ya existe, inténtelo de nuevo"
                : "Usuario registrado con éxito, por favor inicie sesión";
        if (!clienteExistente) {
            cbd.registrarUsuario(cliente.getNombreUsuario(), cliente.getPassword());
        }
        enviarMensaje(respuesta, oos);
    }

    private void tratarMensaje() {
        String texto = mensaje.getTexto();
        if (texto == null || texto.isEmpty()) {
            return;
        }
        if (texto.startsWith("@")) {
            procesarMensajePrivado(texto);
        } else if (texto.startsWith("/")) {
            procesarComando(texto);
        } else {
            enviarATodos();
        }
    }

    private void procesarMensajePrivado(String texto) {
        // Formato requerido: @destinatario mensaje
        int espacio = texto.indexOf(" ");
        if (espacio != -1) {
            String destinatario = texto.substring(1, espacio);
            String mensajePrivado = texto.substring(espacio + 1);
            enviarMensajePrivado(destinatario, mensajePrivado);
        } else {
            enviarMensaje("Mensaje privado mal formado.", oos);
        }
    }

    private void enviarMensajePrivado(String destinatario, String mensajePrivado) {
        String resultado = cbd.checkUserIsConnected(destinatario);
        switch (resultado) {
            case "true":
                enviarMensaje("Mensaje privado de " + cliente.getNombreUsuario() + ": " + mensajePrivado,
                        clientes.get(destinatario));
                break;
            case "false":
                cbd.guardarMensaje(cliente.getNombreUsuario(), mensajePrivado, destinatario);
                enviarMensaje("El usuario está desconectado. El mensaje se ha guardado en su bandeja de entrada para cuando se conecte.", oos);
                break;
            case null:
                enviarMensaje("Usuario desconocido.", oos);
                break;
            default:
                break;
        }
    }

    private void procesarComando(String texto) {
        int primerEspacio = texto.indexOf(" ");
        int segundoEspacio = texto.indexOf(" ", primerEspacio + 1);
        if (primerEspacio != -1) {
            String comando = texto.substring(0, primerEspacio);
            String argumento = texto.substring(primerEspacio + 1);
            switch (comando) {
                case "/unirse":
                    unirseCanal(argumento);
                    break;
                case "/salir":
                    salirseCanal(argumento);
                    break;
                case "/mg":
                    String[] contenido = procesarComandoArgumentos(primerEspacio, segundoEspacio, comando, texto);
                    if (contenido != null) {
                        comprobarCanal(contenido);
                    }
                    break;
                default:
                    enviarMensaje("Comando desconocido: " + comando, oos);
                    break;
            }
        } else {
            // Comandos sin argumentos.
            switch (texto) {
                case "/listarCanales":
                    listarCanales();
                    break;
                case "/misCanales":
                    misCanales();
                    break;
                case "/listarUsuarios":
                    listarUsuariosConectados();
                    break;
                case "/entradas":
                    visualizarMensajes();
                    break;
                case "/laLiga":
                    visualizarLaLiga();
                    break;
                case "/noticias":
                    visualizarNoticias();
                    break;
                case "/criptos":
                    visualizarCriptoMonedas();
                    break;
                case "/help":
                    visualizarAyuda();
                    break;
                default:
                    enviarMensaje("Comando desconocido.", oos);
                    break;
            }
        }
    }


    private void visualizarContenidoPorCanal(String canal, Supplier<String> apiSupplier) {
        List<String> canalesUsuario = cbd.listarCanalesByNickName(cliente.getNombreUsuario());
        if (canalesUsuario != null && canalesUsuario.contains(canal)) {
            String output = apiSupplier.get();
            enviarMensaje(output, oos);
        } else {
            enviarMensaje("Este comando es exclusivo para usuarios unidos al canal de " + canal + ".", oos);
        }
    }

    private void visualizarCriptoMonedas() {
        visualizarContenidoPorCanal("CriptoMonedas", () -> apis.getCriptoMonedas());
    }

    private void visualizarNoticias() {
        visualizarContenidoPorCanal("Noticias", () -> apis.getNoticias());
    }

    private void visualizarLaLiga() {
        visualizarContenidoPorCanal("Futbol", () -> apis.getClasificacionSpain());
    }

    private void visualizarMensajes() {
        List<String> mensajesPendientes = cbd.getMensajesByNickName(cliente.getNombreUsuario());
        if (mensajesPendientes == null || mensajesPendientes.isEmpty()) {
            enviarMensaje("No tienes mensajes nuevos.", oos);
        } else {
            for (String msg : mensajesPendientes) {
                enviarMensaje(msg, oos);
            }
            cbd.setMensajesLeidos();
        }
    }

    private void enviarATodos() {
        String mensajeFormateado = cliente.getNombreUsuario() + ": " + mensaje.getTexto();
        System.out.println(mensaje.getCliente() + ": Enviado mensaje a todos los usuarios || " + LocalDateTime.now().format(formatoHora));
        for (Map.Entry<String, ObjectOutputStream> entry : clientes.entrySet()) {
            try {
                // Se evita enviar el mensaje al remitente.
                if (!entry.getKey().equals(cliente.getNombreUsuario())) {
                    enviarMensaje(mensajeFormateado, entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void listarCanales() {
        System.out.println(mensaje.getCliente() + ": Listado de canales || " + LocalDateTime.now().format(formatoHora));
        List<String> canales = cbd.listarCanales();
        enviarMensaje("Canales disponibles: " + canales.size() + "\n" + canales.toString(), oos);
    }

    private void misCanales() {
        System.out.println(mensaje.getCliente() + ": Listado de mis canales || " + LocalDateTime.now().format(formatoHora));
        List<String> canales = cbd.listarCanalesByNickName(cliente.getNombreUsuario());
        enviarMensaje("Canales a los que perteneces: " + canales.size(), oos);
        if (!canales.isEmpty()) {
            enviarMensaje(canales.toString(), oos);
        }
    }

    private String[] procesarComandoArgumentos(int primerEspacio, int segundoEspacio, String comando, String texto) {
        if (segundoEspacio != -1) {
            String argumento = texto.substring(primerEspacio + 1, segundoEspacio);
            String mensajeCanal = texto.substring(segundoEspacio + 1);
            return new String[]{comando, argumento, mensajeCanal};
        } else {
            enviarMensaje("Comando " + comando + " mal formado.", oos);
            return null;
        }
    }

    private void unirseCanal(String nombreCanal) {
        if (Character.isUpperCase(nombreCanal.charAt(0))) {
            if (cbd.insertarUsuarioEnCanal(cliente.getNombreUsuario(), nombreCanal)) {
                enviarMensaje("Te has unido al canal " + nombreCanal, oos);
            } else {
                enviarMensaje("El canal " + nombreCanal + " no existe.", oos);
            }
        } else {
            enviarMensaje("El canal " + nombreCanal + " no existe.", oos);
        }
    }

    private void salirseCanal(String nombreCanal) {
        if (cbd.deleteUserFromCanal(cliente.getNombreUsuario(), nombreCanal)) {
            enviarMensaje("Has abandonado el canal " + nombreCanal, oos);
        } else {
            enviarMensaje("El canal " + nombreCanal + " no existe.", oos);
        }
    }

    private void comprobarCanal(String[] contenido) {
        List<String> canalesServidor = cbd.listarCanales();
        for (String canal : canalesServidor) {
            if (canal.equals(contenido[1])) {
                escribirCanal(contenido[1], contenido[2]);
                break;
            }
        }
    }

    private void escribirCanal(String nombreCanal, String contenidoMensaje) {
        for (Map.Entry<String, ObjectOutputStream> entry : clientes.entrySet()) {
            try {
                List<String> canalesUsuario = cbd.listarCanalesByNickName(entry.getKey());
                if (canalesUsuario != null && canalesUsuario.contains(nombreCanal)
                        && !entry.getKey().equals(cliente.getNombreUsuario())) {
                    String mensajeCanal = String.format("[%s] %s dice: %s", nombreCanal, cliente.getNombreUsuario(), contenidoMensaje);
                    enviarMensaje(mensajeCanal, entry.getValue());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void listarUsuariosConectados() {
        System.out.println(mensaje.getCliente() + ": Listado de usuarios en línea || " + LocalDateTime.now().format(formatoHora));
        List<String> usuariosOnline = cbd.listarUsuariosOnline();
        enviarMensaje("Usuarios conectados: " + usuariosOnline.size() + "\n" + usuariosOnline.toString(), oos);
    }

    private void enviarMensaje(String msg, ObjectOutputStream outStream) {
        try {
            outStream.writeObject(new Mensaje(new Cliente(""), msg));
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cerrarConexiones() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void visualizarAyuda() {
        String ayuda = "      GUIA DE COMANDOS DEL CHAT     \n" +
                "  @usuario mensaje  = Enviar un mensaje privado.\n" +
                "  /unirse canal     = Unirte a un canal.\n" +
                "  /salir canal      = Salir de un canal.\n" +
                "  /listarCanales    = Ver canales disponibles.\n" +
                "  /misCanales       = Ver canales a los que estás unido.\n" +
                "  /listarUsuarios   = Ver usuarios conectados.\n" +
                "  /mg canal mensaje = Enviar mensaje a un canal.\n" +
                "  /entradas         = Ver mensajes recibidos mientras estabas fuera.\n" +
                "  /laLiga           = Listar la clasificación de la Liga Española (Solo para canal Futbol).\n" +
                "  /noticias         = Listar las Noticias más actuales (Solo para canal Noticias).\n" +
                "  /criptos          = Listar top criptomonedas actuales (Solo para canal CriptoMonedas).\n" +
                "  (*) Desconectar   = Cerrar la conexión.\n" +
                "  ¡Disfruta la comunicación!";
        enviarMensaje(ayuda, oos);
    }
}
