#version 150

in vec4 vertexColor;
in vec2 texCoord;
in vec2 fragPos;

uniform vec2 u_Size;
uniform vec4 u_Radius;
uniform vec4 u_Color;

out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 b, vec4 r) {

    float radius = (p.x > 0.0) ? ((p.y > 0.0) ? r.z : r.y) : ((p.y > 0.0) ? r.w : r.x);
    vec2 q = abs(p) - b + vec2(radius);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
}

void main() {
    vec2 halfSize = u_Size * 0.5;

    vec2 p = (texCoord - 0.5) * u_Size;

    float dist = roundedBoxSDF(p, halfSize, u_Radius);

    float edgeSoftness = fwidth(dist);
    float alpha = 1.0 - smoothstep(-edgeSoftness, 0.0, dist);

    vec4 finalColor = vertexColor * u_Color;
    finalColor.a *= alpha;

    if (finalColor.a <= 0.0) {
        discard;
    }

    fragColor = finalColor;
}
