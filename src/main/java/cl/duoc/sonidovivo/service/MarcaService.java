package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.MarcaResponse;
import cl.duoc.sonidovivo.model.Marca;
import cl.duoc.sonidovivo.repository.MarcaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Transactional(readOnly = true)
    public List<MarcaResponse> listar() {
        return marcaRepository.findAllByOrderByNombreAsc().stream()
                .map(MarcaResponse::desde)
                .toList();
    }

    // Busca la marca por nombre (sin importar mayúsculas); si no existe, la crea.
    // La usan ProductoService y DataInitializer, así el formulario puede seguir enviando la marca como texto.
    @Transactional
    public Marca obtenerOCrear(String nombre) {
        String limpio = nombre.trim();
        return marcaRepository.findByNombreIgnoreCase(limpio)
                .orElseGet(() -> marcaRepository.save(new Marca(limpio)));
    }
}