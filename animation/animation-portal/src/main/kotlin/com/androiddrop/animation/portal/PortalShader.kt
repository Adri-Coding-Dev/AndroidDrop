package com.androiddrop.animation.portal

/**
 * Código GLSL de los shaders del portal energético.
 *
 * POR QUÉ dos pares de shaders: El portal requiere un pipeline de dos pasos.
 * El primer par (vertex/fragment) renderiza la geometría del portal (anillo +
 * vórtice). El segundo par (post-process) aplica bloom y distorsión de lente
 * gravitacional sobre el resultado del primer paso.
 */
object PortalShader {

    /**
     * Vertex shader principal del portal.
     * Transformación estándar + coordenadas UV.
     */
    val vertexShader: String = """
        #version 300 es
        precision highp float;

        in vec3 aPosition;
        in vec2 aTexCoord;

        uniform float uRingRotation;

        out vec2 vTexCoord;
        out vec3 vPosition;

        void main() {
            // Rotar alrededor del eje Z
            float cosR = cos(uRingRotation);
            float sinR = sin(uRingRotation);
            vec3 rotatedPos = vec3(
                aPosition.x * cosR - aPosition.y * sinR,
                aPosition.x * sinR + aPosition.y * cosR,
                aPosition.z
            );

            vTexCoord = aTexCoord;
            vPosition = rotatedPos;
            gl_Position = vec4(rotatedPos, 1.0);
        }
    """.trimIndent()

    /**
     * Fragment shader principal del portal.
     * Renderiza el efecto de vórtice y el anillo energético.
     */
    val fragmentShader: String = """
        #version 300 es
        precision highp float;

        in vec2 vTexCoord;
        in vec3 vPosition;

        uniform float uVortexDepth;
        uniform float uIntensity;
        uniform float uGlowIntensity;
        uniform float uDistortion;
        uniform float uTime;

        out vec4 fragColor;

        const vec3 RING_COLOR = vec3(0.2, 0.6, 1.0);
        const vec3 VORTEX_INNER = vec3(0.05, 0.1, 0.3);
        const vec3 VORTEX_OUTER = vec3(0.1, 0.4, 0.9);

        void main() {
            vec2 uv = vTexCoord - 0.5;
            float dist = length(uv);

            // Vórtice: espiral que gira
            float angle = atan(uv.y, uv.x);
            float spiral = sin(angle * 4.0 + dist * 10.0 - uTime * 2.0) * 0.5 + 0.5;
            float vortexAlpha = smoothstep(0.7, 0.0, dist) * uVortexDepth;

            // Anillo energético exterior
            float ring = smoothstep(0.5, 0.48, dist) - smoothstep(0.4, 0.38, dist);
            ring += smoothstep(0.45, 0.43, dist) * 0.5;
            float ringPulse = sin(dist * 30.0 - uTime * 3.0) * 0.5 + 0.5;
            ring *= (0.5 + ringPulse * 0.5);

            // Efecto de lente gravitacional (distorsión radial)
            float distortion = 1.0 + uDistortion * exp(-dist * 3.0);
            vec2 distortedUv = (uv * distortion) + 0.5;

            // Color del vórtice
            vec3 vortexColor = mix(VORTEX_INNER, VORTEX_OUTER, spiral);
            vortexColor *= vortexAlpha * uIntensity;

            // Color del anillo
            vec3 ringColor = RING_COLOR * ring * uIntensity * (1.0 + uGlowIntensity * 0.5);

            // Glow exterior (bloom simulado)
            float glow = exp(-dist * 5.0) * uGlowIntensity * 0.3;

            // Composición
            vec3 finalColor = vortexColor + ringColor + vec3(RING_COLOR) * glow;
            float alpha = max(vortexAlpha, ring) + glow;

            fragColor = vec4(finalColor, alpha);
        }
    """.trimIndent()

    /**
     * Vertex shader para post-processing.
     * Simplemente pasa las coordenadas del quad fullscreen.
     */
    val postVertexShader: String = """
        #version 300 es
        precision highp float;

        in vec2 aPosition;
        in vec2 aTexCoord;

        out vec2 vTexCoord;

        void main() {
            vTexCoord = aTexCoord;
            gl_Position = vec4(aPosition, 0.0, 1.0);
        }
    """.trimIndent()

    /**
     * Fragment shader de post-processing.
     * Aplica bloom (brillo extra) y distorsión de lente gravitacional.
     */
    val postFragmentShader: String = """
        #version 300 es
        precision highp float;

        in vec2 vTexCoord;

        uniform sampler2D uTexture;
        uniform float uDistortion;
        uniform float uGlow;

        out vec4 fragColor;

        void main() {
            vec2 uv = vTexCoord;

            // Distorsión de lente gravitacional
            vec2 center = vec2(0.5, 0.5);
            vec2 offset = uv - center;
            float dist = length(offset);
            float distortionFactor = 1.0 + uDistortion * exp(-dist * 4.0);
            vec2 distortedUv = center + offset * distortionFactor;

            // Muestrear textura
            vec4 color = texture(uTexture, distortedUv);

            // Bloom: extraer regiones brillantes y expandirlas
            float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
            float bloomMask = smoothstep(0.5, 1.0, luminance);
            vec4 bloom = vec4(0.0);

            if (bloomMask > 0.0) {
                // Muestrear vecinos para blur simple
                vec4 blur = vec4(0.0);
                float kernel[9] = float[](
                    1.0, 2.0, 1.0,
                    2.0, 4.0, 2.0,
                    1.0, 2.0, 1.0
                );
                float kernelSum = 16.0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        vec2 sampleUv = distortedUv + vec2(i, j) * 0.002;
                        vec4 sampleColor = texture(uTexture, sampleUv);
                        float sampleLum = dot(sampleColor.rgb, vec3(0.299, 0.587, 0.114));
                        bloom += sampleColor * kernel[(i+1)*3 + (j+1)] * smoothstep(0.5, 1.0, sampleLum);
                    }
                }
                bloom /= kernelSum;
                bloom *= uGlow;
            }

            // Composición final
            vec4 finalColor = color + bloom;
            fragColor = finalColor;
        }
    """.trimIndent()
}
