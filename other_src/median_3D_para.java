
import java.util.concurrent.atomic.AtomicInteger;

import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;



public class median_3D_para implements PlugInFilter {

	protected ImageStack stack,stack2;
	int nb;
	static double valPixel=1;
	protected final Thread[] threads= newThreadArray();
	int nbhist;
	
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_8G+DOES_16+STACK_REQUIRED;
	}

	public void run(ImageProcessor ip) {

		GenericDialog dia = new GenericDialog("3D median filter :", IJ.getInstance());	
		dia.addNumericField("Size for mean box filter", valPixel, 0);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		int taille = (int) dia.getNextNumber();

		//ImageStack stack2;
		if(stack.getProcessor(1) instanceof ShortProcessor)
		{
			nbhist=65536;
			stack2 = newShortEmpty(stack);
		}
		else
		{
			nbhist=256;
			stack2 = newByteEmpty(stack);
		}
		treatment(stack,stack2,taille);
		borderk(stack,stack2,taille);
		borderj(stack,stack2,taille);
		borderi(stack,stack2,taille);
		new ImagePlus("mean3D", stack2).show();
	}

void treatment(final ImageStack st, final ImageStack stm, int d)
{
	final int dimX = st.getWidth();
	final int dimY = st.getHeight();
	final int dimZ = st.getSize();
	final int lw=2*d+1;
	final int nw=2*d+1;
	final int mw=2*d+1;
	final int lnm=lw*nw*mw;
	final int lnm2=lnm/2;
	final int lw2=lw/2;
	final int nw2=nw/2;
	final int mw2=mw/2;
	final int lmin=lw2;
	final int lmax=dimZ-lw2;
	final int nmin=nw2;
	final int nmax=dimY-nw2;
	final int mmin=mw2;
	final int mmax=dimX-mw2;
		
	final AtomicInteger ai = new AtomicInteger(lmin);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < lmax; superi = ai.getAndIncrement()) {
				int z = superi;
				for (int y=nmin;y<nmax;y++)
				{
					int index=0;
					int [] t= new int[lnm];
					int [] hist= new int[nbhist];
					int g;
					for (int k=0,zz=z-lw2;k<lw;k++,zz++)
					{
						ImageProcessor ip = st.getProcessor(zz+1);
						for (int j=0,yy=y-nw2;j<nw;j++,yy++)
						{
							for (int i=0,xx=0;i<mw;i++,xx++)
							{
								g=ip.getPixel(xx,yy);
								t[index++]=g;
								hist[g]++;
							}
						}
					}
					sort(t);
					int mdn=t[lnm2];
					ImageProcessor ip2=stm.getProcessor(z+1);
					ip2.set(mmin,y,(int)(mdn));
					
					int ltmdn=0;
					for(int i=0;i<mdn;i++) ltmdn+=hist[i];
					
					for(int x=mmin+1;x<mmax;x++)
					{
						int xxleft=x-mw2-1;
						int xxright=x+mw2;
						for(int k=0,zz=z-lw2;k<lw;k++,zz++)
						{
							ImageProcessor ip = st.getProcessor(zz+1);
							for(int j=0,yy=y-nw2;j<nw;j++,yy++)
							{
								g=ip.getPixel(xxleft,yy);
								hist[g]--;
								if(g<mdn) ltmdn--;
								g=ip.getPixel(xxright,yy);
								hist[g]++;
								if(g<mdn) ltmdn++;
							}
						}
						if (ltmdn>lnm2)
						{
							do
							{
								mdn--;
								ltmdn -=hist[mdn];
							}
							while(ltmdn>lnm2);
						}
						else
						{
							while(ltmdn+hist[mdn]<=lnm2)
							{
								ltmdn+=hist[mdn];
								mdn++;
							}
						}
						ip2.set(x,y,(int)(mdn));
					}
				}
			}
		}
		};
	}
	startAndJoin(threads);
}

void borderk(final ImageStack st, final ImageStack stm, final int d)
{
    final int dimX = st.getWidth();
	final int dimY = st.getHeight();
	final int dimZ = st.getSize();
	final AtomicInteger ai = new AtomicInteger(0);
	final AtomicInteger ai2 = new AtomicInteger(dimZ-d);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < d; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip2=stm.getProcessor(k+1);
				for(int j=0;j<dimY;j++)
					{
						for(int i=0;i<dimX;j++)
						{
							int imin = i-d;
							int imax = i+d;
							int jmin = j-d;
							int jmax = j+d;
							int kmin = k-d;
							int kmax = k+d;
							while (imin < 0) imin++;
							while (jmin < 0) jmin++;
							while (kmin < 0) kmin++;
							while (imax > dimX - 1) imax--;
							while (jmax > dimY - 1) jmax--;
							while (kmax > dimZ - 1) kmax--;
							int vox=0;
							int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
							int[] tab=new int[nbvox];
							for(int ii=imin;ii<=imax;ii++)
							{
								for(int jj=jmin;jj<=jmax;jj++)
								{
									for(int kk=kmin;kk<=kmax;kk++)
									{
										tab[vox]= (int)(st.getProcessor(kk+1).getPixel(ii,jj));
										vox++;
									}
								}
							}
							sort(tab);
					        int num=nbvox/2;
							ip2.set(i,j,(int)(tab[num]));
	
						}
					}
			}
			
			for (int superi = ai2.getAndIncrement(); superi < dimZ; superi = ai2.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip2=stm.getProcessor(k+1);
				for(int j=0;j<dimY;j++)
					{
						for(int i=0;i<dimX;j++)
						{
							int imin = i-d;
							int imax = i+d;
							int jmin = j-d;
							int jmax = j+d;
							int kmin = k-d;
							int kmax = k+d;
							while (imin < 0) imin++;
							while (jmin < 0) jmin++;
							while (kmin < 0) kmin++;
							while (imax > dimX - 1) imax--;
							while (jmax > dimY - 1) jmax--;
							while (kmax > dimZ - 1) kmax--;
							int vox=0;
							int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
					        int[] tab=new int[nbvox];
							for(int ii=imin;ii<=imax;ii++)
							{
								for(int jj=jmin;jj<=jmax;jj++)
								{
									for(int kk=kmin;kk<=kmax;kk++)
									{
										tab[vox] = (int)(st.getProcessor(kk+1).getPixel(ii,jj));
										vox++;
									}
								}
							}
							sort(tab);
					        int num=nbvox/2;
							ip2.set(i,j,(int)(tab[num]));
	
						}
					}
			}
   
		}		
		};
	}
	startAndJoin(threads);
}

void borderj(final ImageStack st, final ImageStack stm, final int d)
{
    final int dimX = st.getWidth();
	final int dimY = st.getHeight();
	final int dimZ = st.getSize();
	final AtomicInteger ai = new AtomicInteger(0);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip2=stm.getProcessor(k+1);
				for(int j=0;j<d;j++)
					{
						for(int i=0;i<dimX;j++)
						{
							int imin = i-d;
							int imax = i+d;
							int jmin = j-d;
							int jmax = j+d;
							int kmin = k-d;
							int kmax = k+d;
							while (imin < 0) imin++;
							while (jmin < 0) jmin++;
							while (kmin < 0) kmin++;
							while (imax > dimX - 1) imax--;
							while (jmax > dimY - 1) jmax--;
							while (kmax > dimZ - 1) kmax--;
							int vox=0;
							int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
					        int[] tab=new int[nbvox];
							for(int ii=imin;ii<=imax;ii++)
							{
								for(int jj=jmin;jj<=jmax;jj++)
								{
									for(int kk=kmin;kk<=kmax;kk++)
									{
										tab[vox] = (int)(st.getProcessor(kk+1).getPixel(ii,jj));
										vox++;
									}
								}
							}
							sort(tab);
					        int num=nbvox/2;
							ip2.set(i,j,(int)(tab[num]));
	
						}
					}
				for(int j=dimY-d;j<dimY;j++)
				{
					for(int i=0;i<dimX;j++)
					{
						int imin = i-d;
						int imax = i+d;
						int jmin = j-d;
						int jmax = j+d;
						int kmin = k-d;
						int kmax = k+d;
						while (imin < 0) imin++;
						while (jmin < 0) jmin++;
						while (kmin < 0) kmin++;
						while (imax > dimX - 1) imax--;
						while (jmax > dimY - 1) jmax--;
						while (kmax > dimZ - 1) kmax--;
						int vox=0;
						int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
				        int[] tab=new int[nbvox];
						for(int ii=imin;ii<=imax;ii++)
						{
							for(int jj=jmin;jj<=jmax;jj++)
							{
								for(int kk=kmin;kk<=kmax;kk++)
								{
									tab[vox] = (int)(st.getProcessor(kk+1).getPixel(ii,jj));
									vox++;
								}
							}
						}
						sort(tab);
				        int num=nbvox/2;
						ip2.set(i,j,(int)(tab[num]));
					}
				}
			}
		}		
		};
	}
	startAndJoin(threads);
}

void borderi(final ImageStack st, final ImageStack stm, final int d)
{
    final int dimX = st.getWidth();
	final int dimY = st.getHeight();
	final int dimZ = st.getSize();
	final AtomicInteger ai = new AtomicInteger(0);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip2=stm.getProcessor(k+1);
				for(int j=0;j<dimY;j++)
					{
						for(int i=0;i<d;j++)
						{
							int imin = i-d;
							int imax = i+d;
							int jmin = j-d;
							int jmax = j+d;
							int kmin = k-d;
							int kmax = k+d;
							while (imin < 0) imin++;
							while (jmin < 0) jmin++;
							while (kmin < 0) kmin++;
							while (imax > dimX - 1) imax--;
							while (jmax > dimY - 1) jmax--;
							while (kmax > dimZ - 1) kmax--;
							int vox=0;
							int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
					        int[] tab=new int[nbvox];
							for(int ii=imin;ii<=imax;ii++)
							{
								for(int jj=jmin;jj<=jmax;jj++)
								{
									for(int kk=kmin;kk<=kmax;kk++)
									{
										tab[vox] = (int)(st.getProcessor(kk+1).getPixel(ii,jj));
										vox++;
									}
								}
							}
							sort(tab);
					        int num=nbvox/2;
							ip2.set(i,j,(int)(tab[num]));
	
						}
						for(int i=dimX-d;i<dimX;j++)
						{
							int imin = i-d;
							int imax = i+d;
							int jmin = j-d;
							int jmax = j+d;
							int kmin = k-d;
							int kmax = k+d;
							while (imin < 0) imin++;
							while (jmin < 0) jmin++;
							while (kmin < 0) kmin++;
							while (imax > dimX - 1) imax--;
							while (jmax > dimY - 1) jmax--;
							while (kmax > dimZ - 1) kmax--;
							int vox=0;
							int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
					        int[] tab=new int[nbvox];
							for(int ii=imin;ii<=imax;ii++)
							{
								for(int jj=jmin;jj<=jmax;jj++)
								{
									for(int kk=kmin;kk<=kmax;kk++)
									{
										tab[vox] = (int)(st.getProcessor(kk+1).getPixel(ii,jj));
										vox++;
									}
								}
							}
							sort(tab);
					        int num=nbvox/2;
							ip2.set(i,j,(int)(tab[num]));
	
						}
					}
				
			}
		}		
		};
	}
	startAndJoin(threads);
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
	

void QuickSort(int a[], int lo0, int hi0) /* throws Exception */
{
   int lo = lo0;
   int hi = hi0;
   int mid;

   // pause for redraw
  // pause(lo, hi);
   if ( hi0 > lo0)
   {

      /* Arbitrarily establishing partition element as the midpoint of
       * the array.
       */
      mid = a[ ( lo0 + hi0 ) / 2 ];

      // loop through the array until indices cross
      while( lo <= hi )
      {
         /* find the first element that is greater than or equal to 
          * the partition element starting from the left Index.
          */
         while( ( lo < hi0 ) && ( a[lo] < mid ) )
            ++lo;

         /* find an element that is smaller than or equal to 
          * the partition element starting from the right Index.
          */
         while( ( hi > lo0 ) && ( a[hi] > mid ) )
            --hi;

         // if the indexes have not crossed, swap
         if( lo <= hi ) 
         {
            swap(a, lo, hi);
            // pause
            //pause();

            ++lo;
            --hi;
         }
      }

      /* If the right index has not reached the left side of array
       * must now sort the left partition.
       */
      if( lo0 < hi )
         QuickSort( a, lo0, hi );

      /* If the left index has not reached the right side of array
       * must now sort the right partition.
       */
      if( lo < hi0 )
         QuickSort( a, lo, hi0 );

   }
}


private void swap(int a[], int i, int j)
{
   int T;
   T = a[i]; 
   a[i] = a[j];
   a[j] = T;

}

public void sort(int a[]) /* throws Exception */
{
   QuickSort(a, 0, a.length - 1);
}

public static ImageStack newShortEmpty(ImageStack src) {
    int xSize = src.getWidth();
    int ySize = src.getHeight();
    int zSize = src.getSize();

    ImageStack dest = new ImageStack(xSize, ySize);
    for (int z = 1; z <= zSize; ++z) {
      dest.addSlice(src.getSliceLabel(z),
          new ShortProcessor(xSize, ySize));
    }

    dest.setColorModel(src.getColorModel());

    return dest;
  }

public static ImageStack newByteEmpty(ImageStack src) {
    int xSize = src.getWidth();
    int ySize = src.getHeight();
    int zSize = src.getSize();

    ImageStack dest = new ImageStack(xSize, ySize);
    for (int z = 1; z <= zSize; ++z) {
      dest.addSlice(src.getSliceLabel(z),
          new ByteProcessor(xSize, ySize));
    }

    dest.setColorModel(src.getColorModel());

    return dest;
  }


}

