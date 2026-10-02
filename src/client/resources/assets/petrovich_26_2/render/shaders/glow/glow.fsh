#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform vec4 u_GlowColor;
uniform float u_Intensity;

out vec4 fragColor;

void main() {

    float dist = distance(texCoord, vec2(0.5));

    float glow = exp(-dist * 4.0 * max(u_Intensity, 0.1));
    glow = clamp(glow, 0.0, 1.0);

    vec4 color = u_GlowColor * vertexColor;
    color.a *= glow;

    if (color.a <= 0.0) {
        discard;
    }

    fragColor = color;
}
