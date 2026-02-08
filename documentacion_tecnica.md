# Documentación Técnica de Código - NoticiasApp

Este documento proporciona una visión detallada de la arquitectura de la API, los servicios de negocio y las entidades de datos del proyecto NoticiasApp.

## Índice de Clases

### Controladores (Endpoints API)
* [AdminControlador](#admincontrolador) - Gestión administrativa y estadísticas.
* [CategoriaControlador](#categoriacontrolador) - Gestión de categorías y jerarquías.
* [InteraccionControlador](#interaccioncontrolador) - Gestión de comentarios y denuncias.
* [NoticiaControlador](#noticiacontrolador) - Gestión integral de noticias.
* [UsuarioControlador](#usuariocontrolador) - Gestión de usuarios, sesiones y seguridad.

### Servicios (Lógica de Negocio)
* [AlmacenamientoServicio](#almacenamientoservicio) - Procesamiento de archivos y Base64.
* [CategoriaServicio](#categoriaservicio) - Lógica de categorías.
* [ComentarioServicio](#comentarioservicio) - Gestión de hilos de comentarios y votos.
* [DenunciaServicio](#denunciaservicio) - Tramitación de quejas.
* [ErrorLogServicio](#errorlogservicio) - Registro persistente de excepciones.
* [LoggerService](#loggerservice) - Logging de acciones y sesiones.
* [ModeracionServicio](#moderacionservicio) - Filtros NSFW y sanciones automáticas.
* [NoticiaServicio](#noticiaservicio) - Motor principal de noticias.
* [UsuarioServicio](#usuarioservicio) - Operaciones de usuario y ciclo de vida.

### Entidades (Modelo de Datos)
* [CategoriaEntidad](#categoriaentidad) - Estructura de categorías.
* [UsuarioEntidad](#usuarioentidad) - Perfil y estado de usuario.
* (Otras entidades del modelo JPA...)

---

## Controladores (API REST)

### AdminControlador
**Paquete:** `com.noticias.api.controladores`  
**Propósito:** Gestionar operaciones de alto nivel para administradores, incluyendo estadísticas del sistema, resolución de sanciones y gestión de usuarios vetados.

| Método | Endpoint | Petición | Descripción | Parámetros | Retorno |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `getStats` | `/admin/stats` | `GET` | Obtiene estadísticas globales (usuarios, sanciones, noticias). | - | `ResponseEntity<Map<String, Object>>` |
| `listarSanciones` | `/admin/sanciones` | `GET` | Lista todas las sanciones registradas. | - | `List<SancionEntidad>` |
| `resolverSancion` | `/admin/sanciones/{id}/resolver` | `POST` | Resuelve una sanción aplicando acciones (vetar, quitar, reducir). | `id` (path), `resolucion`, `accion`, `adminId` | `ResponseEntity<?>` |
| `listarVetados` | `/admin/vetados` | `GET` | Lista usuarios que tienen el estado vetado activo. | - | `List<UsuarioEntidad>` |
| `listarDenuncias` | `/admin/denuncias` | `GET` | Lista todas las denuncias pendientes y resueltas. | - | `List<DenunciaEntidad>` |
| `resolverDenuncia` | `/admin/denuncias/{id}/resolver` | `POST` | Cambia el estado de una denuncia específica. | `id` (path), `estado` | `ResponseEntity<?>` |

### CategoriaControlador
**Paquete:** `com.noticias.api.controladores`  
**Propósito:** Gestión completa de categorías de noticias, incluyendo el manejo de jerarquías (raíz y subcategorías).

| Método | Endpoint | Petición | Descripción | Parámetros | Retorno |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `listarTodas` | `/categorias` | `GET` | Obtiene la lista completa de todas las categorías. | - | `List<CategoriaEntidad>` |
| `listarCategoriasRaiz` | `/categorias/raiz` | `GET` | Obtiene solo las categorías que no tienen padre. | - | `List<CategoriaEntidad>` |
| `listarSubcategorias` | `/categorias/{id}/subcategorias` | `GET` | Obtiene las categorías hijas de un ID dado. | `id` (path) | `List<CategoriaEntidad>` |
| `buscarPorId` | `/categorias/{id}` | `GET` | Busca una categoría por su identificador primario. | `id` (path) | `CategoriaEntidad` |
| `crear` | `/categorias` | `POST` | Crea una nueva categoría base o subcategoría. | `categoria` (body) | `CategoriaEntidad` |
| `actualizar` | `/categorias/{id}` | `PUT` | Actualiza nombre, descripción o color de una categoría. | `id` (path), `categoria` (body) | `CategoriaEntidad` |
| `eliminar` | `/categorias/{id}` | `DELETE` | Elimina lógicamente una categoría del sistema. | `id` (path) | `Void` |

### InteraccionControlador
**Paquete:** `com.noticias.api.controladores`  
**Propósito:** Gestionar la interacción social entre usuarios y noticias (comentarios, votos y denuncias).

| Método | Endpoint | Petición | Descripción | Parámetros | Retorno |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `listarComentarios` | `/interacciones/comentarios/noticia/{id}` | `GET` | Lista comentarios de una noticia (soporta anonimato/usuario). | `noticiaId`, `usuarioId` (opc) | `List<ComentarioEntidad>` |
| `crearComentario` | `/interacciones/comentarios` | `POST` | Crea un comentario o respuesta a otro comentario. | `payload` (Map) | `ResponseEntity<?>` |
| `eliminarComentario` | `/interacciones/comentarios/{id}` | `DELETE` | Elimina un comentario (autor o administrador). | `id` (path), `usuarioId`, `esAdmin` | `ResponseEntity<?>` |
| `votarComentario` | `/interacciones/comentarios/{id}/votar` | `POST` | Registra un Like o Dislike en un comentario. | `id` (path), `like`, `usuarioId` | `Void` |
| `crearDenuncia` | `/interacciones/denuncias` | `POST` | Permite denunciar una noticia o un comentario. | `payload` (Map) | `ResponseEntity<?>` |

### NoticiaControlador
**Paquete:** `com.noticias.api.controladores`  
**Propósito:** Controlador principal para el flujo de noticias: listado, filtrado, publicación con imágenes y moderación.

| Método | Endpoint | Petición | Descripción | Parámetros | Retorno |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `listarTodas` | `/noticias` | `GET` | Lista todas las noticias activas en el sistema. | - | `List<NoticiaDTO>` |
| `listarDestacadas` | `/noticias/destacadas` | `GET` | Lista noticias marcadas como destacadas. | - | `List<NoticiaDTO>` |
| `listarPopulares` | `/noticias/populares` | `GET` | Lista noticias con mayor número de visitas. | - | `List<NoticiaDTO>` |
| `listarPorCategoriaFiltrado` | `/noticias/categoria/{id}/filtrar` | `GET` | Filtro avanzado por categoría, fecha y criterios. | `categoriaId`, `filtro`, `mes`, `anio` | `List<NoticiaDTO>` |
| `publicarNoticiaUsuario` | `/noticias/publicar` | `POST` | Publica noticia con imagen (incluye validación NSFW). | `titulo`, `subtitulo`, `contenido`, `categoriaId`, `autorId`, `file` | `ResponseEntity<?>` |
| `editarNoticia` | `/noticias/{id}/editar` | `POST` | Edita una noticia existente permitiendo cambiar imagen. | `id` (path), `titulo`, `subtitulo`, `file`... | `ResponseEntity<?>` |
| `eliminarConJustificacion` | `/noticias/{id}/eliminar-con-justificacion` | `POST` | Eliminación administrativa con registro de motivo. | `id` (path), `payload` | `ResponseEntity<?>` |
| `votar` | `/noticias/{id}/votar` | `POST` | Registra voto de usuario en la noticia. | `id` (path), `like`, `usuarioId` | `Void` |

### UsuarioControlador
**Paquete:** `com.noticias.api.controladores`  
**Propósito:** Gestionar el ciclo de vida de los usuarios, perfiles, recuperación de cuentas y blindaje del usuario OWNER.

| Método | Endpoint | Petición | Descripción | Parámetros | Retorno |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `listarTodos` | `/usuarios` | `GET` | Obtiene todos los usuarios (uso administrativo). | - | `List<UsuarioEntidad>` |
| `buscarPorEmail` | `/usuarios/email/{email}` | `GET` | Busca un usuario por su correo electrónico. | `email` (path) | `UsuarioEntidad` |
| `crear` | `/usuarios` | `POST` | Registra un nuevo usuario en la base de datos. | `usuario` (body) | `UsuarioEntidad` |
| `confirmarEmail` | `/usuarios/confirmar-email` | `POST` | Verifica la cuenta de usuario mediante token. | `payload` (token) | `ResponseEntity` |
| `recuperarPassword` | `/usuarios/recuperar-password` | `POST` | Inicia el proceso de recuperación de contraseña. | `payload` (email) | `ResponseEntity` |
| `vetarUsuario` | `/usuarios/{id}/vetar` | `POST` | Sanciona a un usuario con un veto temporal o permanente. | `id` (path), `motivo`, `duracion` | `ResponseEntity` |
| `eliminarConJustificacion` | `/usuarios/{id}/eliminar-con-justificacion` | `POST` | Eliminación definitiva con cascada (comentarios y noticias). | `id` (path), `payload` | `ResponseEntity` |
| `subirImagen` | `/usuarios/{id}/imagen` | `POST` | Actualiza la foto de perfil del usuario. | `id` (path), `file` | `Map<String, String>` |

---

## Servicios (Lógica de Negocio)

### AlmacenamientoServicio
**Propósito:** Gestionar el almacenamiento de imágenes. Actualmente las optimiza, redimensiona y las convierte a formato **Base64** para su persistencia en la base de datos (Data URLs).
* **`almacenar(MultipartFile file)`**: Procesa una imagen, valida su tipo y tamaño, y retorna un String Base64.
* **`optimizarImagen(MultipartFile file)`**: Redimensiona la imagen si excede 1920x1080 y aplica compresión JPEG al 85%.

### ErrorLogServicio
**Propósito:** Canalizar todas las excepciones del sistema hacia una tabla de logs en base de datos para depuración post-mortem.
* **`logError(Exception e, ...)`**: Registra de forma asíncrona los detalles del error, incluyendo StackTrace, endpoint y usuario afectado.
* **`generarMensajeAmigable(Exception e)`**: Traduce excepciones técnicas a mensajes comprensibles para el usuario final.

### ModeracionServicio
**Propósito:** Blindar el sistema contra contenido inapropiado y gestionar sanciones automáticas.
* **`esContenidoNSFW(MultipartFile archivo)`**: Conecta con un microservicio externo de IA para detectar imágenes +18 (umbral > 0.8).
* **`vetarUsuarioAutomaticamente(...)`**: Sanciona al usuario por 7 días si intenta subir contenido prohibido.

---

## Entidades (Modelo de Datos)

### CategoriaEntidad
* **ID**: Identificador único.
* **Nombre**: Único, hasta 50 caracteres.
* **Parent**: Referencia a la categoría padre (soporte recursivo).
* **Subcategorías**: Lista de categorías descendientes.

### UsuarioEntidad
* **Email**: Identificador único para login.
* **Rol**: Relación con la entidad Rol (ADMIN, TRABAJADOR, OWNER, USER).
* **Veto**: Flags `vetado` (boolean), `motivoVeto` y `vetadoHasta`.
* **2FA**: Soporte para `secretKey2FA`.
* **ImagenUrl**: Almacena el Data URL de la imagen de perfil.

---
> [!NOTE]
> Esta documentación se ha generado automáticamente basada en el análisis estructural del código fuente y comentarios Javadoc presentes.
