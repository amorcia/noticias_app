-- Eliminar tablas existentes si es necesario para reiniciar limpio
-- NOTA: No eliminamos 'usuarios' por petición del usuario para preservar sus datos
DROP TABLE IF EXISTS noticia_etiquetas CASCADE;
DROP TABLE IF EXISTS etiquetas CASCADE;
DROP TABLE IF EXISTS denuncias CASCADE;
DROP TABLE IF EXISTS votos_comentarios CASCADE;
DROP TABLE IF EXISTS votos CASCADE;
DROP TABLE IF EXISTS comentarios CASCADE;
DROP TABLE IF EXISTS noticias_eliminadas CASCADE;
DROP TABLE IF EXISTS noticias CASCADE;
DROP TABLE IF EXISTS sanciones CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    role_id SERIAL PRIMARY KEY,
    role_nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla de Categorías
CREATE TABLE IF NOT EXISTS categorias (
    cat_id SERIAL PRIMARY KEY,
    cat_nombre VARCHAR(50) UNIQUE NOT NULL,
    cat_descripcion TEXT,
    cat_color VARCHAR(20),
    cat_parent_id INTEGER REFERENCES categorias(cat_id) ON DELETE SET NULL
);

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    user_id SERIAL PRIMARY KEY,
    user_nombre_completo VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) UNIQUE NOT NULL,
    user_movil VARCHAR(20),
    user_password VARCHAR(255) NOT NULL,
    user_rol_id INTEGER REFERENCES roles(role_id) ON DELETE SET NULL,
    user_activo BOOLEAN DEFAULT TRUE,
    user_codigo_verificacion VARCHAR(255),
    user_token_session VARCHAR(512),
    user_vetado BOOLEAN DEFAULT FALSE,
    user_motivo_veto TEXT,
    user_fecha_veto TIMESTAMP,
    user_vetado_hasta TIMESTAMP,
    user_secret_key_2fa VARCHAR(255),
    user_imagen_url VARCHAR(255),
    user_email_pendiente VARCHAR(255)
);

-- Tabla de Noticias
CREATE TABLE IF NOT EXISTS noticias (
    news_id SERIAL PRIMARY KEY,
    news_titulo VARCHAR(255) NOT NULL,
    news_subtitulo VARCHAR(255),
    news_contenido TEXT NOT NULL,
    news_imagen_url VARCHAR(255),
    news_autor_id INTEGER REFERENCES usuarios(user_id) ON DELETE SET NULL,
    news_categoria_id INTEGER REFERENCES categorias(cat_id) ON DELETE SET NULL,
    news_fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    news_visitas INTEGER DEFAULT 0,
    news_destacada BOOLEAN DEFAULT FALSE,
    news_es_aportacion_usuario BOOLEAN DEFAULT FALSE,
    news_likes INTEGER DEFAULT 0,
    news_dislikes INTEGER DEFAULT 0,
    news_comentarios_count INTEGER DEFAULT 0
);

-- Tabla de Comentarios
CREATE TABLE IF NOT EXISTS comentarios (
    coments_id SERIAL PRIMARY KEY,
    coments_contenido TEXT NOT NULL,
    coments_usuario_id INTEGER REFERENCES usuarios(user_id) ON DELETE CASCADE,
    coments_noticia_id INTEGER REFERENCES noticias(news_id) ON DELETE CASCADE,
    coments_padre_id INTEGER REFERENCES comentarios(coments_id) ON DELETE CASCADE,
    coments_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Denuncias
CREATE TABLE IF NOT EXISTS denuncias (
    denun_id SERIAL PRIMARY KEY,
    denun_noticia_id INTEGER REFERENCES noticias(news_id) ON DELETE CASCADE,
    denun_comentario_id INTEGER REFERENCES comentarios(coments_id) ON DELETE CASCADE,
    denun_denunciante_id INTEGER REFERENCES usuarios(user_id) ON DELETE CASCADE,
    denun_motivo VARCHAR(255) NOT NULL,
    denun_descripcion TEXT,
    denun_estado VARCHAR(50) DEFAULT 'PENDIENTE',
    denun_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Votos (Reacciones)
CREATE TABLE IF NOT EXISTS votos (
    react_id SERIAL PRIMARY KEY,
    react_usuario_id INTEGER REFERENCES usuarios(user_id) ON DELETE CASCADE,
    react_noticia_id INTEGER REFERENCES noticias(news_id) ON DELETE CASCADE,
    react_tipo VARCHAR(20) NOT NULL,
    react_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Votos Comentarios
CREATE TABLE IF NOT EXISTS votos_comentarios (
    react_com_id SERIAL PRIMARY KEY,
    react_com_usuario_id INTEGER REFERENCES usuarios(user_id) ON DELETE CASCADE,
    react_com_comentario_id INTEGER REFERENCES comentarios(coments_id) ON DELETE CASCADE,
    react_com_tipo VARCHAR(20) NOT NULL,
    react_com_fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Etiquetas
CREATE TABLE IF NOT EXISTS etiquetas (
    tag_id SERIAL PRIMARY KEY,
    tag_nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla Intermedia Noticia-Etiquetas
CREATE TABLE IF NOT EXISTS noticia_etiquetas (
    news_tag_noticia_id INTEGER REFERENCES noticias(news_id) ON DELETE CASCADE,
    news_tag_etiqueta_id INTEGER REFERENCES etiquetas(tag_id) ON DELETE CASCADE,
    PRIMARY KEY (news_tag_noticia_id, news_tag_etiqueta_id)
);

-- Tabla de Noticias Eliminadas
CREATE TABLE IF NOT EXISTS noticias_eliminadas (
    news_del_id BIGSERIAL PRIMARY KEY,
    news_del_titulo VARCHAR(255) NOT NULL,
    news_del_subtitulo VARCHAR(255),
    news_del_contenido TEXT NOT NULL,
    news_del_imagen_url TEXT,
    news_del_autor_original_id INTEGER,
    news_del_autor_original_nombre VARCHAR(255),
    news_del_categoria_id INTEGER,
    news_del_categoria_nombre VARCHAR(100),
    news_del_categoria_color VARCHAR(20),
    news_del_fecha_publicacion TIMESTAMP,
    news_del_likes INTEGER DEFAULT 0,
    news_del_dislikes INTEGER DEFAULT 0,
    news_del_visitas INTEGER DEFAULT 0,
    news_del_comentarios_count INTEGER DEFAULT 0,
    news_del_motivo VARCHAR(100) NOT NULL,
    news_del_descripcion TEXT,
    news_del_eliminado_por_id INTEGER,
    news_del_eliminado_por_nombre VARCHAR(255),
    news_del_rol_eliminador VARCHAR(50),
    news_del_fecha_eliminacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    news_del_es_aportacion_usuario BOOLEAN DEFAULT FALSE
);

-- Tabla de Sanciones
CREATE TABLE IF NOT EXISTS sanciones (
    san_id SERIAL PRIMARY KEY,
    san_usuario_id INTEGER REFERENCES usuarios(user_id) ON DELETE CASCADE,
    san_admin_id INTEGER REFERENCES usuarios(user_id) ON DELETE SET NULL,
    san_tipo VARCHAR(20) NOT NULL, -- TEMPORAL, PERMANENTE
    san_estado VARCHAR(20) NOT NULL, -- PENDIENTE, RESUELTO
    san_motivo TEXT,
    san_apelacion TEXT,
    san_resolucion TEXT,
    san_fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    san_fecha_fin TIMESTAMP
);

-- Limpieza de datos (excepto usuarios) y reset de secuencias
TRUNCATE roles, categorias, noticias, comentarios, denuncias, votos, votos_comentarios, etiquetas, noticia_etiquetas, noticias_eliminadas, sanciones RESTART IDENTITY CASCADE;

-- INSERTS INICIALES (Sólo catálogos básicos)

-- 1. Roles
INSERT INTO roles (role_id, role_nombre) VALUES 
(1, 'OWNER'),
(2, 'ADMIN'),
(3, 'TRABAJADOR'),
(4, 'USER')
ON CONFLICT (role_id) DO NOTHING;

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
