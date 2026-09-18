package edu.umg.programacion2.proyecto.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import edu.umg.programacion2.proyecto.modelo.Libro;

public class LibroDAO {

    public Libro crear(Libro item) throws SQLException {
        return null;
    }

    public List<Libro> listarTodos() throws SQLException {
        return null;
    }

    public Optional<Libro> buscarPorId(int id) throws SQLException {
        return Optional.empty();
    }

    public boolean actualizar(Libro item) throws SQLException {
        return false;
    }

    public boolean eliminar(int id) throws SQLException {
        return false;
    }
}