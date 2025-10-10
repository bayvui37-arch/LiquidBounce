#version 150

in vec2 FragCoord;
in vec4 FragColor;

uniform vec2 uSize;
uniform vec2 uLocation;
uniform float uRadius;

uniform float uBorderWidth;
uniform vec4 uBorderColor;

out vec4 fragColor;

float distance(vec2 pos, vec2 size, float radius) {
    vec2 v = abs(pos) - size + radius;
    return length(max(v, 0.0)) + min(max(v.x, v.y), 0.0) - radius;
}

void main() {
    vec2 center = uSize * 0.5;
    vec2 fragPos = center - (FragCoord * uSize);
    float dist = distance(fragPos, center - 1.0, uRadius);

    float smoothedAlpha = 1.0 - smoothstep(-1.0, 1.0, dist);

    if (smoothedAlpha > 0.49 || uBorderWidth == 0.0) {
        float borderAlpha = 1.0 - smoothstep(uBorderWidth - 1.0, uBorderWidth, abs(dist));
        vec4 finalColor = mix(FragColor, vec4(uBorderColor.rgb, uBorderColor.a * borderAlpha), borderAlpha);
        fragColor = vec4(finalColor.rgb, finalColor.a * smoothedAlpha * FragColor.a);
    } else {
        fragColor = vec4(0.0);
    }
}