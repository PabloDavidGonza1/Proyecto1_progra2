package edu.umg.programacion2.proyecto.ui;

import edu.umg.programacion2.proyecto.ui.VentanaPrincipal;

import javax.swing.*;

public class MainUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}