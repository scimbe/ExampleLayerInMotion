package com.example.motion.visual.animation;

/**
 * Implementiert verschiedene Easing-Funktionen für Animationsübergänge.
 * Diese Funktionen werden verwendet, um natürlichere und dynamischere Bewegungen zu erzeugen.
 */
public class EasingFunctions {

    /**
     * Lineare Interpolation (keine Beschleunigung)
     */
    public static float linear(float t) {
        return t;
    }

    /**
     * Quadratische Ease-In Funktion
     */
    public static float easeInQuad(float t) {
        return t * t;
    }

    /**
     * Quadratische Ease-Out Funktion
     */
    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }

    /**
     * Quadratische Ease-In-Out Funktion
     */
    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    /**
     * Kubische Ease-In Funktion
     */
    public static float easeInCubic(float t) {
        return t * t * t;
    }

    /**
     * Kubische Ease-Out Funktion
     */
    public static float easeOutCubic(float t) {
        return (--t) * t * t + 1;
    }

    /**
     * Kubische Ease-In-Out Funktion
     */
    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }

    /**
     * Elastische Ease-In Funktion
     */
    public static float easeInElastic(float t) {
        return (float) (Math.sin(13 * Math.PI/2 * t) * Math.pow(2, 10 * (t - 1)));
    }

    /**
     * Elastische Ease-Out Funktion
     */
    public static float easeOutElastic(float t) {
        return (float) (Math.sin(-13 * Math.PI/2 * (t + 1)) * Math.pow(2, -10 * t) + 1);
    }

    /**
     * Elastische Ease-In-Out Funktion
     */
    public static float easeInOutElastic(float t) {
        if (t < 0.5f) {
            return (float) (0.5f * Math.sin(13 * Math.PI * t) * Math.pow(2, 20 * t - 10));
        } else {
            return (float) (0.5f * (Math.sin(-13 * Math.PI * t) * Math.pow(2, -20 * t + 10) + 2));
        }
    }

    /**
     * Bounce Ease-Out Funktion
     */
    public static float easeOutBounce(float t) {
        if (t < 1 / 2.75f) {
            return 7.5625f * t * t;
        } else if (t < 2 / 2.75f) {
            t -= 1.5f / 2.75f;
            return 7.5625f * t * t + 0.75f;
        } else if (t < 2.5f / 2.75f) {
            t -= 2.25f / 2.75f;
            return 7.5625f * t * t + 0.9375f;
        } else {
            t -= 2.625f / 2.75f;
            return 7.5625f * t * t + 0.984375f;
        }
    }

    /**
     * Bounce Ease-In Funktion
     */
    public static float easeInBounce(float t) {
        return 1 - easeOutBounce(1 - t);
    }

    /**
     * Bounce Ease-In-Out Funktion
     */
    public static float easeInOutBounce(float t) {
        if (t < 0.5f) {
            return 0.5f * easeInBounce(t * 2);
        } else {
            return 0.5f * easeOutBounce(t * 2 - 1) + 0.5f;
        }
    }

    /**
     * Berechnet eine Ease-Funktion basierend auf dem angegebenen Typ
     * @param t Fortschritt (0.0 bis 1.0)
     * @param type Typ der Ease-Funktion
     * @return Interpolierter Wert
     */
    public static float ease(float t, EasingType type) {
        switch (type) {
            case LINEAR:
                return linear(t);
            case EASE_IN_QUAD:
                return easeInQuad(t);
            case EASE_OUT_QUAD:
                return easeOutQuad(t);
            case EASE_IN_OUT_QUAD:
                return easeInOutQuad(t);
            case EASE_IN_CUBIC:
                return easeInCubic(t);
            case EASE_OUT_CUBIC:
                return easeOutCubic(t);
            case EASE_IN_OUT_CUBIC:
                return easeInOutCubic(t);
            case EASE_IN_ELASTIC:
                return easeInElastic(t);
            case EASE_OUT_ELASTIC:
                return easeOutElastic(t);
            case EASE_IN_OUT_ELASTIC:
                return easeInOutElastic(t);
            case EASE_IN_BOUNCE:
                return easeInBounce(t);
            case EASE_OUT_BOUNCE:
                return easeOutBounce(t);
            case EASE_IN_OUT_BOUNCE:
                return easeInOutBounce(t);
            default:
                return linear(t);
        }
    }
    
    /**
     * Enum für verschiedene Arten von Ease-Funktionen
     */
    public enum EasingType {
        LINEAR,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_OUT_QUAD,
        EASE_IN_CUBIC,
        EASE_OUT_CUBIC,
        EASE_IN_OUT_CUBIC,
        EASE_IN_ELASTIC,
        EASE_OUT_ELASTIC,
        EASE_IN_OUT_ELASTIC,
        EASE_IN_BOUNCE,
        EASE_OUT_BOUNCE,
        EASE_IN_OUT_BOUNCE
    }
}
