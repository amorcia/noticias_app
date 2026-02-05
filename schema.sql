-- Database Schema for Noticias App

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    rol_id SERIAL PRIMARY KEY,
    rol_nombre VARCHAR(255) NOT NULL UNIQUE,
    rol_descripcion VARCHAR(255)
);

-- 2. Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    usu_id SERIAL PRIMARY KEY,
    usu_nombre_completo VARCHAR(255) NOT NULL,
    usu_email VARCHAR(255) NOT NULL UNIQUE,
    usu_movil VARCHAR(255),
    usu_password VARCHAR(255) NOT NULL,
    rol_id INTEGER REFERENCES roles(rol_id),
    usu_activo BOOLEAN NOT NULL DEFAULT TRUE,
    usu_codigo_verificacion VARCHAR(255),
    usu_token_session VARCHAR(512),
    usu_vetado BOOLEAN NOT NULL DEFAULT FALSE,
    usu_motivo_veto TEXT,
    usu_fecha_veto TIMESTAMP,
    usu_vetado_hasta TIMESTAMP,
    usu_secret_key_2fa VARCHAR(255),
    usu_imagen_url VARCHAR(255),
    usu_email_pendiente VARCHAR(255)
);

-- 3. Usuarios Eliminados
CREATE TABLE IF NOT EXISTS usuarios_eliminados (
    uel_id BIGSERIAL PRIMARY KEY,
    uel_nombre_completo VARCHAR(255),
    uel_email VARCHAR(255),
    uel_rol_nombre VARCHAR(255),
    uel_motivo VARCHAR(255) NOT NULL,
    uel_descripcion TEXT,
    uel_eliminado_por_id INTEGER,
    uel_eliminado_por_nombre VARCHAR(255),
    uel_fecha_eliminacion TIMESTAMP
);

-- 4. Categorias
CREATE TABLE IF NOT EXISTS categorias (
    cat_id SERIAL PRIMARY KEY,
    cat_nombre VARCHAR(50) NOT NULL UNIQUE,
    cat_descripcion TEXT,
    cat_color VARCHAR(20),
    cat_parent_id INTEGER REFERENCES categorias(cat_id)
);

-- 5. Etiquetas
CREATE TABLE IF NOT EXISTS etiquetas (
    etq_id SERIAL PRIMARY KEY,
    etq_nombre VARCHAR(50) NOT NULL UNIQUE
);

-- 6. Noticias
CREATE TABLE IF NOT EXISTS noticias (
    not_id SERIAL PRIMARY KEY,
    not_titulo VARCHAR(255) NOT NULL,
    not_subtitulo VARCHAR(255),
    not_contenido TEXT NOT NULL,
    not_imagen_url TEXT,
    usu_id INTEGER REFERENCES usuarios(usu_id),
    cat_id INTEGER REFERENCES categorias(cat_id),
    not_fecha_publicacion TIMESTAMP,
    not_es_aportacion_usuario BOOLEAN NOT NULL DEFAULT FALSE,
    not_likes INTEGER NOT NULL DEFAULT 0,
    not_dislikes INTEGER NOT NULL DEFAULT 0,
    not_comentarios_count INTEGER NOT NULL DEFAULT 0,
    not_visitas INTEGER NOT NULL DEFAULT 0,
    not_destacada BOOLEAN NOT NULL DEFAULT FALSE
);

-- 7. Noticias Eliminadas
CREATE TABLE IF NOT EXISTS noticias_eliminadas (
    nel_id BIGSERIAL PRIMARY KEY,
    nel_titulo VARCHAR(255) NOT NULL,
    nel_subtitulo VARCHAR(255),
    nel_contenido TEXT NOT NULL,
    nel_imagen_url TEXT,
    nel_autor_original_id INTEGER,
    nel_autor_original_nombre VARCHAR(255),
    nel_categoria_id INTEGER,
    nel_categoria_nombre VARCHAR(255),
    nel_categoria_color VARCHAR(255),
    nel_likes INTEGER DEFAULT 0,
    nel_dislikes INTEGER DEFAULT 0,
    nel_visitas INTEGER DEFAULT 0,
    nel_comentarios_count INTEGER DEFAULT 0,
    nel_fecha_publicacion_original TIMESTAMP,
    nel_motivo VARCHAR(255) NOT NULL,
    nel_descripcion TEXT,
    nel_eliminado_por_id INTEGER,
    nel_eliminado_por_nombre VARCHAR(255),
    nel_rol_eliminador VARCHAR(255),
    nel_fecha_eliminacion TIMESTAMP,
    nel_es_aportacion_usuario BOOLEAN DEFAULT FALSE
);

-- 8. Comentarios
CREATE TABLE IF NOT EXISTS comentarios (
    com_id SERIAL PRIMARY KEY,
    not_id INTEGER NOT NULL REFERENCES noticias(not_id) ON DELETE CASCADE,
    usu_id INTEGER NOT NULL REFERENCES usuarios(usu_id),
    com_contenido TEXT NOT NULL,
    com_likes INTEGER NOT NULL DEFAULT 0,
    com_dislikes INTEGER NOT NULL DEFAULT 0,
    com_padre_id INTEGER REFERENCES comentarios(com_id),
    com_fecha TIMESTAMP
);

-- 9. Votos
CREATE TABLE IF NOT EXISTS votos (
    vot_id SERIAL PRIMARY KEY,
    usu_id INTEGER NOT NULL REFERENCES usuarios(usu_id),
    not_id INTEGER NOT NULL REFERENCES noticias(not_id) ON DELETE CASCADE,
    vot_tipo VARCHAR(255) NOT NULL,
    vot_fecha TIMESTAMP NOT NULL
);

-- 10. Votos Comentarios
CREATE TABLE IF NOT EXISTS votos_comentarios (
    vco_id SERIAL PRIMARY KEY,
    usu_id INTEGER NOT NULL REFERENCES usuarios(usu_id),
    com_id INTEGER NOT NULL REFERENCES comentarios(com_id) ON DELETE CASCADE,
    vco_tipo VARCHAR(255) NOT NULL,
    vco_fecha TIMESTAMP NOT NULL
);

-- 11. Denuncias
CREATE TABLE IF NOT EXISTS denuncias (
    den_id SERIAL PRIMARY KEY,
    not_id INTEGER REFERENCES noticias(not_id) ON DELETE CASCADE,
    com_id INTEGER REFERENCES comentarios(com_id) ON DELETE CASCADE,
    denunciante_id INTEGER NOT NULL REFERENCES usuarios(usu_id),
    den_motivo VARCHAR(255) NOT NULL,
    den_descripcion TEXT,
    den_estado VARCHAR(255) NOT NULL,
    den_fecha TIMESTAMP
);

-- 12. Sanciones
CREATE TABLE IF NOT EXISTS sanciones (
    san_id SERIAL PRIMARY KEY,
    usu_id INTEGER NOT NULL REFERENCES usuarios(usu_id),
    admin_id INTEGER REFERENCES usuarios(usu_id),
    san_tipo VARCHAR(255) NOT NULL,
    san_estado VARCHAR(255) NOT NULL,
    san_motivo TEXT,
    san_apelacion TEXT,
    san_resolucion TEXT,
    san_fecha_inicio TIMESTAMP NOT NULL,
    san_fecha_fin TIMESTAMP
);

-- 13. Error Logs
CREATE TABLE IF NOT EXISTS error_logs (
    el_id BIGSERIAL PRIMARY KEY,
    el_usuario_id INTEGER,
    el_usuario_nombre VARCHAR(255),
    el_tipo_error VARCHAR(50) NOT NULL,
    el_mensaje_tecnico TEXT NOT NULL,
    el_mensaje_usuario TEXT,
    el_stack_trace TEXT,
    el_clase VARCHAR(255),
    el_metodo VARCHAR(255),
    el_paquete VARCHAR(255),
    el_endpoint VARCHAR(500),
    el_metodo_http VARCHAR(10),
    el_timestamp TIMESTAMP,
    el_ultima_actividad TIMESTAMP,
    el_ip_address VARCHAR(45)
);

-- INITIAL DATA

-- 1. Roles
INSERT INTO roles (rol_id, rol_nombre) VALUES 
(1, 'OWNER'),
(2, 'ADMIN'),
(3, 'TRABAJADOR'),
(4, 'USER')
ON CONFLICT (rol_id) DO NOTHING;

-- 2. Categorías
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Tecnología', 'Novedades del mundo tech', '#3B82F6', NULL),   -- id 1
('Deportes', 'Todo sobre deportes', '#EF4444', NULL),        -- id 2
('Politica', 'Actualidad política', '#F59E0B', NULL),        -- id 3
('Cultura', 'Arte, cine y música', '#8B5CF6', NULL);         -- id 4

-- Subcategorías de Tecnología (ID 1)
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Moviles', 'Smartphones y tablets', '#60A5FA', 1),
('IA', 'Inteligencia Artificial', '#60A5FA', 1),
('Hardware', 'Componentes y PC', '#60A5FA', 1);

-- Subcategorías de Deportes (ID 2)
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Futbol', 'Liga y Champions', '#F87171', 2),
('Baloncesto', 'NBA y ACB', '#F87171', 2);
