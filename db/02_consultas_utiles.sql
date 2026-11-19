-- Consultas para mostrar en la presentación que los datos persisten en PostgreSQL.
-- Entrar desde la EC2:  psql -h ENDPOINT_DEL_RDS -U postgres -d sonido_vivo      (dentro de psql: \dt lista las tablas, \q sale)

-- Usuarios con su rol (la contraseña está guardada como hash BCrypt $2a$12$...)
SELECT u.id, u.nombre, u.correo, r.nombre AS rol, u.activo, LEFT(u.password_hash, 20) AS hash_inicio
FROM usuario u JOIN rol r ON r.id = u.rol_id
ORDER BY u.id;

-- Cantidad de usuarios por rol
SELECT r.nombre AS rol, COUNT(u.id) AS usuarios
FROM rol r LEFT JOIN usuario u ON u.rol_id = r.id
GROUP BY r.nombre
ORDER BY r.nombre;

-- Productos por categoría, con stock total
SELECT c.nombre AS categoria, COUNT(p.id) AS productos, SUM(p.stock) AS unidades
FROM categoria c LEFT JOIN producto p ON p.categoria_id = c.id AND p.activo
GROUP BY c.nombre
ORDER BY c.nombre;

-- Productos con stock crítico (el frontend usa 3 o menos)
SELECT codigo, nombre, stock FROM producto WHERE activo AND stock <= 3 ORDER BY stock, codigo;

-- Pedidos con su cliente y total
SELECT p.codigo, p.fecha, u.correo, p.tipo_entrega, p.estado, p.total
FROM pedido p JOIN usuario u ON u.id = p.usuario_id
ORDER BY p.id DESC;

-- Detalle de un pedido
SELECT d.codigo, d.nombre, d.cantidad, d.precio_unitario, d.subtotal
FROM detalle_pedido d JOIN pedido p ON p.id = d.pedido_id
WHERE p.codigo = 'PED-1001';
