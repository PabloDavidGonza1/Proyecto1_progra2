-- sql/schema.sql
CREATE DATABASE IF NOT EXISTS libreria_db;
USE libreria_db;

CREATE TABLE IF NOT EXISTS libro (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    titulo            VARCHAR(150) NOT NULL,
    autor             VARCHAR(100) NOT NULL,
    categoria         VARCHAR(50),
    precio            DECIMAL(10,2) NOT NULL,
    existencias       INT NOT NULL DEFAULT 0,
    anio_publicacion  INT NOT NULL,
    CONSTRAINT chk_precio_positivo CHECK (precio > 0),
    CONSTRAINT chk_existencias_no_negativas CHECK (existencias >= 0)
);