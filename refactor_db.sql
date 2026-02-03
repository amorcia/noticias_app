-- ============================================================================
-- MIGRATION SCRIPT - Rename columns to 3-letter prefix convention
-- Generated: 2026-02-03
-- ============================================================================
-- This script renames existing columns and adds missing ones
-- Execute AFTER cleanup_duplicates.sql
-- ============================================================================

BEGIN;

-- ============================================================================
-- STEP 1: TABLE roles
-- ============================================================================

-- Rename PK
ALTER TABLE roles RENAME COLUMN id TO rol_id;

-- Rename standard columns
ALTER TABLE roles RENAME COLUMN nombre TO rol_nombre;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS rol_descripcion TEXT;

-- Update FK references in usuarios
ALTER TABLE usuarios RENAME COLUMN rol_id TO rol_id_temp;
ALTER TABLE usuarios ADD COLUMN rol_id INTEGER;
UPDATE usuarios SET rol_id = rol_id_temp;
ALTER TABLE usuarios DROP COLUMN rol_id_temp CASCADE;
ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles(rol_id);

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 2: TABLE categorias
-- ============================================================================

-- Rename PK
ALTER TABLE categorias RENAME COLUMN id TO cat_id;

-- Rename standard columns
ALTER TABLE categorias RENAME COLUMN nombre TO cat_nombre;
ALTER TABLE categorias RENAME COLUMN descripcion TO cat_descripcion;
ALTER TABLE categorias RENAME COLUMN color TO cat_color;
ALTER TABLE categorias RENAME COLUMN parent_id TO cat_parent_id;

-- Update self-referencing FK
ALTER TABLE categorias DROP CONSTRAINT IF EXISTS categorias_parent_id_fkey;
ALTER TABLE categorias ADD CONSTRAINT fk_categorias_parent 
    FOREIGN KEY (cat_parent_id) REFERENCES categorias(cat_id) ON DELETE SET NULL;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 3: TABLE usuarios
-- ============================================================================

-- Rename PK
ALTER TABLE usuarios RENAME COLUMN id TO usu_id;

-- Rename standard columns
ALTER TABLE usuarios RENAME COLUMN nombre_completo TO usu_nombre_completo;
ALTER TABLE usuarios RENAME COLUMN email TO usu_email;
ALTER TABLE usuarios RENAME COLUMN movil TO usu_movil;
ALTER TABLE usuarios RENAME COLUMN password TO usu_password;
ALTER TABLE usuarios RENAME COLUMN activo TO usu_activo;
ALTER TABLE usuarios RENAME COLUMN codigo_verificacion TO usu_codigo_verificacion;
ALTER TABLE usuarios RENAME COLUMN token_session TO usu_token_session;
ALTER TABLE usuarios RENAME COLUMN vetado TO usu_vetado;
ALTER TABLE usuarios RENAME COLUMN motivo_veto TO usu_motivo_veto;
ALTER TABLE usuarios RENAME COLUMN fecha_veto TO usu_fecha_veto;
ALTER TABLE usuarios RENAME COLUMN vetado_hasta TO usu_vetado_hasta;
ALTER TABLE usuarios RENAME COLUMN secret_key_2fa TO usu_secret_key_2fa;
ALTER TABLE usuarios RENAME COLUMN imagen_url TO usu_imagen_url;
ALTER TABLE usuarios RENAME COLUMN email_pendiente TO usu_email_pendiente;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 4: TABLE noticias
-- ============================================================================

-- Rename PK
ALTER TABLE noticias RENAME COLUMN id TO not_id;

-- Rename standard columns
ALTER TABLE noticias RENAME COLUMN titulo TO not_titulo;
ALTER TABLE noticias RENAME COLUMN subtitulo TO not_subtitulo;
ALTER TABLE noticias RENAME COLUMN contenido TO not_contenido;
ALTER TABLE noticias RENAME COLUMN imagen_url TO not_imagen_url;
ALTER TABLE noticias RENAME COLUMN fecha_publicacion TO not_fecha_publicacion;
ALTER TABLE noticias RENAME COLUMN visitas TO not_visitas;
ALTER TABLE noticias RENAME COLUMN destacada TO not_destacada;
ALTER TABLE noticias RENAME COLUMN es_aportacion_usuario TO not_es_aportacion_usuario;
ALTER TABLE noticias RENAME COLUMN likes TO not_likes;
ALTER TABLE noticias RENAME COLUMN dislikes TO not_dislikes;
ALTER TABLE noticias RENAME COLUMN comentarios_count TO not_comentarios_count;

-- Rename FKs
ALTER TABLE noticias RENAME COLUMN autor_id TO usu_id;
ALTER TABLE noticias RENAME COLUMN categoria_id TO cat_id;

-- Update FK constraints
ALTER TABLE noticias DROP CONSTRAINT IF EXISTS noticias_autor_id_fkey;
ALTER TABLE noticias DROP CONSTRAINT IF EXISTS noticias_categoria_id_fkey;
ALTER TABLE noticias ADD CONSTRAINT fk_noticias_usuario FOREIGN KEY (usu_id) REFERENCES usuarios(usu_id);
ALTER TABLE noticias ADD CONSTRAINT fk_noticias_categoria FOREIGN KEY (cat_id) REFERENCES categorias(cat_id);

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 5: TABLE comentarios
-- ============================================================================

-- Rename PK
ALTER TABLE comentarios RENAME COLUMN id TO com_id;

-- Rename standard columns
ALTER TABLE comentarios RENAME COLUMN contenido TO com_contenido;
ALTER TABLE comentarios RENAME COLUMN fecha TO com_fecha;
ALTER TABLE comentarios RENAME COLUMN likes TO com_likes;
ALTER TABLE comentarios RENAME COLUMN dislikes TO com_dislikes;
ALTER TABLE comentarios RENAME COLUMN padre_id TO com_padre_id;

-- Rename FKs
ALTER TABLE comentarios RENAME COLUMN usuario_id TO usu_id;
ALTER TABLE comentarios RENAME COLUMN noticia_id TO not_id;

-- Update FK constraints
ALTER TABLE comentarios DROP CONSTRAINT IF EXISTS comentarios_usuario_id_fkey;
ALTER TABLE comentarios DROP CONSTRAINT IF EXISTS comentarios_noticia_id_fkey;
ALTER TABLE comentarios DROP CONSTRAINT IF EXISTS comentarios_padre_id_fkey;
ALTER TABLE comentarios ADD CONSTRAINT fk_comentarios_usuario FOREIGN KEY (usu_id) REFERENCES usuarios(usu_id) ON DELETE CASCADE;
ALTER TABLE comentarios ADD CONSTRAINT fk_comentarios_noticia FOREIGN KEY (not_id) REFERENCES noticias(not_id) ON DELETE CASCADE;
ALTER TABLE comentarios ADD CONSTRAINT fk_comentarios_padre FOREIGN KEY (com_padre_id) REFERENCES comentarios(com_id) ON DELETE CASCADE;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 6: TABLE denuncias
-- ============================================================================

-- Rename PK
ALTER TABLE denuncias RENAME COLUMN id TO den_id;

-- Rename standard columns
ALTER TABLE denuncias RENAME COLUMN motivo TO den_motivo;
ALTER TABLE denuncias RENAME COLUMN descripcion TO den_descripcion;
ALTER TABLE denuncias RENAME COLUMN estado TO den_estado;
ALTER TABLE denuncias RENAME COLUMN fecha TO den_fecha;

-- Rename FKs
ALTER TABLE denuncias RENAME COLUMN noticia_id TO not_id;
ALTER TABLE denuncias RENAME COLUMN comentario_id TO com_id;
-- denunciante_id stays as is (special case)

-- Update FK constraints
ALTER TABLE denuncias DROP CONSTRAINT IF EXISTS denuncias_noticia_id_fkey;
ALTER TABLE denuncias DROP CONSTRAINT IF EXISTS denuncias_comentario_id_fkey;
ALTER TABLE denuncias DROP CONSTRAINT IF EXISTS denuncias_denunciante_id_fkey;
ALTER TABLE denuncias ADD CONSTRAINT fk_denuncias_noticia FOREIGN KEY (not_id) REFERENCES noticias(not_id) ON DELETE CASCADE;
ALTER TABLE denuncias ADD CONSTRAINT fk_denuncias_comentario FOREIGN KEY (com_id) REFERENCES comentarios(com_id) ON DELETE CASCADE;
ALTER TABLE denuncias ADD CONSTRAINT fk_denuncias_denunciante FOREIGN KEY (denunciante_id) REFERENCES usuarios(usu_id) ON DELETE CASCADE;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 7: TABLE votos
-- ============================================================================

-- Rename PK
ALTER TABLE votos RENAME COLUMN id TO vot_id;

-- Rename standard columns
ALTER TABLE votos RENAME COLUMN tipo TO vot_tipo;
ALTER TABLE votos RENAME COLUMN fecha TO vot_fecha;

-- Rename FKs
ALTER TABLE votos RENAME COLUMN usuario_id TO usu_id;
ALTER TABLE votos RENAME COLUMN noticia_id TO not_id;

-- Update FK constraints
ALTER TABLE votos DROP CONSTRAINT IF EXISTS votos_usuario_id_fkey;
ALTER TABLE votos DROP CONSTRAINT IF EXISTS votos_noticia_id_fkey;
ALTER TABLE votos ADD CONSTRAINT fk_votos_usuario FOREIGN KEY (usu_id) REFERENCES usuarios(usu_id) ON DELETE CASCADE;
ALTER TABLE votos ADD CONSTRAINT fk_votos_noticia FOREIGN KEY (not_id) REFERENCES noticias(not_id) ON DELETE CASCADE;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 8: TABLE votos_comentarios
-- ============================================================================

-- Rename PK
ALTER TABLE votos_comentarios RENAME COLUMN id TO vco_id;

-- Rename standard columns
ALTER TABLE votos_comentarios RENAME COLUMN tipo TO vco_tipo;
ALTER TABLE votos_comentarios RENAME COLUMN fecha TO vco_fecha;

-- Rename FKs
ALTER TABLE votos_comentarios RENAME COLUMN usuario_id TO usu_id;
ALTER TABLE votos_comentarios RENAME COLUMN comentario_id TO com_id;

-- Update FK constraints
ALTER TABLE votos_comentarios DROP CONSTRAINT IF EXISTS votos_comentarios_usuario_id_fkey;
ALTER TABLE votos_comentarios DROP CONSTRAINT IF EXISTS votos_comentarios_comentario_id_fkey;
ALTER TABLE votos_comentarios ADD CONSTRAINT fk_votos_comentarios_usuario FOREIGN KEY (usu_id) REFERENCES usuarios(usu_id) ON DELETE CASCADE;
ALTER TABLE votos_comentarios ADD CONSTRAINT fk_votos_comentarios_comentario FOREIGN KEY (com_id) REFERENCES comentarios(com_id) ON DELETE CASCADE;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 9: TABLE etiquetas
-- ============================================================================

-- Rename PK
ALTER TABLE etiquetas RENAME COLUMN id TO etq_id;

-- Rename standard columns
ALTER TABLE etiquetas RENAME COLUMN nombre TO etq_nombre;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 10: TABLE noticia_etiquetas
-- ============================================================================

-- Rename FKs
ALTER TABLE noticia_etiquetas RENAME COLUMN noticia_id TO not_id;
ALTER TABLE noticia_etiquetas RENAME COLUMN etiqueta_id TO etq_id;

-- Update FK constraints
ALTER TABLE noticia_etiquetas DROP CONSTRAINT IF EXISTS noticia_etiquetas_noticia_id_fkey;
ALTER TABLE noticia_etiquetas DROP CONSTRAINT IF EXISTS noticia_etiquetas_etiqueta_id_fkey;
ALTER TABLE noticia_etiquetas DROP CONSTRAINT IF EXISTS noticia_etiquetas_pkey;
ALTER TABLE noticia_etiquetas ADD CONSTRAINT fk_noticia_etiquetas_noticia FOREIGN KEY (not_id) REFERENCES noticias(not_id) ON DELETE CASCADE;
ALTER TABLE noticia_etiquetas ADD CONSTRAINT fk_noticia_etiquetas_etiqueta FOREIGN KEY (etq_id) REFERENCES etiquetas(etq_id) ON DELETE CASCADE;
ALTER TABLE noticia_etiquetas ADD PRIMARY KEY (not_id, etq_id);

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 11: TABLE noticias_eliminadas
-- ============================================================================

-- Rename PK
ALTER TABLE noticias_eliminadas RENAME COLUMN id TO nel_id;

-- Rename all columns
ALTER TABLE noticias_eliminadas RENAME COLUMN titulo TO nel_titulo;
ALTER TABLE noticias_eliminadas RENAME COLUMN subtitulo TO nel_subtitulo;
ALTER TABLE noticias_eliminadas RENAME COLUMN contenido TO nel_contenido;
ALTER TABLE noticias_eliminadas RENAME COLUMN imagen_url TO nel_imagen_url;
ALTER TABLE noticias_eliminadas RENAME COLUMN autor_original_id TO nel_autor_original_id;
ALTER TABLE noticias_eliminadas RENAME COLUMN autor_original_nombre TO nel_autor_original_nombre;
ALTER TABLE noticias_eliminadas RENAME COLUMN categoria_id TO nel_categoria_id;
ALTER TABLE noticias_eliminadas RENAME COLUMN categoria_nombre TO nel_categoria_nombre;
ALTER TABLE noticias_eliminadas RENAME COLUMN categoria_color TO nel_categoria_color;
ALTER TABLE noticias_eliminadas RENAME COLUMN fecha_publicacion TO nel_fecha_publicacion_original;
ALTER TABLE noticias_eliminadas RENAME COLUMN likes TO nel_likes;
ALTER TABLE noticias_eliminadas RENAME COLUMN dislikes TO nel_dislikes;
ALTER TABLE noticias_eliminadas RENAME COLUMN visitas TO nel_visitas;
ALTER TABLE noticias_eliminadas RENAME COLUMN comentarios_count TO nel_comentarios_count;
ALTER TABLE noticias_eliminadas RENAME COLUMN motivo TO nel_motivo;
ALTER TABLE noticias_eliminadas RENAME COLUMN descripcion TO nel_descripcion;
ALTER TABLE noticias_eliminadas RENAME COLUMN eliminado_por_id TO nel_eliminado_por_id;
ALTER TABLE noticias_eliminadas RENAME COLUMN eliminado_por_nombre TO nel_eliminado_por_nombre;
ALTER TABLE noticias_eliminadas RENAME COLUMN rol_eliminador TO nel_rol_eliminador;
ALTER TABLE noticias_eliminadas RENAME COLUMN fecha_eliminacion TO nel_fecha_eliminacion;
ALTER TABLE noticias_eliminadas RENAME COLUMN es_aportacion_usuario TO nel_es_aportacion_usuario;

COMMIT;
BEGIN;

-- ============================================================================
-- STEP 12: TABLE sanciones
-- ============================================================================

-- Rename PK
ALTER TABLE sanciones RENAME COLUMN id TO san_id;

-- Rename standard columns
ALTER TABLE sanciones RENAME COLUMN tipo TO san_tipo;
ALTER TABLE sanciones RENAME COLUMN estado TO san_estado;
ALTER TABLE sanciones RENAME COLUMN motivo TO san_motivo;
ALTER TABLE sanciones RENAME COLUMN apelacion TO san_apelacion;
ALTER TABLE sanciones RENAME COLUMN resolucion TO san_resolucion;
ALTER TABLE sanciones RENAME COLUMN fecha_inicio TO san_fecha_inicio;
ALTER TABLE sanciones RENAME COLUMN fecha_fin TO san_fecha_fin;

-- Rename FKs
ALTER TABLE sanciones RENAME COLUMN usuario_id TO usu_id;
-- admin_id stays as is (special case)

-- Update FK constraints
ALTER TABLE sanciones DROP CONSTRAINT IF EXISTS sanciones_usuario_id_fkey;
ALTER TABLE sanciones DROP CONSTRAINT IF EXISTS sanciones_admin_id_fkey;
ALTER TABLE sanciones ADD CONSTRAINT fk_sanciones_usuario FOREIGN KEY (usu_id) REFERENCES usuarios(usu_id) ON DELETE CASCADE;
ALTER TABLE sanciones ADD CONSTRAINT fk_sanciones_admin FOREIGN KEY (admin_id) REFERENCES usuarios(usu_id);

COMMIT;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- Run these to verify the migration was successful:

SELECT 'roles' as table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'roles' 
ORDER BY ordinal_position;

SELECT 'usuarios' as table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'usuarios' 
ORDER BY ordinal_position;
