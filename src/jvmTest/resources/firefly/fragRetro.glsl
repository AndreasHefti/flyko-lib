#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_res;

void main(){
  vec2 realCoords = floor(u_res * v_texCoords);
  float ym = floor(mod(realCoords.y, 2));
  float xm = floor(mod(realCoords.x, 3.00));
  float iy = 1;
  float ix = 1;
  if (ym < 1.0) { iy = .75; } else if (ym < 2.0) { iy = .85; }
  //if (xm < 1.0) { ix = .95; } else if (xm < 2.0) { ix = .98; }
  gl_FragColor = v_color * texture2D(u_texture, v_texCoords) * iy;

  float fact = 0.8;
  if (xm == 1) {
    gl_FragColor.g = gl_FragColor.g * fact;
    gl_FragColor.b = gl_FragColor.b * fact;
  }
  else if (xm == 2) {
    gl_FragColor.r = gl_FragColor.r * fact;
    gl_FragColor.b = gl_FragColor.b * fact;
  }
  else {
    gl_FragColor.r = gl_FragColor.r * fact;
    gl_FragColor.g = gl_FragColor.g * fact;
  }

//  if (xm < 1.0) { gl_FragColor.r = gl_FragColor.r * 1.5; }
//  else if (xm < 2.0) { gl_FragColor.g = gl_FragColor.g * 1.5; }
//  else { gl_FragColor.b = gl_FragColor.b * 1.5; }

}