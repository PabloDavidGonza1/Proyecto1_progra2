package edu.umg.programacion2.proyecto.dao;

import edu.umg.programacion2.proyecto.modelo.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibroDAO {

	private static final String URL = "jdbc:mariadb://localhost:3306/libreria_db";
    private static final String USUARIO = "root";
    private static final String CLAVE = "pablo1213";

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    public Libro crear(Libro libro) throws SQLException {
        String sql = "INSERT INTO libro (titulo, autor, categoria, precio, existencias, anio_publicacion) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getCategoria());
            ps.setBigDecimal(4, libro.getPrecio());
            ps.setInt(5, libro.getExistencias());
            ps.setInt(6, libro.getAnioPublicacion());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    libro.setId(rs.getInt(1));
                }
            }
            return libro;
        }
    }

    public List<Libro> listarTodos() throws SQLException {
        String sql = "SELECT * FROM libro ORDER BY id";
        List<Libro> lista = new ArrayList<>();

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Libro> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM libro WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean actualizar(Libro libro) throws SQLException {
        String sql = "UPDATE libro SET titulo=?, autor=?, categoria=?, precio=?, "
                   + "existencias=?, anio_publicacion=? WHERE id=?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getCategoria());
            ps.setBigDecimal(4, libro.getPrecio());
            ps.setInt(5, libro.getExistencias());
            ps.setInt(6, libro.getAnioPublicacion());
            ps.setInt(7, libro.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM libro WHERE id = ?";
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("categoria"),
                rs.getBigDecimal("precio"),
                rs.getInt("existencias"),
                rs.getInt("anio_publicacion")
        );
    }
}