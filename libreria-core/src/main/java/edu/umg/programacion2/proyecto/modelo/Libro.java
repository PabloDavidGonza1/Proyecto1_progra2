package edu.umg.programacion2.proyecto.modelo;

import java.math.BigDecimal;

public class Libro {

    private int id;
    private String titulo;
    private String autor;
    private String categoria;
    private BigDecimal precio;
    private int existencias;
    private int anioPublicacion;

    public Libro() {
    }

    public Libro(String titulo, String autor, String categoria,
                 BigDecimal precio, int existencias, int anioPublicacion) {

        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.precio = precio;
        this.existencias = existencias;
        this.anioPublicacion = anioPublicacion;
    }

    public Libro(int id, String titulo, String autor, String categoria,
                 BigDecimal precio, int existencias, int anioPublicacion) {

        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.precio = precio;
        this.existencias = existencias;
        this.anioPublicacion = anioPublicacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", categoria='" + categoria + '\'' +
                ", precio=" + precio +
                ", existencias=" + existencias +
                ", anioPublicacion=" + anioPublicacion +
                '}';
    }
}