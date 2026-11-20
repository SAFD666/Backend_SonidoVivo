package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.DireccionRequest;
import cl.duoc.sonidovivo.dto.RegionResponse;
import cl.duoc.sonidovivo.dto.RegionResponse.ComunaResponse;
import cl.duoc.sonidovivo.exception.SolicitudInvalidaException;
import cl.duoc.sonidovivo.model.Comuna;
import cl.duoc.sonidovivo.model.Direccion;
import cl.duoc.sonidovivo.model.Region;
import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.ComunaRepository;
import cl.duoc.sonidovivo.repository.DireccionRepository;
import cl.duoc.sonidovivo.repository.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

// Regiones, comunas y direcciones (tablas region, comuna y direccion del DER).
@Service
public class UbicacionService {

    // Las 16 regiones, de norte a sur, con comunas principales (se pueden agregar más después).
    private static final Map<String, List<String>> DATOS = new LinkedHashMap<>();
    static {
        DATOS.put("Arica y Parinacota", List.of("Arica"));
        DATOS.put("Tarapacá", List.of("Iquique", "Alto Hospicio"));
        DATOS.put("Antofagasta", List.of("Antofagasta", "Calama"));
        DATOS.put("Atacama", List.of("Copiapó", "Vallenar"));
        DATOS.put("Coquimbo", List.of("La Serena", "Coquimbo", "Ovalle"));
        DATOS.put("Valparaíso", List.of("Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "Concón",
                "Quillota", "San Antonio", "Los Andes"));
        DATOS.put("Metropolitana de Santiago", List.of("Santiago", "Providencia", "Las Condes", "Ñuñoa",
                "Maipú", "Puente Alto", "La Florida", "San Bernardo"));
        DATOS.put("Libertador General Bernardo O'Higgins", List.of("Rancagua", "San Fernando"));
        DATOS.put("Maule", List.of("Talca", "Curicó", "Linares"));
        DATOS.put("Ñuble", List.of("Chillán"));
        DATOS.put("Biobío", List.of("Concepción", "Talcahuano", "Los Ángeles", "San Pedro de la Paz"));
        DATOS.put("La Araucanía", List.of("Temuco", "Villarrica"));
        DATOS.put("Los Ríos", List.of("Valdivia"));
        DATOS.put("Los Lagos", List.of("Puerto Montt", "Osorno", "Castro"));
        DATOS.put("Aysén del General Carlos Ibáñez del Campo", List.of("Coyhaique"));
        DATOS.put("Magallanes y de la Antártica Chilena", List.of("Punta Arenas"));
    }

    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;
    private final DireccionRepository direccionRepository;

    public UbicacionService(RegionRepository regionRepository, ComunaRepository comunaRepository,
                            DireccionRepository direccionRepository) {
        this.regionRepository = regionRepository;
        this.comunaRepository = comunaRepository;
        this.direccionRepository = direccionRepository;
    }

    // Datos iniciales: solo agrega lo que falta (no duplica al reiniciar).
    @Transactional
    public void cargarSiFalta() {
        DATOS.forEach((nombreRegion, comunas) -> {
            Region region = regionRepository.findByNombre(nombreRegion)
                    .orElseGet(() -> regionRepository.save(new Region(nombreRegion)));
            for (String nombreComuna : comunas) {
                if (!comunaRepository.existsByNombreAndRegionId(nombreComuna, region.getId())) {
                    comunaRepository.save(new Comuna(nombreComuna, region));
                }
            }
        });
    }

    // Regiones de norte a sur, cada una con sus comunas en orden alfabético.
    @Transactional(readOnly = true)
    public List<RegionResponse> listar() {
        Map<Integer, List<ComunaResponse>> porRegion = comunaRepository.findAllByOrderByNombreAsc().stream()
                .collect(Collectors.groupingBy(c -> c.getRegion().getId(),
                        Collectors.mapping(c -> new ComunaResponse(c.getId(), c.getNombre()), Collectors.toList())));
        return regionRepository.findAllByOrderByIdAsc().stream()
                .map(r -> new RegionResponse(r.getId(), r.getNombre(), porRegion.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    // Checkout: reutiliza la dirección si el usuario ya la tenía; si no, la crea.
    @Transactional
    public Direccion obtenerOCrearDireccion(Usuario usuario, DireccionRequest req) {
        Comuna comuna = comunaRepository.findById(req.idComuna())
                .orElseThrow(() -> new SolicitudInvalidaException("La comuna " + req.idComuna() + " no existe"));
        String calle = req.calle().trim();
        String numero = req.numero().trim();
        String depto = req.depto() == null || req.depto().isBlank() ? null : req.depto().trim();
        return direccionRepository
                .findFirstByUsuarioIdAndCalleIgnoreCaseAndNumeroAndComunaId(usuario.getId(), calle, numero, comuna.getId())
                .orElseGet(() -> direccionRepository.save(new Direccion(calle, numero, depto, comuna, usuario)));
    }

    // Solo para los pedidos de ejemplo: convierte "O'Higgins 820, Concepción" en una dirección.
    @Transactional
    public Direccion direccionDesdeTexto(Usuario usuario, String texto) {
        int coma = texto.lastIndexOf(", ");
        String calleNumero = texto.substring(0, coma).trim();
        String nombreComuna = texto.substring(coma + 2).trim();
        int espacio = calleNumero.lastIndexOf(' ');
        Comuna comuna = comunaRepository.findByNombreIgnoreCase(nombreComuna)
                .orElseThrow(() -> new IllegalStateException("Comuna inexistente en seed: " + nombreComuna));
        DireccionRequest req = new DireccionRequest(
                calleNumero.substring(0, espacio), calleNumero.substring(espacio + 1), null, comuna.getId());
        return obtenerOCrearDireccion(usuario, req);
    }
}