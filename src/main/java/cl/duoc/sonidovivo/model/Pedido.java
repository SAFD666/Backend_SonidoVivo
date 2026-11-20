package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Tabla "pedido": datos del pedido del frontend (fecha, cliente, dirección, teléfono, total e items).
// Estado, tipo de entrega y método de pago son claves foráneas a sus tablas del DER.
@Entity
@Table(name = "pedido", uniqueConstraints = @UniqueConstraint(name = "uk_pedido_codigo", columnNames = "codigo"))
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo visible (ej. PED-1001) asignado tras insertar como 1000 + id.
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pedido_usuario"))
    private Usuario usuario;

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String clienteNombre;

    @Column(name = "cliente_correo", nullable = false, length = 120)
    private String clienteCorreo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 200)
    private String direccion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tipo_entrega", nullable = false, foreignKey = @ForeignKey(name = "fk_pedido_tipo_entrega"))
    private TipoEntrega tipoEntrega;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_metodo_pago", nullable = false, foreignKey = @ForeignKey(name = "fk_pedido_metodo_pago"))
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_estado", nullable = false, foreignKey = @ForeignKey(name = "fk_pedido_estado"))
    private Estado estado;

    @Column(nullable = false)
    private int total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    public void agregarDetalle(DetallePedido d) {
        d.setPedido(this);
        detalles.add(d);
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteCorreo() { return clienteCorreo; }
    public void setClienteCorreo(String clienteCorreo) { this.clienteCorreo = clienteCorreo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    // Hacia afuera se siguen entregando como texto, igual que antes (el frontend no nota el cambio)
    public String getTipoEntrega() { return tipoEntrega.getNombre(); }
    public void setTipoEntrega(TipoEntrega tipoEntrega) { this.tipoEntrega = tipoEntrega; }
    public String getMetodoPago() { return metodoPago.getNombre(); }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    // El estado se entrega como el enum de siempre, así las reglas de PedidoService no cambian
    public EstadoPedido getEstado() { return EstadoPedido.desdeTexto(estado.getNombre()); }
    public void setEstado(Estado estado) { this.estado = estado; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public List<DetallePedido> getDetalles() { return detalles; }
}