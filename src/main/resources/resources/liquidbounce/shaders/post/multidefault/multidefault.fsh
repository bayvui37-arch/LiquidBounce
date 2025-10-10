#version 150

uniform sampler2D Sampler0;
uniform vec2 uHalfTexelSize;

in vec2 TexCoord;
out vec4 fragColor;

void main() {
    vec4 center = texture(Sampler0, TexCoord);

    if (center.a != 0) {
         fragColor = vec4(center.rgb, center.a * 0.75);
         return;
    }

    for (float x = -2; x <= 2; x++) {
         for (float y = -2; y <= 2; y++) {
             vec4 curColor = texture(Sampler0, TexCoord + vec2(uHalfTexelSize.x * x, uHalfTexelSize.y * y));
             if (curColor.a != 0) {
                 fragColor = vec4(curColor.rgb, min(1.0, curColor.a * 3.0));
                 return;
             }
         }
    }
    discard;
}