package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.*;
import cl.duoc.sonidovivo.exception.SolicitudInvalidaException;
import cl.duoc.sonidovivo.model.EstadoPedido;
import cl.duoc.sonidovivo.security.UsuarioAutenticado;
import cl.duoc.sonidovivo.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "5. Pedidos", description = "Compras. Regla de stock: no se puede pedir más de lo disponible (409). Se identifican por código (PED-1001)")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/opciones")
    @Operation(summary = "Opciones del checkout", description = "Público. Tipos de entrega, métodos de pago y estados válidos.")
    public OpcionesPedidoResponse opciones() {
        return pedidoService.opciones();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Crear pedido", description = "Solo Cliente (el checkout del frontend es solo para clientes). El pedido queda a nombre del token. Descuenta stock.")
    @ApiResponse(responseCode = "404", description = "Producto inexistente", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Stock insuficiente", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PedidoResponse crear(@Valid @RequestBody PedidoRequest req, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return pedidoService.crear(req, yo);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar pedidos", description = "Cliente: los suyos (Mis pedidos). Administrador/Vendedor: todos; filtros ?usuarioId= (historial del usuario) y ?estado=Enviado.")
    public List<PedidoResponse> listar(@RequestParam(required = false) Long usuarioId,
                                       @RequestParam(required = false) String estado,
                                       @AuthenticationPrincipal UsuarioAutenticado yo) {
        EstadoPedido filtro = null;
        if (estado != null && !estado.isBlank()) {
            try {
                filtro = EstadoPedido.desdeTexto(estado); // acepta "Enviado", "En preparación"...
            } catch (IllegalArgumentException e) {
                throw new SolicitudInvalidaException(e.getMessage());
            }
        }
        return pedidoService.listar(yo, usuarioId, filtro);
    }

    @GetMapping("/{codigo}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ver pedido / comprobante", description = "Dueño, Administrador o Vendedor.")
    @ApiResponse(responseCode = "403", description = "Pedido de otra persona", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PedidoResponse obtener(@PathVariable String codigo, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return pedidoService.obtener(codigo, yo);
    }

    @PatchMapping("/{codigo}/estado")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Cambiar estado", description = "Administrador o Vendedor. Cancelar devuelve el stock; un pedido cancelado no puede reabrirse (409).")
    @ApiResponse(responseCode = "409", description = "Transición no permitida", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PedidoResponse cambiarEstado(@PathVariable String codigo, @Valid @RequestBody CambioEstadoRequest req) {
        return pedidoService.cambiarEstado(codigo, req.estado());
    }

    @PatchMapping("/{codigo}/cancelar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancelar mi pedido", description = "Solo el dueño y solo si está En preparación.")
    @ApiResponse(responseCode = "409", description = "Ya no se puede cancelar", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PedidoResponse cancelar(@PathVariable String codigo, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return pedidoService.cancelar(codigo, yo);
    }
}
