/**
 * Created by Matze on 19.12.2018.
 */
package freakey17;

class Light {
 private Light(){}
 RGB color;
 Vec3D position;
 Light(Vec3D pos, RGB c) { position=pos; color=c; }
}
