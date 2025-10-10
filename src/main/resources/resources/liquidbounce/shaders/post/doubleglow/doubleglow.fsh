#version 150

uniform sampler2D Sampler0;

in vec2 TexCoord;

uniform float uTime;
uniform vec4 uColor1;
uniform vec4 uColor2;
uniform int colorMode;
uniform int glowQuality;
uniform float glowRadius;
uniform float glowAlphaMult;
uniform int glowThinOutline;
uniform float fillAlphaMult;
uniform vec2 uHalfTexelSize;
uniform float uSaturation;
uniform float uBrightness;

out vec4 fragColor;

vec3 wave(vec2 pos)
{
    return mix(uColor1.rgb, uColor2.rgb, sin((distance(vec2(0), pos) - uTime * 60.0) / 60.) * 0.5 + 0.5);
}

float rand(vec2 n) {
	return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

float noise(vec2 p){
	vec2 ip = floor(p);
	vec2 u = fract(p);
	u = u*u*(3.0-2.0*u);

	float res = mix(
		mix(rand(ip),rand(ip+vec2(1.0,0.0)),u.x),
		mix(rand(ip+vec2(0.0,1.0)),rand(ip+vec2(1.0,1.0)),u.x),u.y);
	return res*res;
}

void main() {
    vec4 prevColor = texture(Sampler0, TexCoord);
    vec4 outputColor;
    switch(colorMode) {
        case 0:
            outputColor = uColor1;
            break;
        case 1:
            vec3 p = -3.1416 * vec3(0.0, 0.5, 1.0);
            float diagonal = (TexCoord.x + TexCoord.y) * 0.5;
            float distance = diagonal * 10.0 + uTime;
            vec3 col = 0.5 + 0.5 * -sin(distance) * cos(distance + p);

            vec3 gray = vec3(dot(col, vec3(0.299, 0.587, 0.114)));
            col = mix(gray, col, uSaturation);

            outputColor = vec4(col * uBrightness, 1);
            break;
        case 2:
            outputColor = vec4(wave(gl_FragCoord.xy), 1);
            break;
        case 3:
            outputColor = vec4(wave(vec2(0.0, gl_FragCoord.y)), 1);
            break;

        case 4:

            vec3 o = -3.1416 * vec3(0.0, 0.5, 1.0);

            float g = gl_FragCoord.y + uTime;
            vec3 col1 = 0.5 + 0.5 * -sin(g) * cos(g + o);

            outputColor = vec4(col1 * uColor1.rgb, 1);

        /*
            vec3 col1 = uColor1.rgb;

            col1 += sin(TexCoord.y * 1400.) * 0.15;

            float y1 = fract(-uTime / 8.);
            float b1 = 0.002;
            col1 += (smoothstep(y1 - b1, y1, TexCoord.y) - smoothstep(y1, y1 + b1, TexCoord.y)) * abs(sin((TexCoord.y + uTime) * 1.)) * .5;

            float p1 = fract(-uTime / 16.);
            float s1 = 0.2;
            float d1 = smoothstep(p1 - s1, p1, TexCoord.y) - smoothstep(p1, p1 + s1, TexCoord.y);
            col1 += noise((TexCoord * 1000. + uTime * 10.) * 5.) * (0.1  + 0.2 * d1);

            outputColor = vec4(col1, 1.0);
        */
            break;
    }

    float alpha = 0;

    if(glowThinOutline == 1 && prevColor.a == 0) {
        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                if(x != 0 || y != 0) {
                    vec4 offsetColor = texture(Sampler0, TexCoord + vec2(x, y) * uHalfTexelSize);

                    if(offsetColor.a != 0) {
                        fragColor = vec4(outputColor.rgb, 1);
                        return;
                    }
                }
            }
        }
    }

    float i = 0;
    float j = 0;
    int width = int(glowQuality * int(glowRadius));
    int count = int(glowRadius * glowRadius + glowRadius);

    j += sign(texture(Sampler0, TexCoord + vec2(width, 0) * uHalfTexelSize).a);
    j += sign(texture(Sampler0, TexCoord + vec2(-width, 0) * uHalfTexelSize).a);
    j += sign(texture(Sampler0, TexCoord + vec2(0, width) * uHalfTexelSize).a);
    j += sign(texture(Sampler0, TexCoord + vec2(0, -width) * uHalfTexelSize).a);

    j += sign(texture(Sampler0, TexCoord + vec2(width, width) * uHalfTexelSize).a);
    j += sign(texture(Sampler0, TexCoord + vec2(width, -width) * uHalfTexelSize).a);
    j += sign(texture(Sampler0, TexCoord + vec2(-width, width) * uHalfTexelSize).a);
    j += sign(texture(Sampler0, TexCoord + vec2(-width, -width) * uHalfTexelSize).a);

    if(glowRadius > 2 && j == 0.0) {
        if(prevColor.a != 0) {
            fragColor = vec4(outputColor.rgb, outputColor.a * fillAlphaMult);
            return;
        } else {
            discard;
        }
    }

    for(int x = -width; x <= width; x += glowQuality) {
        for(int y = -width; y <= width; y += glowQuality) {
            if (abs(x) == width && abs(y) == width \
                || abs(x) == width && y == 0
                || x == 0 && abs(y) == width
                || x == 0 && y == 0) {
                continue;
            }

            vec4 offsetColor = texture(Sampler0, TexCoord + vec2(x, y) * uHalfTexelSize);

            if(offsetColor.a != 0)
                j++;

            i++;
        }
    }

    alpha = (j / (count * 4));

    if(prevColor.a != 0) {
        alpha = max((1.0 - alpha) * glowAlphaMult, outputColor.a * fillAlphaMult);
    } else {
        alpha *= glowAlphaMult;
    }

    fragColor = vec4(outputColor.rgb, alpha);
}
