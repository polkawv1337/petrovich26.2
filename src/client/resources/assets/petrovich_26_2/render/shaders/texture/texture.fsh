#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform sampler2D Sampler0;
uniform vec4 u_Color;

out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord);
    vec4 finalColor = tex * vertexColor * u_Color;

    if (finalColor.a <= 0.0) {
        discard;
    }

    fragColor = finalColor;
}
