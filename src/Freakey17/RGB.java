package Freakey17;
import java.awt.*;

/**
 * Created by Matze on 19.12.2018.
 */


class RGB {
   float red,green,blue;
   Color color;

   RGB(float r, float g, float b)
    { if (r>1) r=1; else if (r<0) r=0;
      if (g>1) g=1; else if (g<0) g=0;
      if (b>1) b=1; else if (b<0) b=0;
      red=r; green=g; blue=b;
    }

   Color color()
    { if (color!=null) return color;
      color=new Color((int) (red*255),(int) (green*255), (int) (blue*255));
      return color;
    }

}
