package com.androiddrop.animation.sphere

/**
 * Código GLSL de los shaders de la esfera energética.
 *
 * POR QUÉ shaders en un object de Kotlin vs archivos .glsl separados:
 * Mantener el código GLSL inline en Kotlin evita tener que gestionar
 * assets raw y su carga asíncrona. Para un motor de animación donde
 * los shaders son relativamente cortos y específicos del dominio, esta
 * aproximación es más práctica y mantenible.
 *
 * El vertex shader aplica desplazamiento por noise 3D para dar la
 * apariencia de superficie viva/orgánica. El fragment shader combina
 * efecto Fresnel, cáusticas internas y pulsos de energía para crear
 * el aspecto característico de "esfera energética".
 */
object SphereShader {

    /**
     * Vertex shader de la esfera.
     *
     * Transformación estándar del vértice más desplazamiento por noise
     * 3D pseudoaleatorio. El desplazamiento depende de la normal, la
     * posición original y el tiempo, dando un efecto de "respiración"
     * y ondulación superficial.
     */
    val vertexShader: String = """
        #version 300 es
        precision highp float;

        // Atributos
        in vec3 aPosition;
        in vec3 aNormal;
        in vec2 aTexCoord;

        // Uniforms
        uniform mat4 uMatrix;
        uniform float uTime;
        uniform float uPulsePhase;
        uniform float uScale;

        // Outputs al fragment shader
        out vec3 vNormal;
        out vec3 vPosition;
        out vec2 vTexCoord;
        out float vDisplacement;

        // Función de noise 3D simple (hash-based)
        float hash(vec3 p) {
            p = fract(p * 0.3183099 + 0.1);
            p *= 17.0;
            return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
        }

        float noise(vec3 p) {
            vec3 i = floor(p);
            vec3 f = fract(p);
            f = f * f * (3.0 - 2.0 * f);

            float a = hash(i);
            float b = hash(i + vec3(1.0, 0.0, 0.0));
            float c = hash(i + vec3(0.0, 1.0, 0.0));
            float d = hash(i + vec3(1.0, 1.0, 0.0));
            float e = hash(i + vec3(0.0, 0.0, 1.0));
            float f_ = hash(i + vec3(1.0, 0.0, 1.0));
            float g = hash(i + vec3(0.0, 1.0, 1.0));
            float h = hash(i + vec3(1.0, 1.0, 1.0));

            float mix1 = mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
            float mix2 = mix(mix(e, f_, f.x), mix(g, h, f.x), f.y);
            return mix(mix1, mix2, f.z);
        }

        void main() {
            // Desplazamiento por noise para superficie orgánica
            float pulse = sin(uPulsePhase + length(aPosition) * 3.0) * 0.03;
            float n = noise(aPosition * 2.0 + uTime * 0.1) * 0.05;
            float displacement = pulse + n;
            vDisplacement = displacement;

            // Desplazar vértice a lo largo de la normal
            vec3 displacedPos = aPosition + aNormal * displacement;

            // Aplicar escala
            displacedPos *= uScale;

            // Transformación final
            vec4 worldPos = uMatrix * vec4(displacedPos, 1.0);
            vPosition = worldPos.xyz;
            vNormal = mat3(uMatrix) * aNormal;
            vTexCoord = aTexCoord;

            gl_Position = worldPos;
        }
    """.trimIndent()

    /**
     * Fragment shader de la esfera.
     *
     * Combina múltiples efectos:
     *   - Fresnel: Borde más brillante que el centro (efecto de campo energético).
     *   - Cáusticas: Patrones de luz interna simulando refracción.
     *   - Pulsos de energía: Ondas que viajan por la superficie.
     *   - Blend aditivo: Para el glow exterior.
     */
    val fragmentShader: String = """
        #version 300 es
        precision highp float;

        in vec3 vNormal;
        in vec3 vPosition;
        in vec2 vTexCoord;
        in float vDisplacement;

        uniform float uEnergy;
        uniform float uBrightness;
        uniform float uTime;

        out vec4 fragColor;

        // Color base de la esfera (azul energético AndroidDrop)
        const vec3 BASE_COLOR = vec3(0.1, 0.4, 0.9);
        const vec3 ENERGY_COLOR = vec3(0.3, 0.7, 1.0);
        const vec3 EDGE_COLOR = vec3(0.6, 0.9, 1.0);

        void main() {
            vec3 normal = normalize(vNormal);
            vec3 viewDir = normalize(-vPosition);

            // Efecto Fresnel: más brillante en los bordes
            float fresnel = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0);
            fresnel *= (1.0 + uEnergy * 0.5);

            // Cáusticas internas simuladas con noise trigonométrico
            float caustic = sin(vPosition.x * 5.0 + uTime * 0.5) *
                            cos(vPosition.y * 5.0 + uTime * 0.3) *
                            sin(vPosition.z * 5.0 + uTime * 0.7);
            caustic = caustic * 0.5 + 0.5;
            caustic *= uEnergy;

            // Pulsos de energía viajando por la superficie
            float pulse = sin(vTexCoord.x * 20.0 + vTexCoord.y * 15.0 - uTime * 2.0) * 0.5 + 0.5;
            pulse *= uEnergy;

            // Desplazamiento como indicador de actividad
            float activity = abs(vDisplacement) * 20.0;

            // Composición final
            vec3 color = BASE_COLOR;
            color += ENERGY_COLOR * (caustic + pulse * 0.3);
            color += EDGE_COLOR * fresnel * 0.5;
            color += vec3(0.3, 0.6, 1.0) * activity;

            // Brillo y energía
            color *= uBrightness * (0.8 + uEnergy * 0.4);

            // Alpha para blending aditivo
            float alpha = 0.7 + fresnel * 0.3 + caustic * 0.2;

            fragColor = vec4(color, alpha);
        }
    """.trimIndent()
}
