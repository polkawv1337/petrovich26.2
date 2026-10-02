#version 150

in vec4 vertexColor;
in vec2 texCoord;

uniform sampler2D Sampler0;
uniform vec2 u_Offset;

out vec4 fragColor;

void main() {
    vec4 sum = vec4(0.0);
    sum += texture(Sampler0, texCoord + vec2(-u_Offset.x, -u_Offset.y));
    sum += texture(Sampler0, texCoord + vec2( u_Offset.x, -u_Offset.y));
    sum += texture(Sampler0, texCoord + vec2(-u_Offset.x,  u_Offset.y));
    sum += texture(Sampler0, texCoord + vec2( u_Offset.x,  u_Offset.y));

    fragColor = (sum * 0.25) * vertexColor;
}
