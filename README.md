Sonido Vivo — Backend (API REST)

API REST de la tienda de instrumentos Sonido Vivo: PostgreSQL en Amazon RDS, login con JWT, roles, catálogo y pedidos. Funciona con el frontend React SonidoVivo-main (mismos datos, roles y estados que src/data).

	
Asignatura	DSY1104 – Desarrollo Fullstack II (2026)
Equipo 7	Sebastian Fica (seb.fica@duocuc.cl) · Alonso Valenzuela (alo.valenzuela@duocuc.cl)
Repositorio frontend	pegar enlace
Swagger (EC2)	http://IP_PUBLICA_EC2:8080/swagger-ui.html LA IP_PUBLICA_EC2 se remplaza



Tecnología:

Java 21 · Spring Boot 3.5 · Spring Data JPA · PostgreSQL (RDS) · Spring Security + JWT (HS256, 60 min) · BCrypt (costo 12) · Swagger (springdoc). Se ejecuta en una EC2 Ubuntu 24.04, puerto 8080.


Modelo de datos:

Tablas: rol, usuario, categoria, producto, pedido, detalle_pedido (Hibernate las crea al iniciar; SQL de referencia en db/02_modelo_referencia.sql).


Roles: 

Administrador, Vendedor, Cliente. El registro público siempre crea Cliente.
Contraseñas: solo se guarda el hash BCrypt (usuario.password_hash).
Usuarios y productos se desactivan (activo=false), no se borran.
Pedidos: el servidor calcula precios, total, fecha y estado. Si se pide más que el stock → 409. Al cancelar se devuelve el stock.
Datos iniciales (solo si faltan): 9 categorías, 50 productos, 3 cuentas demo y 2 pedidos de ejemplo.






API:

Rutas bajo /api/v1. Para probar en Swagger: POST /auth/login, copiar accessToken, botón Authorize.

Auth

Rutas: POST /auth/register, POST /auth/login, GET /auth/me

Permiso: Público / autenticado


Usuarios

Rutas: GET, POST /usuarios · GET, PUT, DELETE /usuarios/{id} · PATCH /usuarios/{id}/rol · PATCH /usuarios/{id}/reactivar

Permiso: Administrador (GET y PUT de uno mismo también)


Categorías

Rutas: GET /categorias · POST, PUT /categorias/{id}

Permiso: GET público; escribir: Administrador o Vendedor


Productos

Rutas: GET /productos?categoria=&oferta=&q= · GET /productos/{codigo} · POST, PUT, PATCH .../stock, DELETE

Permiso: GET público; escribir: Administrador o Vendedor


Pedidos

Rutas: POST /pedidos (Cliente) · GET /pedidos · GET /pedidos/{codigo} · PATCH .../estado (Admin/Vendedor) · PATCH .../cancelar (dueño) · GET /pedidos/opciones

Permiso: Cliente ve solo los suyos


Códigos de error: 400 datos inválidos · 401 sin token o credenciales incorrectas · 403 sin permiso · 404 no existe · 409 duplicado o stock insuficiente · 429 demasiados intentos de login.





Cuentas de demostración

admin@sonidovivo.cl, vendedor@sonidovivo.cl y cliente@correo.cl, todas con la contraseña de DEMO_PASSWORD.

Cómo ejecutar

bash
cp .env.example .env     # completar DB_URL, DB_PASSWORD, JWT_SECRET, DEMO_PASSWORD
set -a; source .env; set +a
./mvnw clean package -DskipTests
java -jar target/sonidovivo-backend-0.1.0.jar

Pruebas: importar postman/SonidoVivo.postman_collection.json, poner baseUrl y demoPassword, y ejecutar Run collection. Consultas para la presentación en db/03_consultas_utiles.sql.



Estado del trabajo

Operación	Estado
Tabla de usuarios en PostgreSQL (con hash)	                            En avance
Inicio de sesión con JWT	                                            En avance
Registro de usuario	                                                    En avance
Modificar usuario	                                                    En avance
Listar usuarios	                                                        En avance
Eliminar (desactivar) usuario	                                        En avance
Categorías y productos	                                                En avance
Pedidos con regla de stock	                                            En avance
Frontend consume la API	                                                Pendiente