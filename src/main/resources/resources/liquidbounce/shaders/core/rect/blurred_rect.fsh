#version 150

in vec2 FragCoord;
in vec2 TexCoord;

uniform sampler2D Sampler0;
uniform vec2 uSize;

uniform vec4 uRadius;
uniform float uAlpha;
uniform float uBorderWidth;
uniform vec4 uColor;
uniform vec4 uBorderColor;
uniform float uMix;

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, vec4 radius) {
    radius.xy = (pos.x > 0.0) ? radius.xy : radius.wz;
    radius.x  = (pos.y > 0.0) ? radius.x : radius.y;

    vec2 v = abs(pos) - size + radius.x;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius.x;
}

void main() {
    vec2 center = uSize * 0.5;
    vec2 fragPos = center - (FragCoord * uSize);
    float dist = rdist(fragPos, center - 1.0, uRadius);

    float smoothedAlpha = 1.0 - smoothstep(-1.0, 1.0, dist);

    if (smoothedAlpha > 0.49 || uBorderWidth == 0.0) {
        vec4 texColor = texture(Sampler0, TexCoord);
        vec4 mixedColor = mix(texColor * uColor, uColor, uMix);
        mixedColor.a *= smoothedAlpha;

        float borderAlpha = 1.0 - smoothstep(uBorderWidth - 1.0, uBorderWidth, abs(dist));

        fragColor = mix(
            vec4(mixedColor.rgb, 0.0),
            mix(mixedColor, vec4(uBorderColor.rgb, uBorderColor.a * borderAlpha), borderAlpha),
            smoothedAlpha * uAlpha
        );
    } else {
        fragColor = vec4(0.0);
    }
}