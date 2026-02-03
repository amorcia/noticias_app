-- ============================================================================
-- CLEANUP SCRIPT - Remove ALL duplicate and incorrectly prefixed columns
-- Generated: 2026-02-03
-- ============================================================================
-- WARNING: This script will DROP columns. Ensure you have a backup!
-- ============================================================================

BEGIN;

-- ============================================================================
-- TABLE: roles
-- ============================================================================
-- Drop old columns (keeping only: rol_id, rol_nombre, rol_descripcion)
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'roles' 
        AND column_name NOT IN ('rol_id', 'rol_nombre', 'rol_descripcion')
    LOOP
        EXECUTE 'ALTER TABLE roles DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: usuarios
-- ============================================================================
-- Drop old columns (keeping only: usu_id, usu_nombre_completo, usu_email, etc.)
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'usuarios' 
        AND column_name NOT IN (
            'usu_id', 'usu_nombre_completo', 'usu_email', 'usu_movil', 'usu_password',
            'rol_id', 'usu_activo', 'usu_codigo_verificacion', 'usu_token_session',
            'usu_vetado', 'usu_motivo_veto', 'usu_fecha_veto', 'usu_vetado_hasta',
            'usu_secret_key_2fa', 'usu_imagen_url', 'usu_email_pendiente'
        )
    LOOP
        EXECUTE 'ALTER TABLE usuarios DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: categorias
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'categorias' 
        AND column_name NOT IN ('cat_id', 'cat_nombre', 'cat_descripcion', 'cat_color', 'cat_parent_id')
    LOOP
        EXECUTE 'ALTER TABLE categorias DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: noticias
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'noticias' 
        AND column_name NOT IN (
            'not_id', 'not_titulo', 'not_subtitulo', 'not_contenido', 'not_imagen_url',
            'usu_id', 'cat_id', 'not_fecha_publicacion', 'not_visitas', 'not_destacada',
            'not_es_aportacion_usuario', 'not_likes', 'not_dislikes', 'not_comentarios_count'
        )
    LOOP
        EXECUTE 'ALTER TABLE noticias DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: comentarios
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'comentarios' 
        AND column_name NOT IN (
            'com_id', 'com_contenido', 'usu_id', 'not_id', 'com_fecha',
            'com_likes', 'com_dislikes', 'com_padre_id'
        )
    LOOP
        EXECUTE 'ALTER TABLE comentarios DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: denuncias
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'denuncias' 
        AND column_name NOT IN (
            'den_id', 'not_id', 'com_id', 'denunciante_id',
            'den_motivo', 'den_descripcion', 'den_estado', 'den_fecha'
        )
    LOOP
        EXECUTE 'ALTER TABLE denuncias DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: votos
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'votos' 
        AND column_name NOT IN ('vot_id', 'usu_id', 'not_id', 'vot_tipo', 'vot_fecha')
    LOOP
        EXECUTE 'ALTER TABLE votos DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: votos_comentarios
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'votos_comentarios' 
        AND column_name NOT IN ('vco_id', 'usu_id', 'com_id', 'vco_tipo', 'vco_fecha')
    LOOP
        EXECUTE 'ALTER TABLE votos_comentarios DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: etiquetas
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'etiquetas' 
        AND column_name NOT IN ('etq_id', 'etq_nombre')
    LOOP
        EXECUTE 'ALTER TABLE etiquetas DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: noticia_etiquetas
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'noticia_etiquetas' 
        AND column_name NOT IN ('not_id', 'etq_id')
    LOOP
        EXECUTE 'ALTER TABLE noticia_etiquetas DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: noticias_eliminadas
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'noticias_eliminadas' 
        AND column_name NOT IN (
            'nel_id', 'nel_titulo', 'nel_subtitulo', 'nel_contenido', 'nel_imagen_url',
            'nel_autor_original_id', 'nel_autor_original_nombre', 'nel_categoria_id',
            'nel_categoria_nombre', 'nel_categoria_color', 'nel_fecha_publicacion_original',
            'nel_likes', 'nel_dislikes', 'nel_visitas', 'nel_comentarios_count',
            'nel_motivo', 'nel_descripcion', 'nel_eliminado_por_id', 'nel_eliminado_por_nombre',
            'nel_rol_eliminador', 'nel_fecha_eliminacion', 'nel_es_aportacion_usuario'
        )
    LOOP
        EXECUTE 'ALTER TABLE noticias_eliminadas DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================================================
-- TABLE: sanciones
-- ============================================================================
DO $$ 
DECLARE
    col_name TEXT;
BEGIN
    FOR col_name IN 
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = 'sanciones' 
        AND column_name NOT IN (
            'san_id', 'usu_id', 'admin_id', 'san_tipo', 'san_estado',
            'san_motivo', 'san_apelacion', 'san_resolucion', 'san_fecha_inicio', 'san_fecha_fin'
        )
    LOOP
        EXECUTE 'ALTER TABLE sanciones DROP COLUMN IF EXISTS ' || quote_ident(col_name) || ' CASCADE';
    END LOOP;
END $$;

COMMIT;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- Run these to verify the cleanup was successful:

-- SELECT table_name, column_name FROM information_schema.columns WHERE table_schema = 'public' ORDER BY table_name, ordinal_position;
