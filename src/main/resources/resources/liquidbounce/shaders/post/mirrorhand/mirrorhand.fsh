#version 150

in vec2 TexCoord;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec2 uViewOffset;
uniform vec4 uColor;
uniform float uBorder;
uniform vec2 uHalfTexelSize;

out vec4 fragColor;

void main() {
    vec4 center = texture(Sampler0, TexCoord);

    if (center.a != 0 || uBorder == 0) {
        fragColor = texture(Sampler1, TexCoord + uViewOffset) * uColor * texture(Sampler0, TexCoord).a;
        return;
    }

    float alpha = 0;
    for (float x = -2; x <= 2; x++) {
        for (float y = -2; y <= 2; y++) {
            vec4 curColor = texture(Sampler0, TexCoord + vec2(uHalfTexelSize.x * x, uHalfTexelSize.y * y));
            if (curColor.a != 0)
            alpha += max(0, (2.0 - sqrt(x * x + y * y)));
        }
    }

    fragColor = vec4(uColor.rgb, alpha);
}