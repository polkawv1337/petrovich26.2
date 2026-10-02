#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform sampler2D Sampler0;
uniform float u_Radius;
uniform vec2 u_Direction;

out vec4 fragColor;

const float weights[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec4 result = texture(Sampler0, texCoord) * weights[0];
    vec2 step = u_Direction * max(u_Radius, 1.0);

    for (int i = 1; i < 5; ++i) {
        vec2 offset = step * float(i);
        result += texture(Sampler0, texCoord + offset) * weights[i];
        result += texture(Sampler0, texCoord - offset) * weights[i];
    }

    fragColor = result * vertexColor;
}
