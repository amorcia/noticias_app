// Global Settings Manager for NoticiasApp
// This script applies user preferences (theme, colors, language) across ALL views

(function () {
    'use strict';

    // Load settings from localStorage
    const settings = {
        theme: localStorage.getItem('theme') || 'light',
        primaryColor: localStorage.getItem('primaryColor') || '#2563EB',
        language: localStorage.getItem('language') || 'es'
    };

    // Apply theme
    function applyTheme() {
        document.documentElement.setAttribute('data-theme', settings.theme);
    }

    // Apply primary color
    function applyPrimaryColor() {
        document.documentElement.style.setProperty('--primary', settings.primaryColor);
    }

    // Translation dictionary
    const translations = {
        es: {
            'Inicio': 'Inicio',
            'Categorías': 'Categorías',
            'Vuestras Noticias': 'Vuestras Noticias',
            'Iniciar Sesión': 'Iniciar Sesión',
            'Registrarse': 'Registrarse',
            'Mi Perfil': 'Mi Perfil',
            'Ajustes': 'Ajustes',
            'Panel Admin': 'Panel Admin',
            'Cerrar Sesión': 'Cerrar Sesión',
            'Subir': 'Subir',
            'Tecnología': 'Tecnología',
            'Deportes': 'Deportes',
            'Política': 'Política',
            'Cultura': 'Cultura',
            'Móviles': 'Móviles',
            'IA': 'IA',
            'Hardware': 'Hardware',
            'Fútbol': 'Fútbol',
            'Baloncesto': 'Baloncesto',
            'SECCIÓN OFICIAL': 'SECCIÓN OFICIAL',
            'FORO COMUNITARIO': 'FORO COMUNITARIO',
            'No hay noticias en esta categoría todavía.': 'No hay noticias en esta categoría todavía.'
        },
        en: {
            'Inicio': 'Home',
            'Categorías': 'Categories',
            'Vuestras Noticias': 'Your News',
            'Iniciar Sesión': 'Login',
            'Registrarse': 'Sign Up',
            'Mi Perfil': 'My Profile',
            'Ajustes': 'Settings',
            'Panel Admin': 'Admin Panel',
            'Cerrar Sesión': 'Logout',
            'Subir': 'Upload',
            'Tecnología': 'Technology',
            'Deportes': 'Sports',
            'Política': 'Politics',
            'Cultura': 'Culture',
            'Móviles': 'Mobile',
            'IA': 'AI',
            'Hardware': 'Hardware',
            'Fútbol': 'Football',
            'Baloncesto': 'Basketball',
            'SECCIÓN OFICIAL': 'OFFICIAL SECTION',
            'FORO COMUNITARIO': 'COMMUNITY FORUM',
            'No hay noticias en esta categoría todavía.': 'No news in this category yet.'
        }
    };

    // Apply language translations
    function applyLanguage() {
        const lang = settings.language;
        const dict = translations[lang];

        if (!dict) return;

        // Translate all elements with data-translate attribute
        document.querySelectorAll('[data-translate]').forEach(el => {
            const key = el.getAttribute('data-translate');
            if (dict[key]) {
                el.textContent = dict[key];
            }
        });

        // Translate common text nodes (be careful with this)
        document.querySelectorAll('.nav-link, .dropdown-item, .btn').forEach(el => {
            const text = el.textContent.trim();
            if (dict[text]) {
                el.textContent = dict[text];
            }
        });
    }

    // Initialize settings on page load
    function init() {
        applyTheme();
        applyPrimaryColor();
        applyLanguage();
    }

    // Run on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Listen for storage changes (for multi-tab sync)
    window.addEventListener('storage', function (e) {
        if (e.key === 'theme' || e.key === 'primaryColor' || e.key === 'language') {
            settings[e.key.replace('primary', 'primary')] = e.newValue;
            init();
        }
    });

    // Expose settings API globally
    window.NoticiasAppSettings = {
        get: () => settings,
        set: (key, value) => {
            settings[key] = value;
            localStorage.setItem(key, value);
            init();
        },
        reset: () => {
            localStorage.removeItem('theme');
            localStorage.removeItem('primaryColor');
            localStorage.removeItem('language');
            settings.theme = 'light';
            settings.primaryColor = '#2563EB';
            settings.language = 'es';
            init();
        }
    };
})();
