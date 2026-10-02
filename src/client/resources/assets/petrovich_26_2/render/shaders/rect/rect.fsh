#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform vec4 u_Color;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor * u_Color;
    if (color.a <= 0.0) {
        discard;
    }
    fragColor = color;
}
