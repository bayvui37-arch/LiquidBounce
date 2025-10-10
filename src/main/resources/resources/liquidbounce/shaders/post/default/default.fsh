#version 150

in vec2 TexCoord;

uniform sampler2D Sampler0;
uniform vec2 uHalfTexelSize;
uniform vec4 uColor;
uniform float uLineWidth;

out vec4 fragColor;

void main() {
    vec4 center = texture(Sampler0, TexCoord);

    if (center.a != 0) {
        fragColor = uColor;
        return;
    }

    float alpha = 0;
    for (float x = -2; x <= 2; x++) {
        for (float y = -2; y <= 2; y++) {
            vec4 curColor = texture(Sampler0, TexCoord + vec2(uHalfTexelSize.x * x, uHalfTexelSize.y * y));
            if (curColor.a != 0)
                alpha += max(0, (uLineWidth - sqrt(x * x + y * y)));
        }
    }

    fragColor = vec4(uColor.rgb, alpha);
}