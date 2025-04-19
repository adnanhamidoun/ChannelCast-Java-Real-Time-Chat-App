/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.socketchatV02;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ControladorBaseDeDatos {
    private static final String URL = "jdbc:mysql://localhost:3306/ChatDB";
    private static final String USUARIO = "root";
    private static final String CONTRASEÑA = "";


    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASEÑA);
    }


    public void insertarCanales() {
        String consulta = "INSERT INTO canales VALUES (?)";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, "Futbol");
            statement.executeUpdate();
            statement.setString(1, "Noticias");
            statement.executeUpdate();
            statement.setString(1, "CriptoMonedas");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countMensajesNuevos(){
        String consulta = "SELECT COUNT(*) FROM usuarios_mensajes WHERE isRead = 0";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
    public void setMensajesLeidos(){
        String consulta = "UPDATE usuarios_mensajes SET isRead = 1";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean insertarUsuarioEnCanal(String user, String canal) {
        String consulta = "INSERT INTO usuarios_canales VALUES (?, ?)";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            statement.setString(2, canal);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> listarCanales() {
        List<String> canales = new ArrayList<>();
        String consulta = "SELECT * FROM canales";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta); ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {
                canales.add(resultado.getString("nombreCanal"));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return canales;
    }

    public List<String> listarCanalesByNickName(String nickName) {
        List<String> canales = new ArrayList<>();
        String consulta = "SELECT nombreCanal FROM usuarios_canales WHERE user = ?";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, nickName);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    canales.add(resultado.getString("nombreCanal"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return canales;
    }

    public boolean verificarNickName(String user) {
        String consulta = "SELECT COUNT(*) FROM Usuarios WHERE user = ?";
        try (Connection conexion = obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUserFromCanal(String user, String canal) {
        String consulta = "DELETE FROM usuarios_canales WHERE user = ? AND nombreCanal = ?";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            statement.setString(2, canal);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUsuario(String user, String password) {
        String consulta = "SELECT COUNT(*) FROM Usuarios WHERE user = ? AND password = ?";
        try (Connection conexion = obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            statement.setString(2, password);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void registrarUsuario(String user, String password) {
        String consulta = "INSERT INTO Usuarios (user, password) VALUES (?, ?)";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            statement.setString(2, password);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUsuarioOnline(String user) {
        String consulta = "UPDATE Usuarios SET isOnline = true WHERE user = ? ";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUsuarioOffline(String user) {
        String consulta = "UPDATE Usuarios SET isOnline = false WHERE user = ? ";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> listarUsuariosOnline() {
        List<String> usuariosOnline = new ArrayList<>();
        String consulta = "SELECT user FROM Usuarios WHERE isOnline = true";

        try (Connection conexion = obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(consulta);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {
                String usuario = resultado.getString("user");
                usuariosOnline.add(usuario);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar los usuarios online: " + e.getMessage());
            e.printStackTrace();
        }

        return usuariosOnline;
    }

    public String checkUserIsConnected(String user) {
        String consulta = "SELECT isOnline FROM Usuarios WHERE user = ?";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, user);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return String.valueOf(resultado.getBoolean("isOnline"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public void guardarMensaje(String emisor, String mensaje, String destinatario) {
        String consulta = "INSERT INTO usuarios_mensajes (usuarioEmisor,mensaje,usuarioDestinatario)VALUES (?,?,?)";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, emisor);
            statement.setString(2, mensaje);
            statement.setString(3, destinatario);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getMensajesByNickName(String nickName) {
        List<String> mensajes = new ArrayList<>();
        String consulta = "SELECT usuarioEmisor,mensaje,fechaEnvio FROM usuarios_mensajes WHERE usuarioDestinatario = ?";
        try (Connection conexion = obtenerConexion(); PreparedStatement statement = conexion.prepareStatement(consulta)) {
            statement.setString(1, nickName);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    String usuario = resultado.getString("usuarioEmisor");
                    String mensaje = resultado.getString("mensaje");
                    LocalDateTime fechaEnvio = resultado.getTimestamp("fechaEnvio").toLocalDateTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String fechaEnvioFormateada = fechaEnvio.format(formatter);
                    String mensajeFinal = usuario + " te envio un mensaje el " + fechaEnvioFormateada + " -> " + mensaje;
                    mensajes.add(mensajeFinal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensajes;
    }
}
  