#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec2 texelSize;
uniform vec2 direction;
uniform float radius;
uniform float glowAlpha;
uniform float weights[16];
uniform vec4 uColor;

in vec2 TexCoord;
out vec4 fragColor;

void main() {
     if (direction.y > 0 && texture(Sampler0, TexCoord).a != 0.0)
        discard;

    float blr = texture(Sampler1, TexCoord).a * glowAlpha * weights[0];

    vec2 offset = texelSize * direction;

    for (float f = 1.0; f <= radius; f++) {
        float w = weights[int(abs(f))] * glowAlpha;

        blr += texture(Sampler1, TexCoord + f * offset).a * w;
        blr += texture(Sampler1, TexCoord - f * offset).a * w;
    }

    fragColor = vec4(uColor.rgb, blr);
}