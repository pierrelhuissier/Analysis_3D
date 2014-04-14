import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.util.Vector;
import java.awt.event.*;
import java.awt.Point;
import ij.plugin.filter.*;
import java.util.LinkedList;


    
public class region_growing_3D implements PlugInFilter, MouseListener{

	protected ImageStack stack;
	static int valPixelmax=100;
	static int valPixelmin=0;
	int test=1;
    String [] connect = {"6","18","26"};
	Point clickPoint = new Point();
	ImageCanvas canvas;
	double zoom;
	protected byte[][] srcPixels=null ;
	int dimx,dimy,dimz;
	int valmin,valmax;
	boolean cont;
	ImageProcessor ip;
    protected LinkedList candidatePoints = new LinkedList();
	
    protected Roi RoiselectedPoints=null;
	ImagePlus imp;
    int[] coord;

public int setup(String arg, ImagePlus imp) {
    coord = new int[3];
    coord[0]=-1;
    stack=imp.getStack();
    this.imp=imp;
	return DOES_8G+STACK_REQUIRED;
}

final protected void checkForGrow(int x, int y, int z,int dim1, int dim2,int dim3, int cc, int cmin, int cmax) {
    if (x < 0 || x >= dim1 ||
        y < 0 || y >= dim2 ||
        z < 0 || z >= dim3) {
      return;
    }

    int offset = y * dim1 + x;

    int value = srcPixels[z][offset] & 0xff;
    if (value >= cmin && value < cmax) {
        srcPixels[z][offset] = (byte) 255;
        candidatePoints.addLast(new Point3DInt(x, y, z));
    }
}

void label3D6(int dim1, int dim2,int dim3, int cc, int cmin, int cmax)
{
    // Iterate while there are still candidates to check.
    while (!candidatePoints.isEmpty()) {
      Point3DInt p = (Point3DInt) candidatePoints.removeFirst();
      checkForGrow(p.x - 1, p.y, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x, p.y - 1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x, p.y + 1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x, p.y, p.z - 1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x, p.y, p.z + 1,dim1,dim2,dim3,cc,cmin,cmax);
    }
}


void label3D18(int dim1, int dim2,int dim3, int cc, int cmin, int cmax)
{
    // Iterate while there are still candidates to check.
    while (!candidatePoints.isEmpty()) {
      Point3DInt p = (Point3DInt) candidatePoints.removeFirst();
      checkForGrow(p.x + 1, p.y, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y-1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y+1, p.z,dim1,dim2,dim3,cc,cmin,cmax);

//      checkForGrow(p.x + 1, p.y-1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
//      checkForGrow(p.x + 1, p.y+1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);

//      checkForGrow(p.x + 1, p.y-1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
//      checkForGrow(p.x + 1, p.y+1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x - 1, p.y, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y-1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x -1, p.y+1, p.z,dim1,dim2,dim3,cc,cmin,cmax);

//      checkForGrow(p.x - 1, p.y-1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
//      checkForGrow(p.x - 1, p.y+1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);

//      checkForGrow(p.x - 1, p.y-1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
//      checkForGrow(p.x - 1, p.y+1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x , p.y-1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y+1, p.z,dim1,dim2,dim3,cc,cmin,cmax);


      checkForGrow(p.x , p.y-1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y+1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x , p.y-1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y+1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
    }
}

void label3D26(int dim1, int dim2,int dim3, int cc, int cmin, int cmax)
{
    // Iterate while there are still candidates to check.
    while (!candidatePoints.isEmpty()) {
      Point3DInt p = (Point3DInt) candidatePoints.removeFirst();
      checkForGrow(p.x + 1, p.y, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y-1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y+1, p.z,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x + 1, p.y-1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y+1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x + 1, p.y-1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y+1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x + 1, p.y, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x - 1, p.y, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y-1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x -1, p.y+1, p.z,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x - 1, p.y-1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y+1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x - 1, p.y-1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y+1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x - 1, p.y, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x , p.y-1, p.z,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y+1, p.z,dim1,dim2,dim3,cc,cmin,cmax);


      checkForGrow(p.x , p.y-1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y+1, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y, p.z+1,dim1,dim2,dim3,cc,cmin,cmax);

      checkForGrow(p.x , p.y-1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y+1, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
      checkForGrow(p.x , p.y, p.z-1,dim1,dim2,dim3,cc,cmin,cmax);
    }
}


void miseajour (byte[][] vol, int dim3)
{
    for (int i=1;i<=dim3;i++)
    {
        stack.setPixels(vol[i-1],i);
    }
}

class Point3DInt {

  /**  x coordinate. */
  public int x;
  /**  y coordinate. */
  public int y;
  /**  z coordinate. */
  public int z;


  /**  Constructor for the Point3D object */
  public Point3DInt() { }

  /**
   *  Constructor for the Point3D object
   *
   * @param  x  x coordinate.
   * @param  y  y coordinate.
   * @param  z  z coordinate.
   */
  public Point3DInt(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }


  /**
   *  Returns a string representing Point3D coordinates: (x,y,z).
   *
   * @return    String representing Point3D coordinates.
   */
  public String toString() {
    return "(" + x + "," + y + "," + z + ")";
  }
}


public void run(ImageProcessor ip) {

    dimz =stack.getSize();
    dimx = stack.getWidth();
    dimy = stack.getHeight();
    Object[] imageArray = stack.getImageArray();

    IJ.showStatus("wait processing  ...   ");

    srcPixels = new byte[dimz][];
    for (int z = 0; z < dimz; ++z) {
        srcPixels[z] = (byte[]) imageArray[z];
    }
    for (int z = 0; z < dimz; z++) {
    	for (int y = 0; y < dimy; y++) {
        	for (int x = 0; x< dimx; x++) {
                int pix= srcPixels[z][ y * dimx + x] & 0xff;
                if(pix>0) pix=pix-1;
                srcPixels[z][y * dimx + x]= (byte) (pix);
            }
        }
    }


//    IJ.setTool("point");

/**    while(coord[0]==-1){
        WaitForUserDialog dial1 = new WaitForUserDialog("Select seed", "Select seed by click and press OK");
        dial1.show();
        if(imp.getRoi()!=null){
            coord[0] = imp.getRoi().getPolygon().xpoints[0];
            coord[1] = imp.getRoi().getPolygon().ypoints[0];
            coord[2] = imp.getSlice();
        }
    }
*/
    canvas =(imp.getWindow()).getCanvas();
    canvas.addMouseListener(this);
//    canvas.addKeyListener(this);
    zoom = canvas.getMagnification();

    IJ.resetEscape();
    IJ.showStatus("click to start region growing  ...   ");
    cont=true;
    coord[0]=-1;
    cont=true;

    IJ.showStatus("Finished  ...   ");
}

/**
public void keyPressed(KeyEvent e) { 
    canvas.removeMouseListener(this);
    canvas.removeKeyListener(this);
}

public void keyReleased(KeyEvent e) {}
public void keyTyped(KeyEvent e) {}
*/

public void mouseReleased(MouseEvent e) {}
public void mousePressed(MouseEvent e) {}
public void mouseExited(MouseEvent e) {}
public void mouseEntered(MouseEvent e) {}
public void mouseClicked(MouseEvent e)
{
    clickPoint.x = e.getX();
    clickPoint.y = e.getY(); 
    coord[0]= (int) (clickPoint.x/zoom);
    coord[1] = (int) (clickPoint.y/zoom);
    coord[2] = imp.getCurrentSlice();
    grow(coord);
}

public void grow(int coord[])
{
	int x_seed,y_seed,z_seed;
    x_seed = coord[0];
    y_seed = coord[1];
    z_seed = coord[2];
    ImageProcessor ip = stack.getProcessor(z_seed);
    int pix=(int) ip.getPixelValue(x_seed,y_seed);
    if(pix==255)
    {
        canvas.removeMouseListener(this);
        return;
    }

    ip.set(x_seed,y_seed,255);

    GenericDialog dia = new GenericDialog("3D grower:", IJ.getInstance());	
    dia.addNumericField("min", valPixelmin, 0);
    dia.addNumericField("max", valPixelmax, 0);
    dia.addChoice("3D connectivity",connect,connect[0]);
    dia.showDialog();
    
    if (dia.wasCanceled()) return;
	else
    {	
       if(dia.invalidNumber())
        {
            IJ.showMessage("Error", "Invalid input Number");
            return;
        }
        else
        {	
            valmin = (int) dia.getNextNumber();
            valmax = (int) dia.getNextNumber();
            String index1 = connect[dia.getNextChoiceIndex()];	
            if(valmax>255) valmax=255;
            valPixelmax=valmax ;
            valPixelmin=valmin;
            checkForGrow(x_seed,  y_seed, z_seed,dimx,dimy,dimz,255,valmin,valmax);

            IJ.showStatus("Region growing ...   ");
            if((pix>=valmin) && (pix<=valmax)) 
            {
                if (index1=="6")  label3D6(dimx,dimy,dimz,255,valmin,valmax);
                if (index1=="18")  label3D18(dimx,dimy,dimz,255,valmin,valmax);
                if (index1=="26")  label3D26(dimx,dimy,dimz,255,valmin,valmax);

                miseajour (srcPixels ,dimz);	
                imp.unlock();
                imp.draw();
                IJ.showStatus("Click on white to stop...   ");
            }
            else IJ.showMessage("seed point not in min / max range");
        }
    }
}

/**
//public void mouseClicked(MouseEvent e)
public void test(MouseEvent e)
{
    clickPoint.x = e.getX();
    clickPoint.y = e.getY(); 
    x_seed = (int) (clickPoint.x/zoom);
    y_seed = (int) (clickPoint.y/zoom);
    z_seed = imp.getCurrentSlice();
    ImageProcessor ip = stack.getProcessor(z_seed);
    int pix=(int) ip.getPixelValue(x_seed,y_seed);
    ip.set(x_seed,y_seed,255);

    GenericDialog dia = new GenericDialog("3D labeling:", IJ.getInstance());	
    dia.addNumericField("min", valPixelmin, 0);
    dia.addNumericField("max", valPixelmax, 0);
    dia.addChoice("3D connectivity",connect,connect[0]);
    dia.showDialog();
    cont=false;
    
    if (dia.wasCanceled()) return;
	else
    {	
       if(dia.invalidNumber())
        {
            IJ.showMessage("Error", "Invalid input Number");
            return;
        }
        else
        {	
            valmin = (int) dia.getNextNumber();
            valmax = (int) dia.getNextNumber();
            String index1 = connect[dia.getNextChoiceIndex()];	
            valPixelmax=valmax ;
            valPixelmin=valmin;
            checkForGrow(x_seed,  y_seed, z_seed,dimx,dimy,dimz,255,valmin,valmax);

            IJ.showStatus("region growing ...   ");
            if((pix>=valmin) && (pix<=valmax)) 
            {
                if (index1=="6")  label3D6(dimx,dimy,dimz,255,valmin,valmax);
                if (index1=="18")  label3D18(dimx,dimy,dimz,255,valmin,valmax);
                if (index1=="26")  label3D26(dimx,dimy,dimz,255,valmin,valmax);

                miseajour (srcPixels ,dimz);	
                imp.unlock();
                imp.draw();
                IJ.showStatus("finished...   ");
            }
            else IJ.showMessage("seed point not in min / max range");
        }
    }
}
*/
}

