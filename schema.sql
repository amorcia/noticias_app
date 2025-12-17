-- Eliminar tablas existentes si es necesario para reiniciar limpio (Opcional, pero recomendado para desarrollo)
DROP TABLE IF EXISTS noticia_etiquetas CASCADE;
DROP TABLE IF EXISTS etiquetas CASCADE;
DROP TABLE IF EXISTS comentarios CASCADE;
DROP TABLE IF EXISTS noticias CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Tabla de Roles
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla de Usuarios
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    movil VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    rol_id INTEGER REFERENCES roles(id),
    activo BOOLEAN DEFAULT TRUE,
    token_confirmacion VARCHAR(255),
    token_recuperacion VARCHAR(255),
    fecha_token TIMESTAMP,
    -- Campos para el sistema de Veto/Ban
    vetado BOOLEAN DEFAULT FALSE,
    motivo_veto TEXT,
    fecha_veto TIMESTAMP,
    vetado_hasta TIMESTAMP,
    es_super_admin BOOLEAN DEFAULT FALSE
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
    usuario_id INTEGER REFERENCES usuarios(id),
    noticia_id INTEGER REFERENCES noticias(id) ON DELETE CASCADE,
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

-- INSERTS INICIALES

-- 1. Roles
INSERT INTO roles (nombre) VALUES ('ADMIN'), ('USER');

-- 2. Usuarios (Admin por defecto)
-- Password: 'password' encriptada con BCrypt
INSERT INTO usuarios (nombre_completo, email, password, rol_id, activo) 
VALUES ('Administrador Principal', 'admin@noticias.com', '$2a$10$X/hX.6.1.1.1.1.1.1.1.1', 1, true);

-- Super Admin (antoniowebserver@gmail.com) - Password: 'admin123'
INSERT INTO usuarios (nombre_completo, email, password, rol_id, activo, es_super_admin) 
VALUES ('Antonio Super Admin', 'antoniowebserver@gmail.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1, true, true);

-- 3. Categorías y Subcategorías
INSERT INTO categorias (nombre, descripcion, color, parent_id) VALUES 
('Tecnología', 'Novedades del mundo tech', '#3B82F6', NULL),
('Deportes', 'Todo sobre deportes', '#EF4444', NULL),
('Política', 'Actualidad política', '#F59E0B', NULL),
('Cultura', 'Arte, cine y música', '#8B5CF6', NULL);

-- Subcategorías de Tecnología
INSERT INTO categorias (nombre, descripcion, color, parent_id) VALUES 
('Móviles', 'Smartphones y tablets', '#60A5FA', 1),
('IA', 'Inteligencia Artificial', '#60A5FA', 1),
('Hardware', 'Componentes y PC', '#60A5FA', 1);

-- Subcategorías de Deportes
INSERT INTO categorias (nombre, descripcion, color, parent_id) VALUES 
('Fútbol', 'Liga y Champions', '#F87171', 2),
('Baloncesto', 'NBA y ACB', '#F87171', 2);

-- 4. Noticias de Ejemplo
INSERT INTO noticias (titulo, subtitulo, contenido, autor_id, categoria_id, destacada, imagen_url) VALUES 
('Lanzamiento del nuevo SuperPhone', 'Revolucionará el mercado', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.', 1, 5, true, 'https://source.unsplash.com/random/800x600?tech'),
('La IA domina el mundo', 'Nuevos avances en GPT-5', 'Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.', 1, 6, true, 'https://source.unsplash.com/random/800x600?ai'),
('Final de la Champions', 'Un partido histórico', 'Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.', 1, 8, false, 'https://source.unsplash.com/random/800x600?soccer');
