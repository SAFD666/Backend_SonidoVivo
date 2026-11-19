package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.CategoriaRequest;
import cl.duoc.sonidovivo.dto.CategoriaResponse;
import cl.duoc.sonidovivo.exception.ConflictoException;
import cl.duoc.sonidovivo.exception.RecursoNoEncontradoException;
import cl.duoc.sonidovivo.model.Categoria;
import cl.duoc.sonidovivo.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Reglas de categorias (como guardarCategoria del frontend): nombre unico, usa el logotipo si la imagen va vacia y renombrar actualiza productos automaticamente por referencia de id.
@Service
public class CategoriaService {

    public static final String IMAGEN_POR_DEFECTO = "/img/logotipo.jpg";

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Categoria::getId))
                .map(CategoriaResponse::desde).toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una categoría con ese nombre");
        }
        return CategoriaResponse.desde(categoriaRepository.save(new Categoria(nombre, imagenOPorDefecto(req.imagen()))));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría " + id + " no encontrada"));
        String nombre = req.nombre().trim();
        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(otra -> {
            if (!otra.getId().equals(id)) {
                throw new ConflictoException("Ya existe una categoría con ese nombre");
            }
        });
        c.setNombre(nombre);
        c.setImagen(imagenOPorDefecto(req.imagen()));
        return CategoriaResponse.desde(c);
    }

    private String imagenOPorDefecto(String imagen) {
        return imagen == null || imagen.isBlank() ? IMAGEN_POR_DEFECTO : imagen.trim();
    }
}
