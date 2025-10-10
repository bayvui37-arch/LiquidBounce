#version 150

uniform sampler2D Sampler0;
uniform vec2 uHalfTexelSize;
uniform vec4 uColor;
uniform float uTime;

in vec2 TexCoord;
out vec4 fragColor;

vec4 wave(vec2 pos) {
    return mix(vec4(0, 0, 0, 1.0), uColor, sin((distance(vec2(0), pos) - uTime * 2.0) / 2.) * 0.5 + 0.5);
}

void main() {
    vec4 center = texture(Sampler0, TexCoord);
    if (center.a != 0) {
        discard;
    }

    float alpha = 0;
    for (float x = -3; x <= 3; x++) {
        for (float y = -3; y <= 3; y++) {
            if (texture(Sampler0, TexCoord + vec2(uHalfTexelSize.x * x, uHalfTexelSize.y * y)).a == 0) {
                continue;
            }

            alpha += max(0, (9.0 - sqrt(x * x + y * y)));
        }
    }

    if (alpha == 0) 
        discard;

    fragColor = vec4(wave(gl_FragCoord.xy).rgb, alpha);
}
