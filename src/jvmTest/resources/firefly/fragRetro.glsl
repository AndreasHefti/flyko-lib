#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_res;

float scanlines = 2;
float scanlineShadow1 =  .85;
float scanlineShadow2 =  .55;

float tricolor = 3;
float onColorFactor = 1.2;
float offColorFactor = .8;
vec4 tricolorFactorRed = vec4(onColorFactor, offColorFactor, offColorFactor, 1);
vec4 tricolorFactorGreen = vec4(offColorFactor, onColorFactor, offColorFactor, 1);
vec4 tricolorFactorBlue = vec4(offColorFactor, offColorFactor, onColorFactor, 1);

void main(){
  vec2 realCoords = floor(u_res * v_texCoords);
  float ym = floor(mod(realCoords.y, scanlines));
  float xm = floor(mod(realCoords.x, tricolor));
  float scanlineShadow = 1;

  if (ym < 1.0) {
    scanlineShadow = scanlineShadow1;
  } else if (ym < 2.0) {
    scanlineShadow = scanlineShadow2;
  }

  if (xm == 1) {
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords) * scanlineShadow * tricolorFactorRed;
  } else if (xm == 2) {
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords) * scanlineShadow * tricolorFactorGreen;
  } else {
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords) * scanlineShadow * tricolorFactorBlue;
  }

}