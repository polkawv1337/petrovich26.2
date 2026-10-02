#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform vec4 u_StartColor;
uniform vec4 u_EndColor;
uniform float u_Angle;

out vec4 fragColor;

void main() {
    float rad = radians(u_Angle);
    vec2 dir = vec2(cos(rad), sin(rad));

    vec2 centeredUV = texCoord - vec2(0.5);
    float t = dot(centeredUV, dir) + 0.5;
    t = clamp(t, 0.0, 1.0);

    vec4 gradColor = mix(u_StartColor, u_EndColor, t) * vertexColor;
    if (gradColor.a <= 0.0) {
        discard;
    }

    fragColor = gradColor;
}
