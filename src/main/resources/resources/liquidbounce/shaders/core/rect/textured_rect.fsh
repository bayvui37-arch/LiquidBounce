#version 150

in vec2 FragCoord;

uniform sampler2D Sampler0;
uniform vec2 uResolution;
uniform vec2 uSize;

uniform float uRadius;
uniform float uAlpha;
uniform float uBorderWidth;

uniform vec4 uColor;
uniform vec4 uBorderColor;

uniform float uMix;

out vec4 fragColor;

float distance(vec2 pos, vec2 size, float radius) {
    vec2 v = abs(pos) - size + radius;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius;
}

void main() {
    vec2 center = uSize * 0.5;
    float distance = distance(center - (FragCoord * uSize), center - 1.0, uRadius);

    float smoothedAlpha = 1.0 - smoothstep(-1.0, 1.0, distance);

    if (smoothedAlpha > 0.49 || uBorderWidth == 0.0) {
        vec4 texColor = texture(Sampler0, FragCoord) * uColor;
        vec4 mixedColor = mix(texColor, uColor, uMix);
        mixedColor.a *= smoothedAlpha;

        float borderAlpha = 1.0 - smoothstep(uBorderWidth - 2.0, uBorderWidth, abs(distance));

        fragColor = mix(
            vec4(mixedColor.rgb, 0.0),
            mix(mixedColor, vec4(uBorderColor.rgb, uBorderColor.a * borderAlpha), borderAlpha),
            smoothedAlpha * uAlpha
        );
    } else {
        fragColor = vec4(0.0);
    }
}