#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec4 a_position;
uniform sampler2D u_texture;
// Parameters
vec2 curvature = (3.0, 3.0);
vec2 scanLineOpacity = (0.5, 0.5);
vec2 screenResolution = (100, 100);

float vignetteOpacity = 0.5;
float brightness = 1;
float vignetteRoundness = 0.5;

vec2 curveRemapUV(vec2 uv)
{
    // as we near the edge of our screen apply greater distortion using a cubic function
    uv = uv * 2.0 - 1.0;
    vec2 offset = abs(uv.yx) / curvature;
    uv = uv + uv * offset * offset;
    uv = uv * 0.5 + 0.5;
    return uv;
}

vec4 scanLineIntensity(float uv, float resolution, float opacity)
{
    float intensity = sin(uv * resolution * 3.14 * 2.0);
    intensity = ((0.5 * intensity) + 0.5) * 0.9 + 0.1;
    return vec4(vec3(pow(intensity, opacity)), 1.0);
}

vec4 vignetteIntensity(vec2 uv, vec2 resolution, float opacity, float roundness)
{
    float intensity = uv.x * uv.y * (1.0 - uv.x) * (1.0 - uv.y);
    return vec4(vec3(clamp(pow((resolution.x / roundness) * intensity, opacity), 0.0, 1.0)), 1.0);
}

void main(void) {
    vec2 remappedUV = curveRemapUV(v_texCoords);
    vec4 baseColor = texture2D(u_texture, remappedUV);

    baseColor *= vignetteIntensity(remappedUV, screenResolution, vignetteOpacity, vignetteRoundness);
    baseColor *= scanLineIntensity(remappedUV.x, screenResolution.y, scanLineOpacity.x);
    baseColor *= scanLineIntensity(remappedUV.y, screenResolution.x, scanLineOpacity.y);
    baseColor *= vec4(vec3(brightness), 1.0);

    if (remappedUV.x < 0.0 || remappedUV.y < 0.0 || remappedUV.x > 1.0 || remappedUV.y > 1.0){
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        gl_FragColor = baseColor;
    }

//    vec4 rgb = texture2D(u_texture, v_texCoords.xy);
//    vec4 intens ;
//    if (fract(gl_FragCoord.y * (0.5*4.0/3.0)) > 0.5)
//    intens = vec4(0);
//    else
//    intens = smoothstep(0.2,0.8,rgb) + normalize(vec4(rgb.xyz, 1.0));
//    float level = (4.0-gl_TexCoord[0].z) * 0.19;
//    gl_FragColor = intens * (0.5-level) + rgb * 1.1 ;
}