package edu.umg.programacion2.proyecto.ui;

import edu.umg.programacion2.proyecto.dao.LibroDAO;
import edu.umg.programacion2.proyecto.modelo.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private final LibroDAO dao = new LibroDAO();

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JTextField txtTitulo, txtAutor, txtCategoria, txtPrecio, txtExistencias, txtAnio;
    private JButton btnGuardar, btnActualizar, btnEliminar, btnLimpiar;

    private Integer idSeleccionado = null;

    public VentanaPrincipal() {
        super("Catálogo de Libros");
        initComponents();
        cargarTabla();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Título", "Autor", "Categoría", "Precio", "Existencias", "Año"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                cargarFilaEnFormulario(tabla.getSelectedRow());
            }
        });

        JPanel panelForm = new JPanel(new GridLayout(6, 2, 5, 5));
        txtTitulo = new JTextField();
        txtAutor = new JTextField();
        txtCategoria = new JTextField();
        txtPrecio = new JTextField();
        txtExistencias = new JTextField();
        txtAnio = new JTextField();

        panelForm.add(new JLabel("Título:"));       panelForm.add(txtTitulo);
        panelForm.add(new JLabel("Autor:"));         panelForm.add(txtAutor);
        panelForm.add(new JLabel("Categoría:"));     panelForm.add(txtCategoria);
        panelForm.add(new JLabel("Precio:"));        panelForm.add(txtPrecio);
        panelForm.add(new JLabel("Existencias:"));   panelForm.add(txtExistencias);
        panelForm.add(new JLabel("Año publicación:")); panelForm.add(txtAnio);

        btnGuardar = new JButton("Registrar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelForm, BorderLayout.CENTER);
        panelSur.add(panelBotones, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0);
            List<Libro> libros = dao.listarTodos();
            for (Libro l : libros) {
                modeloTabla.addRow(new Object[]{
                        l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria(),
                        l.getPrecio(), l.getExistencias(), l.getAnioPublicacion()
                });
            }
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el listado de libros.", e);
        }
    }

    private void cargarFilaEnFormulario(int fila) {
        idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        txtTitulo.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        txtAutor.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        txtCategoria.setText(String.valueOf(modeloTabla.getValueAt(fila, 3)));
        txtPrecio.setText(String.valueOf(modeloTabla.getValueAt(fila, 4)));
        txtExistencias.setText(String.valueOf(modeloTabla.getValueAt(fila, 5)));
        txtAnio.setText(String.valueOf(modeloTabla.getValueAt(fila, 6)));
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtTitulo.setText("");
        txtAutor.setText("");
        txtCategoria.setText("");
        txtPrecio.setText("");
        txtExistencias.setText("");
        txtAnio.setText("");
        tabla.clearSelection();
    }

    // --- Validación ---
    private Libro validarFormulario() {
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        String categoria = txtCategoria.getText().trim();

        if (titulo.isEmpty() || autor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Título y autor son obligatorios.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser mayor a cero.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int existencias;
        try {
            existencias = Integer.parseInt(txtExistencias.getText().trim());
            if (existencias < 0) {
                JOptionPane.showMessageDialog(this, "Las existencias no pueden ser negativas.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Las existencias deben ser un número entero.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int anio;
        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
            int anioActual = Year.now().getValue();
            if (anio > anioActual) {
                JOptionPane.showMessageDialog(this, "El año de publicación no puede ser futuro.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El año debe ser un número entero.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Libro libro = new Libro(titulo, autor, categoria, precio, existencias, anio);
        if (idSeleccionado != null) {
            libro.setId(idSeleccionado);
        }
        return libro;
    }

    // --- Acciones CRUD ---
    private void registrar() {
        Libro libro = validarFormulario();
        if (libro == null) return;

        try {
            dao.crear(libro);
            JOptionPane.showMessageDialog(this, "Libro registrado.");
            limpiarFormulario();
            cargarTabla();
        } catch (SQLException e) {
            mostrarError("No se pudo registrar el libro.", e);
        }
    }

    private void actualizar() {
        if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla primero.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Libro libro = validarFormulario();
        if (libro == null) return;

        try {
            boolean ok = dao.actualizar(libro);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Libro actualizado.");
                limpiarFormulario();
                cargarTabla();
            }
        } catch (SQLException e) {
            mostrarError("No se pudo actualizar el libro.", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla primero.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas eliminar este libro?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                dao.eliminar(idSeleccionado);
                JOptionPane.showMessageDialog(this, "Libro eliminado.");
                limpiarFormulario();
                cargarTabla();
            } catch (SQLException e) {
                mostrarError("No se pudo eliminar el libro.", e);
            }
        }
    }

    private void mostrarError(String mensajeUsuario, SQLException e) {
        System.err.println("Error SQL: " + e.getMessage());
        JOptionPane.showMessageDialog(this, mensajeUsuario,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}