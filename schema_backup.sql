-- ============================================================================
-- SCHEMA BACKUP - CLEAN VERSION WITH 3-LETTER PREFIXES
-- Generated: 2026-02-03
-- ============================================================================
-- IMPORTANT: This script does NOT drop the 'usuarios' table to preserve user data
-- ============================================================================

-- Drop tables in correct order (respecting FK dependencies)
-- EXCLUDING usuarios table
DROP TABLE IF EXISTS noticia_etiquetas CASCADE;
DROP TABLE IF EXISTS etiquetas CASCADE;
DROP TABLE IF EXISTS votos_comentarios CASCADE;
DROP TABLE IF EXISTS votos CASCADE;
DROP TABLE IF EXISTS denuncias CASCADE;
DROP TABLE IF EXISTS comentarios CASCADE;
DROP TABLE IF EXISTS noticias_eliminadas CASCADE;
DROP TABLE IF EXISTS noticias CASCADE;
DROP TABLE IF EXISTS sanciones CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- ============================================================================
-- TABLE: roles
-- ============================================================================
CREATE TABLE roles (
    rol_id SERIAL PRIMARY KEY,
    rol_nombre VARCHAR(50) UNIQUE NOT NULL,
    rol_descripcion TEXT
);

-- ============================================================================
-- TABLE: usuarios
-- ============================================================================
-- NOTE: This table is NOT dropped to preserve existing user data
-- If creating fresh, uncomment the DROP and CREATE statements below:

/*
DROP TABLE IF EXISTS usuarios CASCADE;
CREATE TABLE usuarios (
    usu_id SERIAL PRIMARY KEY,
    usu_nombre_completo VARCHAR(255) NOT NULL,
    usu_email VARCHAR(255) UNIQUE NOT NULL,
    usu_movil VARCHAR(50),
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
*/

-- ============================================================================
-- TABLE: categorias
-- ============================================================================
CREATE TABLE categorias (
    cat_id SERIAL PRIMARY KEY,
    cat_nombre VARCHAR(50) UNIQUE NOT NULL,
    cat_descripcion TEXT,
    cat_color VARCHAR(20),
    cat_parent_id INTEGER REFERENCES categorias(cat_id) ON DELETE SET NULL
);

-- ============================================================================
-- TABLE: noticias
-- ============================================================================
CREATE TABLE noticias (
    not_id SERIAL PRIMARY KEY,
    not_titulo VARCHAR(255) NOT NULL,
    not_subtitulo VARCHAR(255),
    not_contenido TEXT NOT NULL,
    not_imagen_url TEXT,
    usu_id INTEGER REFERENCES usuarios(usu_id),
    cat_id INTEGER REFERENCES categorias(cat_id),
    not_fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    not_visitas INTEGER DEFAULT 0,
    not_destacada BOOLEAN DEFAULT FALSE,
    not_es_aportacion_usuario BOOLEAN DEFAULT FALSE,
    not_likes INTEGER DEFAULT 0,
    not_dislikes INTEGER DEFAULT 0,
    not_comentarios_count INTEGER DEFAULT 0
);

-- ============================================================================
-- TABLE: comentarios
-- ============================================================================
CREATE TABLE comentarios (
    com_id SERIAL PRIMARY KEY,
    com_contenido TEXT NOT NULL,
    usu_id INTEGER REFERENCES usuarios(usu_id) ON DELETE CASCADE,
    not_id INTEGER REFERENCES noticias(not_id) ON DELETE CASCADE,
    com_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    com_likes INTEGER DEFAULT 0,
    com_dislikes INTEGER DEFAULT 0,
    com_padre_id INTEGER REFERENCES comentarios(com_id) ON DELETE CASCADE
);

-- ============================================================================
-- TABLE: denuncias
-- ============================================================================
CREATE TABLE denuncias (
    den_id SERIAL PRIMARY KEY,
    not_id INTEGER REFERENCES noticias(not_id) ON DELETE CASCADE,
    com_id INTEGER REFERENCES comentarios(com_id) ON DELETE CASCADE,
    denunciante_id INTEGER REFERENCES usuarios(usu_id) ON DELETE CASCADE,
    den_motivo VARCHAR(255) NOT NULL,
    den_descripcion TEXT,
    den_estado VARCHAR(50) DEFAULT 'PENDIENTE',
    den_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- TABLE: votos
-- ============================================================================
CREATE TABLE votos (
    vot_id SERIAL PRIMARY KEY,
    usu_id INTEGER REFERENCES usuarios(usu_id) ON DELETE CASCADE,
    not_id INTEGER REFERENCES noticias(not_id) ON DELETE CASCADE,
    vot_tipo VARCHAR(20) NOT NULL,
    vot_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- TABLE: votos_comentarios
-- ============================================================================
CREATE TABLE votos_comentarios (
    vco_id SERIAL PRIMARY KEY,
    usu_id INTEGER REFERENCES usuarios(usu_id) ON DELETE CASCADE,
    com_id INTEGER REFERENCES comentarios(com_id) ON DELETE CASCADE,
    vco_tipo VARCHAR(20) NOT NULL,
    vco_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- TABLE: etiquetas
-- ============================================================================
CREATE TABLE etiquetas (
    etq_id SERIAL PRIMARY KEY,
    etq_nombre VARCHAR(50) UNIQUE NOT NULL
);

-- ============================================================================
-- TABLE: noticia_etiquetas (Junction Table)
-- ============================================================================
CREATE TABLE noticia_etiquetas (
    not_id INTEGER REFERENCES noticias(not_id) ON DELETE CASCADE,
    etq_id INTEGER REFERENCES etiquetas(etq_id) ON DELETE CASCADE,
    PRIMARY KEY (not_id, etq_id)
);

-- ============================================================================
-- TABLE: noticias_eliminadas
-- ============================================================================
CREATE TABLE noticias_eliminadas (
    nel_id BIGSERIAL PRIMARY KEY,
    nel_titulo VARCHAR(255) NOT NULL,
    nel_subtitulo VARCHAR(255),
    nel_contenido TEXT NOT NULL,
    nel_imagen_url TEXT,
    nel_autor_original_id INTEGER,
    nel_autor_original_nombre VARCHAR(255),
    nel_categoria_id INTEGER,
    nel_categoria_nombre VARCHAR(100),
    nel_categoria_color VARCHAR(20),
    nel_fecha_publicacion_original TIMESTAMP,
    nel_likes INTEGER DEFAULT 0,
    nel_dislikes INTEGER DEFAULT 0,
    nel_visitas INTEGER DEFAULT 0,
    nel_comentarios_count INTEGER DEFAULT 0,
    nel_motivo VARCHAR(100) NOT NULL,
    nel_descripcion TEXT,
    nel_eliminado_por_id INTEGER,
    nel_eliminado_por_nombre VARCHAR(255),
    nel_rol_eliminador VARCHAR(50),
    nel_fecha_eliminacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nel_es_aportacion_usuario BOOLEAN DEFAULT FALSE
);

-- ============================================================================
-- TABLE: sanciones
-- ============================================================================
CREATE TABLE sanciones (
    san_id SERIAL PRIMARY KEY,
    usu_id INTEGER REFERENCES usuarios(usu_id) ON DELETE CASCADE,
    admin_id INTEGER REFERENCES usuarios(usu_id),
    san_tipo VARCHAR(50) NOT NULL,
    san_estado VARCHAR(50) NOT NULL,
    san_motivo TEXT,
    san_apelacion TEXT,
    san_resolucion TEXT,
    san_fecha_inicio TIMESTAMP NOT NULL,
    san_fecha_fin TIMESTAMP
);

-- ============================================================================
-- INITIAL DATA INSERTS
-- ============================================================================

-- Roles (IDs específicos: 1=OWNER, 2=ADMIN, 3=TRABAJADOR, 4=USER)
INSERT INTO roles (rol_id, rol_nombre) VALUES 
(1, 'OWNER'),
(2, 'ADMIN'),
(3, 'TRABAJADOR'),
(4, 'USER');

-- Categorías y Subcategorías
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Tecnología', 'Novedades del mundo tech', '#3B82F6', NULL),
('Deportes', 'Todo sobre deportes', '#EF4444', NULL),
('Politica', 'Actualidad política', '#F59E0B', NULL),
('Cultura', 'Arte, cine y música', '#8B5CF6', NULL);

-- Subcategorías de Tecnología
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Moviles', 'Smartphones y tablets', '#60A5FA', 1),
('IA', 'Inteligencia Artificial', '#60A5FA', 1),
('Hardware', 'Componentes y PC', '#60A5FA', 1);

-- Subcategorías de Deportes
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Futbol', 'Liga y Champions', '#F87171', 2),
('Baloncesto', 'NBA y ACB', '#F87171', 2);
