/**
 * Design Tokens - Single Source of Truth for Design Values
 * 
 * These tokens define the visual language of the LifeOS system.
 * All components should reference these values instead of hardcoding.
 */

export const colors = {
    // Primary Purple Palette
    purple: {
        50: '#faf5ff',
        100: '#f3e8ff',
        200: '#e9d5ff',
        300: '#d8b4fe',
        400: '#c084fc',
        500: '#a855f7', // Primary
        600: '#9333ea',
        700: '#7e22ce',
        800: '#6b21a8',
        900: '#581c87',
    },

    // Accent Colors
    cyan: {
        400: '#22d3ee',
        500: '#06b6d4',
        600: '#0891b2',
    },

    magenta: {
        400: '#e879f9',
        500: '#d946ef',
        600: '#c026d3',
    },

    // Status Colors
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6',

    // Grayscale
    black: '#0a0a0a',
    gray: {
        900: '#111111',
        800: '#1a1a1a',
        700: '#2a2a2a',
        600: '#3a3a3a',
        500: '#6b7280',
        400: '#9ca3af',
        300: '#d1d5db',
    },
    white: '#ffffff',
} as const;

export const spacing = {
    xs: '0.25rem',    // 4px
    sm: '0.5rem',     // 8px
    md: '1rem',       // 16px
    lg: '1.5rem',     // 24px
    xl: '2rem',       // 32px
    '2xl': '3rem',    // 48px
    '3xl': '4rem',    // 64px
} as const;

export const borderRadius = {
    sm: '0.25rem',    // 4px
    md: '0.5rem',     // 8px
    lg: '0.75rem',    // 12px
    xl: '1rem',       // 16px
    full: '9999px',
} as const;

export const fontSize = {
    xs: '0.75rem',    // 12px
    sm: '0.875rem',   // 14px
    base: '1rem',     // 16px
    lg: '1.125rem',   // 18px
    xl: '1.25rem',    // 20px
    '2xl': '1.5rem',  // 24px
    '3xl': '1.875rem',// 30px
    '4xl': '2.25rem', // 36px
    '5xl': '3rem',    // 48px
} as const;

export const fontWeight = {
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
} as const;

export const shadows = {
    glow: {
        purple: '0 0 20px rgba(168, 85, 247, 0.5)',
        cyan: '0 0 20px rgba(34, 211, 238, 0.5)',
        magenta: '0 0 20px rgba(217, 70, 239, 0.5)',
    },
    card: '0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -1px rgba(0, 0, 0, 0.2)',
} as const;

export const transitions = {
    fast: '150ms ease-in-out',
    normal: '300ms ease-in-out',
    slow: '500ms ease-in-out',
} as const;

export const zIndex = {
    base: 0,
    dropdown: 1000,
    sticky: 1100,
    modal: 1200,
    popover: 1300,
    tooltip: 1400,
} as const;
