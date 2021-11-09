#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D my_texture;

#pragma flyko-lib: import = firefly/glsl/blendLighten.glsl
#pragma flyko-lib: import = firefly/glsl/blendNormal.glsl

void main() {
    vec4 baseColor = texture2D(u_texture, v_texCoords);
    vec4 blendColor = texture2D(my_texture, v_texCoords);

    //gl_FragColor = texture2D(my_texture, v_texCoords);
    //gl_FragColor = vec4(blendColor.rgb, v_color.a);
    gl_FragColor = vec4(blendNormal(baseColor.rgb, blendColor.rgb, 0.5), v_color.a);
}




