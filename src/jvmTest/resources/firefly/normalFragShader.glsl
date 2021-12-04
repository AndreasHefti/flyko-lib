#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D normal_texture;

void main() {
    gl_FragColor = texture2D(normal_texture, v_texCoords);
    //gl_FragColor = vec4(1.0,1.0,0.0,1.0);
}




