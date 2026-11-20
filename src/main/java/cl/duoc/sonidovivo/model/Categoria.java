package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

import java.text.Normalizer;
import java.util.Locale;

// Tabla "categoria" del DER: nombre y slug únicos. El slug se genera solo a partir del nombre.
@Entity
@Table(name = "categoria", uniqueConstraints = {
        @UniqueConstraint(name = "uk_categoria_nombre", columnNames = "nombre"),
        @UniqueConstraint(name = "uk_categoria_slug", columnNames = "slug")
})
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    // Texto para la URL del frontend: /categoria/guitarras-acusticas
    @Column(nullable = false, length = 60)
    private String slug;

    @Column(nullable = false, length = 255)
    private String imagen;

    protected Categoria() { }

    public Categoria(String nombre, String imagen) {
        setNombre(nombre);
        this.imagen = imagen;
    }

    // Misma regla que generarSlug() de src/data/productos.js
    public static String generarSlug(String texto) {
        return Normalizer.normalize(texto.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", "-");
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSlug() { return slug; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    // Al cambiar el nombre, el slug se actualiza solo
    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.slug = generarSlug(nombre);
    }
}