-- Eliminar tablas existentes si es necesario para reiniciar limpio (Opcional, pero recomendado para desarrollo)
DROP TABLE IF EXISTS noticia_etiquetas CASCADE;
DROP TABLE IF EXISTS etiquetas CASCADE;
DROP TABLE IF EXISTS comentarios CASCADE;
DROP TABLE IF EXISTS noticias_eliminadas CASCADE;
DROP TABLE IF EXISTS noticias CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS roles CASCADE;


-- Tabla de Roles
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);



-- Tabla de Categorías (con soporte para subcategorías)
CREATE TABLE categorias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT,
    color VARCHAR(20),
    parent_id INTEGER REFERENCES categorias(id) ON DELETE SET NULL
);

-- Tabla de Noticias
CREATE TABLE noticias (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    subtitulo VARCHAR(255),
    contenido TEXT NOT NULL,
    imagen_url VARCHAR(255),
    autor_id INTEGER REFERENCES usuarios(id),
    categoria_id INTEGER REFERENCES categorias(id),
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    visitas INTEGER DEFAULT 0,
    destacada BOOLEAN DEFAULT FALSE,
    es_aportacion_usuario BOOLEAN DEFAULT FALSE,
    likes INTEGER DEFAULT 0,
    dislikes INTEGER DEFAULT 0,
    comentarios_count INTEGER DEFAULT 0
);

-- Tabla de Comentarios
CREATE TABLE comentarios (
    id SERIAL PRIMARY KEY,
    contenido TEXT NOT NULL,
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    noticia_id INTEGER REFERENCES noticias(id) ON DELETE CASCADE,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Denuncias (Asegurar cascada)
DROP TABLE IF EXISTS denuncias CASCADE;
CREATE TABLE denuncias (
    id SERIAL PRIMARY KEY,
    noticia_id INTEGER REFERENCES noticias(id) ON DELETE CASCADE,
    comentario_id INTEGER REFERENCES comentarios(id) ON DELETE CASCADE,
    denunciante_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    motivo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Votos (Asegurar cascada)
DROP TABLE IF EXISTS votos CASCADE;
CREATE TABLE votos (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    noticia_id INTEGER REFERENCES noticias(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Votos Comentarios (Asegurar cascada)
DROP TABLE IF EXISTS votos_comentarios CASCADE;
CREATE TABLE votos_comentarios (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
    comentario_id INTEGER REFERENCES comentarios(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Etiquetas
CREATE TABLE etiquetas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla Intermedia Noticia-Etiquetas
CREATE TABLE noticia_etiquetas (
    noticia_id INTEGER REFERENCES noticias(id) ON DELETE CASCADE,
    etiqueta_id INTEGER REFERENCES etiquetas(id) ON DELETE CASCADE,
    PRIMARY KEY (noticia_id, etiqueta_id)
);

-- Tabla de Noticias Eliminadas (Archivo completo)
CREATE TABLE noticias_eliminadas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    subtitulo VARCHAR(255),
    contenido TEXT NOT NULL,
    imagen_url TEXT,
    autor_original_id INTEGER,
    autor_original_nombre VARCHAR(255),
    categoria_id INTEGER,
    categoria_nombre VARCHAR(100),
    categoria_color VARCHAR(20),
    fecha_publicacion TIMESTAMP,
    likes INTEGER DEFAULT 0,
    dislikes INTEGER DEFAULT 0,
    visitas INTEGER DEFAULT 0,
    comentarios_count INTEGER DEFAULT 0,
    motivo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    eliminado_por_id INTEGER,
    eliminado_por_nombre VARCHAR(255),
    rol_eliminador VARCHAR(50),
    fecha_eliminacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    es_aportacion_usuario BOOLEAN DEFAULT FALSE
);


-- INSERTS INICIALES

-- 1. Roles (IDs específicos: 1=OWNER, 2=ADMIN, 3=TRABAJADOR, 4=USER)
INSERT INTO roles (id, nombre) VALUES 
(1, 'OWNER'),
(2, 'ADMIN'),
(3, 'TRABAJADOR'),
(4, 'USER');

-- 3. Categorías y Subcategorías
INSERT INTO categorias (nombre, descripcion, color, parent_id) VALUES 
('Tecnología', 'Novedades del mundo tech', '#3B82F6', NULL),
('Deportes', 'Todo sobre deportes', '#EF4444', NULL),
('Politica', 'Actualidad política', '#F59E0B', NULL),
('Cultura', 'Arte, cine y música', '#8B5CF6', NULL);

-- Subcategorías de Tecnología
INSERT INTO categorias (nombre, descripcion, color, parent_id) VALUES 
('Moviles', 'Smartphones y tablets', '#60A5FA', 1),
('IA', 'Inteligencia Artificial', '#60A5FA', 1),
('Hardware', 'Componentes y PC', '#60A5FA', 1);

-- Subcategorías de Deportes
INSERT INTO categorias (nombre, descripcion, color, parent_id) VALUES 
('Futbol', 'Liga y Champions', '#F87171', 2),
('Baloncesto', 'NBA y ACB', '#F87171', 2);

