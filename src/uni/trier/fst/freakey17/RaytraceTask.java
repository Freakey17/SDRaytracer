package uni.trier.fst.freakey17;

import java.util.concurrent.Callable;

/**
 * Created by Matze on 19.12.2018.
 */

class RaytraceTask implements Callable
{ SDRaytracer tracer;
  int i;
  RaytraceTask(SDRaytracer t, int ii) { tracer=t; i=ii; }

  public RGB[] call()
   { RGB[] col=new RGB[tracer.heightFrame];
     for (int j = 0; j<tracer.heightFrame; j++)
       {  tracer.image[i][j]=new RGB(0,0,0);
            for(int k=0;k<tracer.rayPerPixel;k++)
            { double di=i+(Math.random()/2-0.25);
              double dj=j+(Math.random()/2-0.25);
              if (tracer.rayPerPixel==1) { di=i; dj=j; }
              Ray eyeRay=new Ray();
              eyeRay.setStart(tracer.startX, tracer.startY, tracer.startZ);   // ro
              eyeRay.setDir  ((float) (((0.5 + di) * tracer.tanFovx * 2.0) / tracer.widthFrame - tracer.tanFovx),
                            (float) (((0.5 + dj) * tracer.tanFovy * 2.0) / tracer.heightFrame - tracer.tanFovy),
                            (float) 1f);    // rd
             eyeRay.normalize();
             col[j]= tracer.addColors(tracer.image[i][j],tracer.rayTrace(eyeRay,0),1.0f/tracer.rayPerPixel);
            }
       }
     return col;
   }
}
