-- Restore categories with correct 3-letter column prefixes

-- 1. Main Categories
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Tecnología', 'Novedades del mundo tech', '#3B82F6', NULL),
('Deportes', 'Todo sobre deportes', '#EF4444', NULL),
('Politica', 'Actualidad política', '#F59E0B', NULL),
('Cultura', 'Arte, cine y música', '#8B5CF6', NULL);

-- 2. Subcategories for Tecnología (Assuming ID 1)
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Moviles', 'Smartphones y tablets', '#60A5FA', (SELECT cat_id FROM categorias WHERE cat_nombre = 'Tecnología')),
('IA', 'Inteligencia Artificial', '#60A5FA', (SELECT cat_id FROM categorias WHERE cat_nombre = 'Tecnología')),
('Hardware', 'Componentes y PC', '#60A5FA', (SELECT cat_id FROM categorias WHERE cat_nombre = 'Tecnología'));

-- 3. Subcategories for Deportes (Assuming ID 2)
INSERT INTO categorias (cat_nombre, cat_descripcion, cat_color, cat_parent_id) VALUES 
('Futbol', 'Liga y Champions', '#F87171', (SELECT cat_id FROM categorias WHERE cat_nombre = 'Deportes')),
('Baloncesto', 'NBA y ACB', '#F87171', (SELECT cat_id FROM categorias WHERE cat_nombre = 'Deportes'));
