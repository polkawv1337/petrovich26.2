#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform vec2 u_Center;
uniform float u_Radius;
uniform vec4 u_CenterColor;
uniform vec4 u_EdgeColor;

out vec4 fragColor;

void main() {
    float dist = distance(texCoord, u_Center);
    float t = clamp(dist / max(u_Radius, 0.0001), 0.0, 1.0);

    vec4 color = mix(u_CenterColor, u_EdgeColor, t) * vertexColor;
    if (color.a <= 0.0) {
        discard;
    }

    fragColor = color;
}
