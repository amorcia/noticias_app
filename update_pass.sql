UPDATE usuarios 
SET password = '$2a$10$UGHBCGr8nLuHaQzAG9E.uuhjYutxPFFRu4QeZxgQ.f0JXK3bd3Xt', 
    activo = true, 
    es_super_admin = true, 
    rol_id = 1 
WHERE email = 'antoniowebserver@gmail.com';
