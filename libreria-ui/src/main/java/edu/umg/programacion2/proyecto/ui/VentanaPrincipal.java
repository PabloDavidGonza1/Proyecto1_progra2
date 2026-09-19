package edu.umg.programacion2.proyecto.ui;

import edu.umg.programacion2.proyecto.dao.LibroDAO;
import edu.umg.programacion2.proyecto.modelo.Libro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    // ---------- Paleta de colores ----------
    private static final Color PRIMARIO     = new Color(0x1565C0);
    private static final Color PRIMARIO_OSC = new Color(0x0D47A1);
    private static final Color FONDO        = new Color(0xF3F5F9);
    private static final Color BLANCO       = Color.WHITE;
    private static final Color EXITO        = new Color(0x2E7D32);
    private static final Color PELIGRO      = new Color(0xC62828);
    private static final Color ADVERTENCIA  = new Color(0xEF6C00);
    private static final Color TEXTO_GRIS   = new Color(0x616161);
    private static final Color BORDE        = new Color(0xCFD8DC);
    private static final Color BORDE_ERROR  = new Color(0xE53935);

    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FUENTE_SUBT   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    private final LibroDAO dao = new LibroDAO();

    private JTabbedPane tabs;

    // --- Tab Catálogo ---
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscar;
    private JLabel lblContador;

    // --- Tab Registrar (solo libros nuevos) ---
    private JTextField txtTituloReg, txtAutorReg, txtCategoriaReg, txtPrecioReg;
    private JSpinner spnExistenciasReg, spnAnioReg, spnFechaIngresoReg;

    // --- Tab Resumen ---
    private JLabel lblTotalLibros, lblTotalEjemplares, lblValorInventario, lblAgotados;

    // --- Barra de estado ---
    private JLabel lblEstado;

    public VentanaPrincipal() {
        super("Sistema de Gestión de Catálogo — Librería");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 640);
        setMinimumSize(new Dimension(860, 540));
        setLocationRelativeTo(null);

        initComponents();
        cargarTabla();
        cargarResumen();
        setEstado("Sistema listo.");
    }

    // =========================================================
    //  CONSTRUCCIÓN DE LA INTERFAZ
    // =========================================================
    private void initComponents() {
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        setJMenuBar(crearMenu());
        add(crearEncabezado(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(FUENTE_SUBT);
        tabs.addTab("Catálogo", crearPanelCatalogo());
        tabs.addTab("Registrar", crearPanelRegistrar());
        tabs.addTab("Resumen", crearPanelResumen());
        tabs.setMnemonicAt(0, KeyEvent.VK_1);
        tabs.setMnemonicAt(1, KeyEvent.VK_2);
        tabs.setMnemonicAt(2, KeyEvent.VK_3);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 2) cargarResumen();
        });

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(tabs, BorderLayout.CENTER);
        centro.add(crearBarraEstado(), BorderLayout.SOUTH);

        add(centro, BorderLayout.CENTER);
    }

    private JMenuBar crearMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);

        JMenu menuAcciones = new JMenu("Acciones");
        menuAcciones.setMnemonic(KeyEvent.VK_C);
        JMenuItem itemNuevo = new JMenuItem("Registrar libro nuevo");
        itemNuevo.addActionListener(e -> tabs.setSelectedIndex(1));
        JMenuItem itemRefrescar = new JMenuItem("Refrescar listado");
        itemRefrescar.addActionListener(e -> {
            cargarTabla();
            cargarResumen();
            setEstado("Listado actualizado.");
        });
        menuAcciones.add(itemNuevo);
        menuAcciones.add(itemRefrescar);

        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setMnemonic(KeyEvent.VK_Y);
        JMenuItem itemAcerca = new JMenuItem("Acerca de");
        itemAcerca.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema de Gestión de Catálogo de Libros\nProyecto Individual — CRUD Swing + Maven + MariaDB",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        menuAyuda.add(itemAcerca);

        menuBar.add(menuArchivo);
        menuBar.add(menuAcciones);
        menuBar.add(menuAyuda);
        return menuBar;
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARIO);
        panel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titulo = new JLabel("Catálogo de Libros");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(BLANCO);

        JLabel subtitulo = new JLabel("Gestión de inventario y ventas");
        subtitulo.setFont(FUENTE_NORMAL);
        subtitulo.setForeground(new Color(0xBBDEFB));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        panel.add(textos, BorderLayout.WEST);
        return panel;
    }

    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xECEFF1));
        panel.setBorder(new EmptyBorder(5, 12, 5, 12));

        lblEstado = new JLabel("Listo.");
        lblEstado.setFont(FUENTE_NORMAL);
        lblEstado.setForeground(TEXTO_GRIS);

        panel.add(lblEstado, BorderLayout.WEST);
        return panel;
    }

    private void setEstado(String mensaje) {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lblEstado.setText(mensaje + "   (" + hora + ")");
    }

    // ---------------------------------------------------------
    //  TAB 1: CATÁLOGO
    // ---------------------------------------------------------
    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(FUENTE_SUBT);
        lblBuscar.setDisplayedMnemonic(KeyEvent.VK_B);

        txtBuscar = new JTextField();
        txtBuscar.setFont(FUENTE_NORMAL);
        txtBuscar.setToolTipText("Escribe para filtrar por título, autor o categoría");
        lblBuscar.setLabelFor(txtBuscar);
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabla(txtBuscar.getText());
            }
        });

        JButton btnNuevoLibro = new JButton("Registrar libro");
        estilizarBotonPrimario(btnNuevoLibro);
        btnNuevoLibro.addActionListener(e -> tabs.setSelectedIndex(1));

        JButton btnRefrescar = new JButton("Refrescar");
        estilizarBotonSecundario(btnRefrescar);
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla();
            cargarResumen();
            setEstado("Listado actualizado.");
        });

        JButton btnVerResumen = new JButton("Ver Resumen");
        estilizarBotonSecundario(btnVerResumen);
        btnVerResumen.addActionListener(e -> mostrarResumenConCondicion());

        JPanel botonesArriba = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botonesArriba.setOpaque(false);
        botonesArriba.add(btnVerResumen);
        botonesArriba.add(btnRefrescar);
        botonesArriba.add(btnNuevoLibro);

        panelBusqueda.add(lblBuscar, BorderLayout.WEST);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
        panelBusqueda.add(botonesArriba, BorderLayout.EAST);

        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Título", "Autor", "Categoría", "Precio", "Existencias", "Año", "Fecha ingreso"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: case 5: case 6: return Integer.class;
                    case 4: return BigDecimal.class;
                    default: return String.class;
                }
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setFont(FUENTE_NORMAL);
        tabla.setRowHeight(28);
        tabla.setSelectionBackground(new Color(0xBBDEFB));
        tabla.setGridColor(new Color(0xE0E0E0));
        tabla.getTableHeader().setFont(FUENTE_SUBT);
        tabla.getTableHeader().setBackground(PRIMARIO);
        tabla.getTableHeader().setForeground(BLANCO);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);

        tabla.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    setText("Q " + ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP));
                } else {
                    setText(value == null ? "" : value.toString());
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        });

        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof Integer && (Integer) value == 0) {
                    setText("Agotado");
                    setForeground(PELIGRO);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setText(value == null ? "" : value.toString());
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }
        });

        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(110);

        // Doble clic abre la ventana emergente de edición
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tabla.getSelectedRow();
                    if (fila != -1) {
                        int filaModelo = tabla.convertRowIndexToModel(fila);
                        abrirDialogoEdicion(filaModelo);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));

        JPanel panelAcciones = new JPanel(new BorderLayout());
        panelAcciones.setOpaque(false);

        lblContador = new JLabel("Cargando...");
        lblContador.setFont(FUENTE_NORMAL);
        lblContador.setForeground(TEXTO_GRIS);

        JLabel lblAyuda = new JLabel("Doble clic en una fila para editar o eliminar");
        lblAyuda.setFont(FUENTE_NORMAL.deriveFont(Font.ITALIC));
        lblAyuda.setForeground(TEXTO_GRIS);

        panelAcciones.add(lblContador, BorderLayout.WEST);
        panelAcciones.add(lblAyuda, BorderLayout.EAST);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        return panel;
    }

    private void filtrarTabla(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            String regex = "(?i)" + java.util.regex.Pattern.quote(texto.trim());
            sorter.setRowFilter(RowFilter.regexFilter(regex, 1, 2, 3));
        }
        actualizarContador();
    }

    private void actualizarContador() {
        int visibles = tabla.getRowCount();
        int total = modeloTabla.getRowCount();
        lblContador.setText("Mostrando " + visibles + " de " + total + " libro(s)");
    }

    // ---------------------------------------------------------
    //  MEJORA 2: Resumen con conteo por condición (recorrido manual)
    // ---------------------------------------------------------
    private void mostrarResumenConCondicion() {
        try {
            List<Libro> libros = dao.listarTodos();

            int total = 0;
            int cumplenCondicion = 0;

            for (Libro libro : libros) {
                total++;
                if (libro.getExistencias() > 5) {   // <-- ajusta la condición si tu examen pide otra
                    cumplenCondicion++;
                }
            }

            String mensaje = "Total de libros registrados: " + total + "\n"
                    + "Libros con más de 5 existencias: " + cumplenCondicion;

            JOptionPane.showMessageDialog(this, mensaje,
                    "Resumen del catálogo", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            mostrarError(this, "No se pudo generar el resumen.", e);
        }
    }

    // ---------------------------------------------------------
    //  TAB 2: REGISTRAR (solo libros nuevos)
    // ---------------------------------------------------------
    private JPanel crearPanelRegistrar() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO);
        contenedor.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel tarjeta = new JPanel(new BorderLayout(0, 15));
        tarjeta.setBackground(BLANCO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(20, 25, 20, 25)));

        JLabel lblTitulo = new JLabel("Registrar libro nuevo");
        lblTitulo.setFont(FUENTE_SUBT);
        lblTitulo.setForeground(PRIMARIO_OSC);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtTituloReg = new JTextField(22);
        txtAutorReg = new JTextField(22);
        txtCategoriaReg = new JTextField(22);
        txtPrecioReg = new JTextField(22);
        txtPrecioReg.setToolTipText("Monto en quetzales, mayor a 0. Ejemplo: 145.00");

        int anioActual = Year.now().getValue();
        spnExistenciasReg = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        spnAnioReg = new JSpinner(new SpinnerNumberModel(anioActual, 1500, anioActual, 1));
        ((JSpinner.DefaultEditor) spnExistenciasReg.getEditor()).getTextField().setColumns(10);
        ((JSpinner.DefaultEditor) spnAnioReg.getEditor()).getTextField().setColumns(10);

        spnFechaIngresoReg = new JSpinner(new SpinnerDateModel());
        spnFechaIngresoReg.setEditor(new JSpinner.DateEditor(spnFechaIngresoReg, "yyyy-MM-dd"));
        spnFechaIngresoReg.setValue(new Date());

        int fila = 0;
        agregarCampo(form, gbc, fila++, "Título:", txtTituloReg, KeyEvent.VK_T);
        agregarCampo(form, gbc, fila++, "Autor:", txtAutorReg, KeyEvent.VK_R);
        agregarCampo(form, gbc, fila++, "Categoría:", txtCategoriaReg, KeyEvent.VK_G);
        agregarCampo(form, gbc, fila++, "Precio (Q):", txtPrecioReg, KeyEvent.VK_P);
        agregarCampoSpinner(form, gbc, fila++, "Existencias:", spnExistenciasReg);
        agregarCampoSpinner(form, gbc, fila++, "Año de publicación:", spnAnioReg);
        agregarCampoSpinner(form, gbc, fila++, "Fecha de ingreso:", spnFechaIngresoReg);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotones.setOpaque(false);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setMnemonic(KeyEvent.VK_G);
        estilizarBotonAccion(btnGuardar, EXITO);
        btnGuardar.addActionListener(e -> registrarNuevoLibro());

        JButton btnLimpiar = new JButton("Limpiar campos");
        estilizarBotonSecundario(btnLimpiar);
        btnLimpiar.addActionListener(e -> limpiarFormularioRegistro());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnLimpiar);

        JLabel nota = new JLabel("Los campos Título y Autor son obligatorios.");
        nota.setFont(FUENTE_NORMAL.deriveFont(Font.ITALIC));
        nota.setForeground(TEXTO_GRIS);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(panelBotones, BorderLayout.NORTH);
        pie.add(nota, BorderLayout.SOUTH);

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(form, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);

        contenedor.add(tarjeta, BorderLayout.NORTH);
        return contenedor;
    }

    private void limpiarFormularioRegistro() {
        txtTituloReg.setText("");
        txtAutorReg.setText("");
        txtCategoriaReg.setText("");
        txtPrecioReg.setText("");
        spnExistenciasReg.setValue(0);
        spnAnioReg.setValue(Year.now().getValue());
        spnFechaIngresoReg.setValue(new Date());
        marcarValido(txtTituloReg);
        marcarValido(txtAutorReg);
        marcarValido(txtPrecioReg);
        txtTituloReg.requestFocusInWindow();
    }

    private void registrarNuevoLibro() {
        Libro libro = validarFormulario(this, txtTituloReg, txtAutorReg, txtCategoriaReg,
                txtPrecioReg, spnExistenciasReg, spnAnioReg, spnFechaIngresoReg, null);
        if (libro == null) return;

        try {
            dao.crear(libro);
            setEstado("Libro \"" + libro.getTitulo() + "\" registrado correctamente.");
            limpiarFormularioRegistro();
            cargarTabla();
            cargarResumen();
            tabs.setSelectedIndex(0);
        } catch (SQLException e) {
            mostrarError(this, "No se pudo registrar el libro.", e);
        }
    }

    // ---------------------------------------------------------
    //  VENTANA EMERGENTE DE EDICIÓN (doble clic en la tabla)
    // ---------------------------------------------------------
    private void abrirDialogoEdicion(int filaModelo) {
        int id = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String tituloActual = String.valueOf(modeloTabla.getValueAt(filaModelo, 1));

        JDialog dialogo = new JDialog(this, "Editar libro — " + tituloActual, true);
        dialogo.setSize(480, 460);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());
        dialogo.getContentPane().setBackground(FONDO);

        JPanel contenido = new JPanel(new BorderLayout(0, 15));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel encabezadoDialogo = new JLabel("Editando libro #" + id);
        encabezadoDialogo.setFont(FUENTE_SUBT);
        encabezadoDialogo.setForeground(PRIMARIO_OSC);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtTitulo = new JTextField(18);
        JTextField txtAutor = new JTextField(18);
        JTextField txtCategoria = new JTextField(18);
        JTextField txtPrecio = new JTextField(18);
        int anioActual = Year.now().getValue();
        JSpinner spnExistencias = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        JSpinner spnAnio = new JSpinner(new SpinnerNumberModel(anioActual, 1500, anioActual, 1));

        JSpinner spnFechaIngreso = new JSpinner(new SpinnerDateModel());
        spnFechaIngreso.setEditor(new JSpinner.DateEditor(spnFechaIngreso, "yyyy-MM-dd"));

        txtTitulo.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 1)));
        txtAutor.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 2)));
        txtCategoria.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 3)));
        txtPrecio.setText(modeloTabla.getValueAt(filaModelo, 4).toString());
        spnExistencias.setValue(modeloTabla.getValueAt(filaModelo, 5));
        spnAnio.setValue(modeloTabla.getValueAt(filaModelo, 6));

        LocalDate fechaExistente = LocalDate.parse(String.valueOf(modeloTabla.getValueAt(filaModelo, 7)));
        Date fechaComoDate = Date.from(fechaExistente.atStartOfDay(ZoneId.systemDefault()).toInstant());
        spnFechaIngreso.setValue(fechaComoDate);

        int fila = 0;
        agregarCampo(form, gbc, fila++, "Título:", txtTitulo, KeyEvent.VK_T);
        agregarCampo(form, gbc, fila++, "Autor:", txtAutor, KeyEvent.VK_R);
        agregarCampo(form, gbc, fila++, "Categoría:", txtCategoria, KeyEvent.VK_G);
        agregarCampo(form, gbc, fila++, "Precio (Q):", txtPrecio, KeyEvent.VK_P);
        agregarCampoSpinner(form, gbc, fila++, "Existencias:", spnExistencias);
        agregarCampoSpinner(form, gbc, fila++, "Año de publicación:", spnAnio);
        agregarCampoSpinner(form, gbc, fila++, "Fecha de ingreso:", spnFechaIngreso);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.setOpaque(false);

        JButton btnActualizar = new JButton("Actualizar");
        estilizarBotonAccion(btnActualizar, PRIMARIO);
        btnActualizar.addActionListener(e -> {
            Libro libro = validarFormulario(dialogo, txtTitulo, txtAutor, txtCategoria,
                    txtPrecio, spnExistencias, spnAnio, spnFechaIngreso, id);
            if (libro == null) return;

            try {
                dao.actualizar(libro);
                setEstado("Libro \"" + libro.getTitulo() + "\" actualizado correctamente.");
                cargarTabla();
                cargarResumen();
                dialogo.dispose();
            } catch (SQLException ex) {
                mostrarError(dialogo, "No se pudo actualizar el libro.", ex);
            }
        });

        JButton btnEliminar = new JButton("Eliminar");
        estilizarBotonAccion(btnEliminar, PELIGRO);
        btnEliminar.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(dialogo,
                    "¿Confirma que desea eliminar \"" + txtTitulo.getText() + "\"? Esta acción no se puede deshacer.",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    dao.eliminar(id);
                    setEstado("Libro \"" + txtTitulo.getText() + "\" eliminado.");
                    cargarTabla();
                    cargarResumen();
                    dialogo.dispose();
                } catch (SQLException ex) {
                    mostrarError(dialogo, "No se pudo eliminar el libro.", ex);
                }
            }
        });

        JButton btnCancelar = new JButton("Cancelar");
        estilizarBotonSecundario(btnCancelar);
        btnCancelar.addActionListener(e -> dialogo.dispose());

        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCancelar);

        contenido.add(encabezadoDialogo, BorderLayout.NORTH);
        contenido.add(form, BorderLayout.CENTER);
        contenido.add(panelBotones, BorderLayout.SOUTH);

        dialogo.add(contenido, BorderLayout.CENTER);
        dialogo.setVisible(true);
    }

    // ---------------------------------------------------------
    //  Campos de formulario reutilizables
    // ---------------------------------------------------------
    private void agregarCampo(JPanel form, GridBagConstraints gbc, int fila, String etiqueta,
                               JTextField campo, int mnemonic) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(FUENTE_NORMAL);
        lbl.setDisplayedMnemonic(mnemonic);
        lbl.setLabelFor(campo);
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        campo.setFont(FUENTE_NORMAL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB0BEC5)),
                new EmptyBorder(5, 8, 5, 8)));
        form.add(campo, gbc);
    }

    private void agregarCampoSpinner(JPanel form, GridBagConstraints gbc, int fila, String etiqueta, JSpinner spinner) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(FUENTE_NORMAL);
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        spinner.setFont(FUENTE_NORMAL);
        form.add(spinner, gbc);
    }

    private void marcarValido(JTextField campo) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB0BEC5)),
                new EmptyBorder(5, 8, 5, 8)));
    }

    private void marcarInvalido(JTextField campo) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_ERROR, 2),
                new EmptyBorder(4, 7, 4, 7)));
        campo.requestFocusInWindow();
    }

    // ---------------------------------------------------------
    //  TAB 3: RESUMEN
    // ---------------------------------------------------------
    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        lblTotalLibros = new JLabel("0", SwingConstants.LEFT);
        lblTotalEjemplares = new JLabel("0", SwingConstants.LEFT);
        lblValorInventario = new JLabel("Q 0.00", SwingConstants.LEFT);
        lblAgotados = new JLabel("0", SwingConstants.LEFT);

        panel.add(crearTarjetaResumen("Total de títulos registrados", lblTotalLibros, PRIMARIO));
        panel.add(crearTarjetaResumen("Ejemplares en existencia", lblTotalEjemplares, EXITO));
        panel.add(crearTarjetaResumen("Valor total del inventario", lblValorInventario, ADVERTENCIA));
        panel.add(crearTarjetaResumen("Títulos agotados", lblAgotados, PELIGRO));

        return panel;
    }

    private JPanel crearTarjetaResumen(String titulo, JLabel valor, Color color) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
        tarjeta.setBackground(BLANCO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, color),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FUENTE_SUBT);
        lblTitulo.setForeground(TEXTO_GRIS);

        valor.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valor.setForeground(color);

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        return tarjeta;
    }

    private void cargarResumen() {
        try {
            List<Libro> libros = dao.listarTodos();
            int totalLibros = libros.size();
            int totalEjemplares = 0;
            BigDecimal valorTotal = BigDecimal.ZERO;
            int agotados = 0;

            for (Libro l : libros) {
                totalEjemplares += l.getExistencias();
                valorTotal = valorTotal.add(l.getPrecio().multiply(BigDecimal.valueOf(l.getExistencias())));
                if (l.getExistencias() == 0) agotados++;
            }

            lblTotalLibros.setText(String.valueOf(totalLibros));
            lblTotalEjemplares.setText(String.valueOf(totalEjemplares));
            lblValorInventario.setText("Q " + valorTotal.setScale(2, RoundingMode.HALF_UP));
            lblAgotados.setText(String.valueOf(agotados));
        } catch (SQLException e) {
            mostrarError(this, "No se pudo calcular el resumen.", e);
        }
    }

    // =========================================================
    //  LÓGICA DE DATOS
    // =========================================================
    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0);
            List<Libro> libros = dao.listarTodos();
            for (Libro l : libros) {
                modeloTabla.addRow(new Object[]{
                        l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria(),
                        l.getPrecio(), l.getExistencias(), l.getAnioPublicacion(),
                        l.getFechaIngreso().toString()
                });
            }
            actualizarContador();
        } catch (SQLException e) {
            mostrarError(this, "No se pudo cargar el listado de libros.", e);
        }
    }

    /**
     * Valida los campos de un formulario (registro o edición) y devuelve un Libro
     * si todo es correcto, o null si hay algún error (mostrando el mensaje respectivo).
     * @param parent  ventana padre para centrar los mensajes (this o el JDialog de edición)
     * @param idExistente  null si es un libro nuevo, o el id si se está editando
     */
    private Libro validarFormulario(Component parent, JTextField txtTitulo, JTextField txtAutor,
                                     JTextField txtCategoria, JTextField txtPrecio,
                                     JSpinner spnExistencias, JSpinner spnAnio,
                                     JSpinner spnFechaIngreso, Integer idExistente) {
        marcarValido(txtTitulo);
        marcarValido(txtAutor);
        marcarValido(txtPrecio);

        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        String categoria = txtCategoria.getText().trim();

        if (titulo.isEmpty()) {
            marcarInvalido(txtTitulo);
            JOptionPane.showMessageDialog(parent, "El título es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (autor.isEmpty()) {
            marcarInvalido(txtAutor);
            JOptionPane.showMessageDialog(parent, "El autor es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                marcarInvalido(txtPrecio);
                JOptionPane.showMessageDialog(parent, "El precio debe ser mayor a cero.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } catch (NumberFormatException ex) {
            marcarInvalido(txtPrecio);
            JOptionPane.showMessageDialog(parent, "El precio debe ser un número válido, por ejemplo 145.00.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int existencias = (Integer) spnExistencias.getValue();
        int anio = (Integer) spnAnio.getValue();

        Date fechaSeleccionada = (Date) spnFechaIngreso.getValue();
        LocalDate fechaIngreso = fechaSeleccionada.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Libro libro = new Libro(titulo, autor, categoria, precio, existencias, anio, fechaIngreso);
        if (idExistente != null) {
            libro.setId(idExistente);
        }
        return libro;
    }

    private void mostrarError(Component parent, String mensajeUsuario, SQLException e) {
        System.err.println("Error SQL: " + e.getMessage());
        setEstado("Ocurrió un error. Revise la consola para más detalles.");
        JOptionPane.showMessageDialog(parent, mensajeUsuario,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================
    //  UTILIDADES DE ESTILO
    // =========================================================
    private void estilizarBotonSecundario(JButton boton) {
        boton.setBackground(new Color(0xECEFF1));
        boton.setForeground(new Color(0x37474F));
        boton.setFont(FUENTE_NORMAL);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(6, 12, 6, 12)));
    }

    private void estilizarBotonPrimario(JButton boton) {
        boton.setBackground(PRIMARIO);
        boton.setForeground(BLANCO);
        boton.setFont(FUENTE_SUBT);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(7, 14, 7, 14));
    }

    private void estilizarBotonAccion(JButton boton, Color color) {
        boton.setBackground(color);
        boton.setForeground(BLANCO);
        boton.setFont(FUENTE_SUBT);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(8, 16, 8, 16));
    }
}