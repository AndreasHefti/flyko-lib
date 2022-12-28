#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
float bloom_spread = .5;
float bloom_intensity = .2;
void main() {
    ivec2 size = textureSize(u_texture, 0);

    float uv_x = v_texCoords.x * size.x;
    float uv_y = v_texCoords.y * size.y;

    vec4 sum = vec4(0.0);
    for (int n = 0; n < 9; ++n) {
        uv_y = (v_texCoords.y * size.y) + (bloom_spread * float(n - 4));
        vec4 h_sum = vec4(0.0);
        h_sum += texelFetch(u_texture, ivec2(uv_x - (4.0 * bloom_spread), uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x - (3.0 * bloom_spread), uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x - (2.0 * bloom_spread), uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x - bloom_spread, uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x, uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x + bloom_spread, uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x + (2.0 * bloom_spread), uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x + (3.0 * bloom_spread), uv_y), 0);
        h_sum += texelFetch(u_texture, ivec2(uv_x + (4.0 * bloom_spread), uv_y), 0);
        sum += h_sum / 9.0;
    }

    gl_FragColor = sum * bloom_intensity; // texture2D(u_texture, v_texCoords) - (sum );
}