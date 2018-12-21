package uni.trier.fst.freakey17;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* Implementation of a very simple Raytracer
   Stephan Diehl, Universit�t Trier, 2010-2016
*/



public class SDRaytracer extends JFrame implements Serializable
{
   private static final long serialVersionUID = 1L;
   boolean profiling=false;
   int widthFrame =1000;
   int heightFrame =1000;
   
   Future[] futureList= new Future[widthFrame];
   int nrOfProcessors = Runtime.getRuntime().availableProcessors();
   ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
   
   int maxRec=3;
   int rayPerPixel=1;
   int startX;
   int startY;
   int startZ;

   List<Triangle> triangles;

   Light mainLight  = new Light(new Vec3D(0,100,0), new RGB(0.1f,0.1f,0.1f));

   Light [] lights= new Light[]{ mainLight
                                ,new Light(new Vec3D(100,200,300), new RGB(0.5f,0,0.0f))
                                ,new Light(new Vec3D(-100,200,300), new RGB(0.0f,0,0.5f))
                                //,new Light(new Vec3D(-100,0,0), new RGB(0.0f,0.8f,0.0f))
                              };

   RGB [][] image= new RGB[widthFrame][heightFrame];
   
   float fovx=(float) 0.628;
   float fovy=(float) 0.628;
   RGB ambientColor =new RGB(0.01f,0.01f,0.01f);
   RGB backgroundColor =new RGB(0.05f,0.05f,0.05f);
   RGB black=new RGB(0.0f,0.0f,0.0f);
   int yAngleFactor =4;
   int xAngleFactor =-4;
   
public static void  main(String [] argv)
  { 
  long start = System.currentTimeMillis();
  SDRaytracer sdr=new SDRaytracer();
  long end = System.currentTimeMillis();
  long time = end - start;
  System.out.println("time: " + time + " ms");
  System.out.println("nrprocs="+sdr.nrOfProcessors);
  }

void profileRenderImage(){
  long end;
  long start;
  long time;

  renderImage(); // initialisiere Datenstrukturen, erster Lauf verf�lscht sonst Messungen
  
  for(int procs=1; procs<6; procs++) {

   maxRec=procs-1;
   System.out.print(procs);
   for(int i=0; i<10; i++)
     { start = System.currentTimeMillis();

       renderImage();

       end = System.currentTimeMillis();
       time = end - start;
       System.out.print(";"+time);
     }
    System.out.println("");
   }
}

SDRaytracer()
 {
   createScene();

   if (!profiling) renderImage(); else profileRenderImage();
   
   setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   Container contentPane = this.getContentPane();
   contentPane.setLayout(new BorderLayout());
   JPanel area = new JPanel() {
       @Override
            public void paint(Graphics g) {
              System.out.println("fovx="+fovx+", fovy="+fovy+", xangle="+ xAngleFactor +", yangle="+ yAngleFactor);
              if (image==null) return;
              for(int i = 0; i< widthFrame; i++)
               for(int j = 0; j< heightFrame; j++)
                { g.setColor(image[i][j].color());
                  // zeichne einzelnen Pixel
                  g.drawLine(i, heightFrame -j,i, heightFrame -j);
                }
            }
           };
           
   addKeyListener(new KeyAdapter()
         { @Override
             public void keyPressed(KeyEvent e)
            { boolean redraw=false;
              if (e.getKeyCode()==KeyEvent.VK_DOWN)
                {  xAngleFactor--;

                  redraw=true;
                }
              if (e.getKeyCode()==KeyEvent.VK_UP)
                {  xAngleFactor++;

                  redraw=true;
                }
              if (e.getKeyCode()==KeyEvent.VK_LEFT)
                { yAngleFactor--;

                  redraw=true;
                }
              if (e.getKeyCode()==KeyEvent.VK_RIGHT)
                { yAngleFactor++;

                  redraw=true;
                }
              if (redraw)
               { createScene();
                 renderImage();
                 repaint();
               }
            }
         });
         
        area.setPreferredSize(new Dimension(widthFrame, heightFrame));
        contentPane.add(area);
        this.pack();
        this.setVisible(true);
}
 
Ray eyeRay =new Ray();
double tanFovx;
double tanFovy;
 
void renderImage(){
   tanFovx = Math.tan(fovx);
   tanFovy = Math.tan(fovy);
   for(int i = 0; i< widthFrame; i++)
   { futureList[i]=  eservice.submit(new RaytraceTask(this,i));
   }
   
    for(int i = 0; i< widthFrame; i++)
       { try {
          RGB [] col = (RGB[]) futureList[i].get();
          for(int j = 0; j< heightFrame; j++)
            image[i][j]=col[j];
         }
   catch (InterruptedException e) {e.printStackTrace();}
   catch (ExecutionException e) {e.printStackTrace();}
    }
   }
 


RGB rayTrace(Ray ray, int rec) {
   if (rec>maxRec) return black;
   IPoint ip = hitObject(ray);
   if (ip.dist>IPoint.EPSILON)
     return lighting(ray, ip, rec);
   else
     return black;
}


IPoint hitObject(Ray ray) {
   IPoint isect=new IPoint(null,null,-1);
   float idist=-1;
   for(Triangle t : triangles)
     { IPoint ip = ray.intersect(t);
        if (ip.dist!=-1)
        {

        }
        if ((idist==-1)||(ip.dist<idist))
         { // save that intersection
          idist=ip.dist;
          isect.ipoint=ip.ipoint;
          isect.dist=ip.dist;
          isect.triangle=t;
         }
     }
   return isect;  // return intersection point and normal
}


RGB addColors(RGB c1, RGB c2, float ratio)
 { return new RGB( (c1.red+c2.red*ratio),
           (c1.green+c2.green*ratio),
           (c1.blue+c2.blue*ratio));
  }
  
RGB lighting(Ray ray, IPoint ip, int rec) {
  Vec3D point=ip.ipoint;
  Triangle triangle=ip.triangle;
  RGB color = addColors(triangle.color, ambientColor,1);
  Ray shadowRay=new Ray();
   for(Light light : lights)
       { shadowRay.start=point;
         shadowRay.dir=light.position.minus(point).mult(-1);
         shadowRay.dir.normalize();
         IPoint ip2=hitObject(shadowRay);
         if(ip2.dist<IPoint.EPSILON)
         {
           float ratio=Math.max(0,shadowRay.dir.dot(triangle.normal));
           color = addColors(color,light.color,ratio);
         }
       }
     Ray reflection=new Ray();
     //R = 2N(N*L)-L)    L ausgehender Vektor
     Vec3D l=ray.dir.mult(-1);
     reflection.start=point;
     reflection.dir=triangle.normal.mult(2*triangle.normal.dot(l)).minus(l);
     reflection.dir.normalize();
     RGB rcolor=rayTrace(reflection, rec+1);
     float ratio =  (float) Math.pow(Math.max(0,reflection.dir.dot(l)), triangle.shininess);
     color = addColors(color,rcolor,ratio);
     return(color);
  }

  void createScene()
   {ArrayList<Triangle> triangles = new ArrayList<>();

   
     Cube.addCube(triangles, 0,35,0, 10,10,10,new RGB(0.3f,0,0),0.4f);       //rot, klein
     Cube.addCube(triangles, -70,-20,-20, 20,100,100,new RGB(0f,0,0.3f),.4f);
     Cube.addCube(triangles, -30,30,40, 20,20,20,new RGB(0,0.4f,0),0.2f);        // gr�n, klein
     Cube.addCube(triangles, 50,-20,-40, 10,80,100,new RGB(.5f,.5f,.5f), 0.2f);
     Cube.addCube(triangles, -70,-26,-40, 130,3,40,new RGB(.5f,.5f,.5f), 0.2f);


     Matrix mRx=Matrix.createXRotation((float) (xAngleFactor *Math.PI/16));
     Matrix mRy=Matrix.createYRotation((float) (yAngleFactor *Math.PI/16));
     Matrix mT=Matrix.createTranslation(0,0,200);
     Matrix m=mT.mult(mRx).mult(mRy);
     m.print();
     m.apply(triangles);
   }

}


