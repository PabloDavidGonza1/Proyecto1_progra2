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


INSERT INTO libro (titulo, autor, categoria, precio, existencias, anio_publicacion)
VALUES
    ('Cien años de soledad', 'Gabriel García Márquez', 'Novela', 145.00, 12, 1967),
    ('Clean Code', 'Robert C. Martin', 'Tecnico', 220.50, 5, 2008),
    ('El principito', 'Antoine de Saint-Exupéry', 'Infantil', 85.00, 0, 1943);