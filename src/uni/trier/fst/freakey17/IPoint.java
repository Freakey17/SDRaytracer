/**
 * Created by Matze on 19.12.2018.
 */

package uni.trier.fst.freakey17;


class IPoint {

  static final float EPSILON=0.0001f;
  Triangle triangle;
  Vec3D ipoint;
  float dist;
  IPoint(Triangle tt, Vec3D ip, float d) { triangle=tt; ipoint=ip; dist=d; }
}
