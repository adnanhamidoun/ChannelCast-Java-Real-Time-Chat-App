DROP
DATABASE IF EXISTS ChatDB;
Create
DATABASE ChatDb;
Use
ChatDB;
CREATE TABLE Usuarios
(
    user       VARCHAR(255) PRIMARY KEY,
    password   VARCHAR(255),
    isOnline  BOOLEAN
);


CREATE TABLE canales(
    nombreCanal VARCHAR(255) PRIMARY KEY
);

CREATE TABLE usuarios_canales (
                                  user VARCHAR(255),
                                  nombreCanal VARCHAR(255),
                                  PRIMARY KEY (user, nombreCanal),
                                  CONSTRAINT fk_usuario FOREIGN KEY (user) REFERENCES Usuarios(user) ON DELETE CASCADE,
                                  CONSTRAINT fk_canal FOREIGN KEY (nombreCanal) REFERENCES canales(nombreCanal) ON DELETE CASCADE
);

CREATE TABLE usuarios_mensajes (
                                   id_mensaje INT AUTO_INCREMENT PRIMARY KEY,
                                   usuarioEmisor VARCHAR(255) NOT NULL,
                                   usuarioDestinatario VARCHAR(255) NOT NULL,
                                   mensaje TEXT NOT NULL,
                                   fechaEnvio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   isRead BOOLEAN DEFAULT FALSE,
                                   CONSTRAINT fk_emisor FOREIGN KEY (usuarioEmisor) REFERENCES Usuarios(user) ON DELETE CASCADE,
                                   CONSTRAINT fk_remitente FOREIGN KEY (usuarioDestinatario) REFERENCES Usuarios(user) ON DELETE CASCADE
);

