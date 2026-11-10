-- Base de datos para Healt&Blood
-- Fecha 15-10-26
-- v1

DROP DATABASE IF EXISTS healthBlood;

CREATE DATABASE healthBlood;

USE healthBlood;

CREATE TABLE users(
    id_usuario INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(45) NOT NULL,
    primer_apellido VARCHAR(45) NOT NULL,
    segundo_apellido VARCHAR(45),
    fecha_nacimiento DATE,
    tipo_diabetes VARCHAR(30),
    fecha_registro DATE
);

CREATE TABLE lectura_glucosa(
    id_lectura INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    valor_mg_dl SMALLINT UNSIGNED NOT NULL,
    tipo_lectura VARCHAR(45), -- AYUNAS, ANTES DE DORMIR, DESPUES DE ALIMENTOS
    notas TEXT NULL,
    fecha_registro DATE,
    FOREIGN KEY(id_usuario) REFERENCES users(id_usuario)
);

INSERT INTO users (nombre, primer_apellido, segundo_apellido, fecha_nacimiento, tipo_diabetes, fecha_registro)
VALUES ("Juan Carlos", "Orozco", "Nieto", "2006-05-27", "Tipo 1", CURDATE());

SELECT * FROM users;
