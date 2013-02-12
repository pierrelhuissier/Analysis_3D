
import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import java.util.concurrent.atomic.AtomicInteger;




public class mean_3Dfast implements PlugInFilter {

	static double valPixel=1;
	ImagePlus Imp;
    protected ImageStack stack;
    public int dimX;
    public int dimY;
    public int dimZ;
	protected int val=255;
	protected Thread[] threads;
	
public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
			dimZ = stack.getSize();
			dimX = stack.getWidth();
			dimY = stack.getHeight();
			threads = newThreadArray();
		return DOES_8G+STACK_REQUIRED;
	}

ImageStack duplicateEmpty(ImageStack src) {
    int xSize = src.getWidth();
    int ySize = src.getHeight();
    int zSize = src.getSize();

    ImageStack dest = new ImageStack(xSize, ySize);
    for (int z = 1; z <= zSize; ++z) {
    	dest.addSlice(src.getSliceLabel(z),src.getProcessor(z).createProcessor(xSize, ySize));
    }

    dest.setColorModel(src.getColorModel());

    return dest;
  }

ImageStack duplicate(ImageStack src) {
    int xSize = src.getWidth();
    int ySize = src.getHeight();
    int zSize = src.getSize();

    ImageStack dest = new ImageStack(xSize, ySize);
    byte[] pixels;
    for (int z = 1; z <= zSize; ++z) {
      pixels = (byte[]) src.getPixels(z);
      dest.addSlice("",pixels);
//          src.getProcessor(z).createProcessor(xSize, ySize));
    }

    dest.setColorModel(src.getColorModel());

    return dest;
  }


 public void run(ImageProcessor ip) {
	long startTime,endTime;
    float t;
    ImagePlus imp = WindowManager.getCurrentImage();
   // if (imp == null) {
  //    IJ.noImage();
   //   return;
  //  }
	int v_mean_radius = 5;

		GenericDialog dia = new GenericDialog("3D mean filter:", IJ.getInstance());	
		dia.addNumericField("Radius desired", v_mean_radius, 0);	
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		final int mean_radius = (int)dia.getNextNumber();
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		startTime = System.currentTimeMillis();
		ImageStack src = imp.getStack();
		
		
		final ImageStack stack2 = duplicate(src);
		final AtomicInteger ai = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
		
		public void run() 
		{	
		for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
			int current = superi;
			byte[] pixels;
			
				pixels = (byte[]) stack2.getProcessor(current).getPixelsCopy();
			
				byte[] pixelsSlice=new byte[dimX*dimY];
				int m;
				int[] pixY=new int[dimX*dimY];
				for(int  j=0;j<dimY;j++)
				{
					for(int  i=0;i<dimX;i++)
					{
						m=j*(dimX)+i;
						pixY[m]=0;
					}
				}
				
					//Sum over  x
					for(int  i=0;i<dimX;i++)
					{
						for(int k = -mean_radius; k<=mean_radius;k++)
						{
							if(((k+i)>=0)&&((k+i)<dimX))
							{
								for(int  j=0;j<dimY;j++)
								{
									int m1=j*(dimX)+i;
									int m2=j*dimX+i+k;
									pixY[m2] += pixels[m1] & 0xff;
								}
							}
						}
					}
					
					//Sum over y 		
					for(int  j=0;j<dimY;j++)
					{
						for(int k = -mean_radius; k<=mean_radius;k++)
						{
							if(((k+j)>=0)&&((k+j)<dimY))
							{
								for(int  i=0;i<dimX;i++)
								{
									int m1=j*(dimX)+i;
									int m2=(j+k)*dimX+i;
									pixY[m2] += pixels[m1] & 0xff;
								}
							}
						}
					}
					int n=(2*mean_radius+1)*2;
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							pixelsSlice[m]=(byte)((int)pixY[m]/n);
						}
					}
					stack2.setPixels(pixelsSlice, current);
			}
		}
		};
		}
		
		startAndJoin(threads);

		new ImagePlus("St2", stack2).show();

		ImageStack stack3 = duplicate(src);

		
		int[][] pixels = new int[dimZ][dimX*dimY];
		byte[] pixelsC = new byte[dimX*dimY];
		for(int l= 1; l<=dimZ; l++)
		{
			for(int  j=0;j<dimY;j++)
			{
				for(int  i=0;i<dimX;i++)
				{
					int m=j*(dimX)+i;
					pixels[l-1][m]=0;
				}
			}	
		}
		for(int l= 1; l<=dimZ; l++)
		{
			pixelsC = (byte[]) stack2.getProcessor(l).getPixelsCopy();
			for(int k=-mean_radius; k<=mean_radius; k++)
			{
				if(((k+l)>0)&&((k+l)<=dimZ))
				{
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							int m=j*(dimX)+i;
							pixels[k+l-1][m]+=(int)(pixelsC[m] & 0xff);
						}
					}
				}	
			}
		}
		
		int n=2*mean_radius+1;
		for(int l= 1; l<=dimZ; l++)
		{
			for(int  j=0;j<dimY;j++)
			{
				for(int  i=0;i<dimX;i++)
				{
					int m=j*(dimX)+i;
					pixels[l-1][m]/=n;
				}
			}
			stack3.setPixels(pixels[l-1], l);			
		}
		new ImagePlus("Source", stack3).show();
		
		endTime = System.currentTimeMillis();
        t = (float)(endTime-startTime)/1000;
        IJ.showStatus(""+t+" sec -> total");

}

/** Create a Thread[] array as large as the number of processors available.
 * From Stephan Preibisch's Multithreading.java class. See:
 * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD
 */
 private Thread[] newThreadArray() {
     int n_cpus = Runtime.getRuntime().availableProcessors();
 	return new Thread[n_cpus];
 }

 /** Start all given threads and wait on each of them until all are done.
 * From Stephan Preibisch's Multithreading.java class. See:
 * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD
 */
 private static void startAndJoin(Thread[] threads)
 {
     for (int ithread = 0; ithread < threads.length; ++ithread)
     {
         threads[ithread].setPriority(Thread.NORM_PRIORITY);
         threads[ithread].start();
			System.gc();
     }

     try
     {   
         for (int ithread = 0; ithread < threads.length; ++ithread)
             threads[ithread].join();
     } catch (InterruptedException ie)
     {
         throw new RuntimeException(ie);
     }
 }


  }
