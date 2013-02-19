/**
 * median 3D 
 * version v1.1 - 06/09/2012
 * Pierre Lhuissier - Luc Salvo
 * SIMaP/GPM2 - Grenoble University - CNRS - France
 */


import java.util.concurrent.atomic.AtomicInteger;

import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;



public class median_3D_fast implements PlugInFilter {

	protected ImageStack stack,stack2;
	protected final Thread[] threads= newThreadArray();
	int countervalue=0;
	int nb;
	static double valPixel=1;
	int nbhist;
	
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_8G+DOES_16+STACK_REQUIRED;
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


void stacktovol (ImageStack stack, byte[][][] vol,int dim1,int dim2, int dim3)
	{
		for (int k=1;k<=dim3;k++)
		 {
			byte[] pixels = (byte[]) stack.getPixels(k);
			IJ.showStatus("Pre-processing volume ...: "+k+"/"+dim3);
			int m=-1;
			for(int  i=0;i<dim1;i++)
			for(int  j=0;j<dim2;j++)			
			{
            m++;
			int pix = pixels[m] & 0xff;
            vol[k-1][j][i] =(byte)(pix);
			}
		}
	}


void voltostack(byte[][][] vol,ImageStack stack2,int dim1,int dim2, int dim3)
{
	for (int k=1;k<=dim3;k++)
	{
		IJ.showStatus("Stacking ...: "+k+"/"+dim3);
		byte[] pixels = new byte[dim1*dim2];	
		int m=-1;
		for( int i=0;i<dim1;i++)
		for(int  j=0;j<dim2;j++)			
		{
            m++;
			pixels[m]=vol[k-1][j][i] ;
			}
			stack2.addSlice("",pixels);
		}

	}


void stacktovol (ImageStack stack, short[][][] vol,int dim1,int dim2, int dim3)
{
	for (int k=1;k<=dim3;k++)
	 {
		short[] pixels = (short[]) stack.getPixels(k);
		IJ.showStatus("Pre-processing volume ...: "+k+"/"+dim3);
		int m=-1;
		for(int  i=0;i<dim1;i++)
		for(int  j=0;j<dim2;j++)			
		{
        m++;
		int pix = pixels[m] & 0xffff;
        vol[k-1][j][i] =(short)(pix);
		}
	}
}


void voltostack(short[][][] vol,ImageStack stack2,int dim1,int dim2, int dim3)
{
for (int k=1;k<=dim3;k++)
{
	IJ.showStatus("Stacking ...: "+k+"/"+dim3);
	short[] pixels = new short[dim1*dim2];	
	int m=-1;
	for( int i=0;i<dim1;i++)
	for(int  j=0;j<dim2;j++)			
	{
        m++;
		pixels[m]=vol[k-1][j][i] ;
		}
		stack2.addSlice("",pixels);
	}

}

int counter()
{
	countervalue++;
	return countervalue;
}

void treatment(final byte[][][] m1,final int dim1,final int dim2,final int dim3,final byte[][][] m2,int d)
{
	final int lw=2*d+1;
	final int nw=2*d+1;
	final int mw=2*d+1;
	final int lnm=lw*nw*mw;
	final int lnm2=lnm/2;
	final int lw2=lw/2;
	final int nw2=nw/2;
	final int mw2=mw/2;
	final int lmin=lw2;
	final int lmax=dim3-lw2;
	final int nmin=nw2;
	final int nmax=dim2-nw2;
	final int mmin=mw2;
	final int mmax=dim1-mw2;
		
	final AtomicInteger ai = new AtomicInteger(lmin);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < lmax; superi = ai.getAndIncrement()) {
				int z = superi;
				int g;
				int val = (int)((100*counter())/lmax);
			    IJ.showStatus("3D median filering in progress "+val+'%');
				for (int y=nmin;y<nmax;y++)
				{
int index=0;
int [] t= new int[lnm];
int [] hist= new int[256];
for (int k=0,zz=z-lw2;k<lw;k++,zz++)
for (int j=0,yy=y-nw2;j<nw;j++,yy++)
for (int i=0,xx=0;i<mw;i++,xx++)
{
t[index++]=g=m1[zz][yy][xx] & 0xff;
hist[g]++;
}
sort(t);
int mdn=t[lnm2];
m2[z][y][mmin]=(byte)mdn;

int ltmdn=0;
for(int i=0;i<mdn;i++) ltmdn+=hist[i];

for(int x=mmin+1;x<mmax;x++)
{
int xxleft=x-mw2-1;
int xxright=x+mw2;
for ( int k=0,zz=z-lw2;k<lw;k++,zz++)
for (int j=0,yy=y-nw2;j<nw;j++,yy++)
{
g=m1[zz][yy][xxleft] & 0xff;
hist[g]--;
if(g<mdn) ltmdn--;
g=m1[zz][yy][xxright] & 0xff;
hist[g]++;
if(g<mdn) ltmdn++;
}

if (ltmdn>lnm2)
do
{
mdn--;
ltmdn -=hist[mdn];
}
while(ltmdn>lnm2);

else
while(ltmdn+hist[mdn]<=lnm2)
{
ltmdn+=hist[mdn];
mdn++;
}
m2[z][y][x]=(byte) mdn;
}
				}
			}
		}
		};
	}
	startAndJoin(threads);
}


void treatment(final short[][][] m1,final int dim1,final int dim2,final int dim3,final short[][][] m2,int d)
{
	final int lw=2*d+1;
	final int nw=2*d+1;
	final int mw=2*d+1;
	final int lnm=lw*nw*mw;
	final int lnm2=lnm/2;
	final int lw2=lw/2;
	final int nw2=nw/2;
	final int mw2=mw/2;
	final int lmin=lw2;
	final int lmax=dim3-lw2;
	final int nmin=nw2;
	final int nmax=dim2-nw2;
	final int mmin=mw2;
	final int mmax=dim1-mw2;
		
	final AtomicInteger ai = new AtomicInteger(lmin);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < lmax; superi = ai.getAndIncrement()) {
				int z = superi;
				int g;
				int val = (int)((100*counter())/lmax);
			    IJ.showStatus("3D median filering in progress "+val+'%');
				for (int y=nmin;y<nmax;y++)
				{
int index=0;
int [] t= new int[lnm];
int [] hist= new int[nbhist];
for (int k=0,zz=z-lw2;k<lw;k++,zz++)
for (int j=0,yy=y-nw2;j<nw;j++,yy++)
for (int i=0,xx=0;i<mw;i++,xx++)
{
t[index++]=g=m1[zz][yy][xx] & 0xff;
hist[g]++;
}
sort(t);
int mdn=t[lnm2];
m2[z][y][mmin]=(byte)mdn;

int ltmdn=0;
for(int i=0;i<mdn;i++) ltmdn+=hist[i];

for(int x=mmin+1;x<mmax;x++)
{
int xxleft=x-mw2-1;
int xxright=x+mw2;
for ( int k=0,zz=z-lw2;k<lw;k++,zz++)
for (int j=0,yy=y-nw2;j<nw;j++,yy++)
{
g=m1[zz][yy][xxleft] & 0xffff;
hist[g]--;
if(g<mdn) ltmdn--;
g=m1[zz][yy][xxright] & 0xffff;
hist[g]++;
if(g<mdn) ltmdn++;
}

if (ltmdn>lnm2)
do
{
mdn--;
ltmdn -=hist[mdn];
}
while(ltmdn>lnm2);

else
while(ltmdn+hist[mdn]<=lnm2)
{
ltmdn+=hist[mdn];
mdn++;
}
m2[z][y][x]=(short) mdn;
}
				}
			}
		}
		};
	}
	startAndJoin(threads);
}


void borderk(final byte[][][] m1,final int dim1,final int dim2,final int dim3,final byte[][][] m2,final int d)
{

    
    final AtomicInteger ai = new AtomicInteger(0);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < d; superi = ai.getAndIncrement()) {
				int k = superi;
				int pix;

      for(int j=0;j<dim2;j=j+1)
      {
        for(int i=0;i<dim1;i=i+1)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(byte) pix;
	
        }
     }
     }
}
};
}
startAndJoin(threads);



final AtomicInteger ai2 = new AtomicInteger(dim3-d);

for (int ithread = 0; ithread < threads.length; ithread++) {

    // Concurrently run in as many threads as CPUs

	threads[ithread] = new Thread() {

	public void run() 
	{	
		for (int superi = ai2.getAndIncrement(); superi < dim3; superi = ai2.getAndIncrement()) {
			int k = superi;
			int pix;
   for(int j=0;j<dim2;j=j+1)
      {
        for(int i=0;i<dim1;i=i+1)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(byte) pix;
	
        }
     }
		}
	}
	};
	}
	startAndJoin(threads);
	}

void borderj(final byte[][][] m1,final int dim1,final int dim2,final int dim3,final byte[][][] m2,final int d)
{
   
    final AtomicInteger ai = new AtomicInteger(0);
  	  
    	for (int ithread = 0; ithread < threads.length; ithread++) {

    	    // Concurrently run in as many threads as CPUs

    		threads[ithread] = new Thread() {
    	
    		public void run() 
    		{	
    			for (int superi = ai.getAndIncrement(); superi < dim3; superi = ai.getAndIncrement()) {
    				int k = superi;
    				int pix;
    				for(int j=0;j<dim2;j++)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(byte) pix;
	
        }
     }
     }
    		}
    		};
    		}
    		startAndJoin(threads);

   
   final AtomicInteger ai2 = new AtomicInteger(0);
    		  	  
    for (int ithread = 0; ithread < threads.length; ithread++) {

    	    	    // Concurrently run in as many threads as CPUs

    threads[ithread] = new Thread() {
    	    	
    public void run() 
    {	
     for (int superi = ai2.getAndIncrement(); superi < dim3; superi = ai2.getAndIncrement()) {
    	    				int k = superi;
    			int pix;
      for(int j=dim2-d;j<dim2;j++)
      {
        for(int i=0;i<dim1;i=i+1)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(byte) pix;
	
        }
     }
     }
}

};
}
startAndJoin(threads);
}

void borderi(final byte[][][] m1,final int dim1,final int dim2,final int dim3,final byte[][][] m2,final int d)

{
	final AtomicInteger ai = new AtomicInteger(0);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dim3; superi = ai.getAndIncrement()) {
				int k = superi;
				int pix;
      for(int j=0;j<dim2;j=j+1)
      {
        for(int i=0;i<d;i++)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(byte) pix;
	
        }
     }
     }
		}

		};
		}
		startAndJoin(threads);
		
		final AtomicInteger ai2 = new AtomicInteger(0);
	  	  
	    for (int ithread = 0; ithread < threads.length; ithread++) {

	    	    	    // Concurrently run in as many threads as CPUs

	    threads[ithread] = new Thread() {
	    	    	
	    public void run() 
	    {	
	     for (int superi = ai2.getAndIncrement(); superi < dim3; superi = ai2.getAndIncrement()) {
	    	    				int k = superi;
	    			int pix;
      for(int j=0;j<dim2;j=j+1)
      {
        for(int i=dim1-d;i<dim1;i++)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(byte) pix;
	
        }
     }
     }
		}

		};
		}
		startAndJoin(threads);
}







void borderk(final short[][][] m1,final int dim1,final int dim2,final int dim3,final short[][][] m2,final int d)
{

    
    final AtomicInteger ai = new AtomicInteger(0);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < d; superi = ai.getAndIncrement()) {
				int k = superi;
				int pix;

      for(int j=0;j<dim2;j=j+1)
      {
        for(int i=0;i<dim1;i=i+1)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xffff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(short) pix;
	
        }
     }
     }
}
};
}
startAndJoin(threads);



final AtomicInteger ai2 = new AtomicInteger(dim3-d);

for (int ithread = 0; ithread < threads.length; ithread++) {

    // Concurrently run in as many threads as CPUs

	threads[ithread] = new Thread() {

	public void run() 
	{	
		for (int superi = ai2.getAndIncrement(); superi < dim3; superi = ai2.getAndIncrement()) {
			int k = superi;
			int pix;
   for(int j=0;j<dim2;j=j+1)
      {
        for(int i=0;i<dim1;i=i+1)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xffff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(short) pix;
	
        }
     }
		}
	}
	};
	}
	startAndJoin(threads);
	}

void borderj(final short[][][] m1,final int dim1,final int dim2,final int dim3,final short[][][] m2,final int d)
{
   
    final AtomicInteger ai = new AtomicInteger(0);
  	  
    	for (int ithread = 0; ithread < threads.length; ithread++) {

    	    // Concurrently run in as many threads as CPUs

    		threads[ithread] = new Thread() {
    	
    		public void run() 
    		{	
    			for (int superi = ai.getAndIncrement(); superi < dim3; superi = ai.getAndIncrement()) {
    				int k = superi;
    				int pix;
    				for(int j=0;j<dim2;j++)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xffff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(short) pix;
	
        }
     }
     }
    		}
    		};
    		}
    		startAndJoin(threads);

   
   final AtomicInteger ai2 = new AtomicInteger(0);
    		  	  
    for (int ithread = 0; ithread < threads.length; ithread++) {

    	    	    // Concurrently run in as many threads as CPUs

    threads[ithread] = new Thread() {
    	    	
    public void run() 
    {	
     for (int superi = ai2.getAndIncrement(); superi < dim3; superi = ai2.getAndIncrement()) {
    	    				int k = superi;
    			int pix;
      for(int j=dim2-d;j<dim2;j++)
      {
        for(int i=0;i<dim1;i=i+1)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xffff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(short) pix;
	
        }
     }
     }
}

};
}
startAndJoin(threads);
}

void borderi(final short[][][] m1,final int dim1,final int dim2,final int dim3,final short[][][] m2,final int d)

{
	final AtomicInteger ai = new AtomicInteger(0);
	  
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dim3; superi = ai.getAndIncrement()) {
				int k = superi;
				int pix;
      for(int j=0;j<dim2;j=j+1)
      {
        for(int i=0;i<d;i++)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xffff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(short) pix;
	
        }
     }
     }
		}

		};
		}
		startAndJoin(threads);
		
		final AtomicInteger ai2 = new AtomicInteger(0);
	  	  
	    for (int ithread = 0; ithread < threads.length; ithread++) {

	    	    	    // Concurrently run in as many threads as CPUs

	    threads[ithread] = new Thread() {
	    	    	
	    public void run() 
	    {	
	     for (int superi = ai2.getAndIncrement(); superi < dim3; superi = ai2.getAndIncrement()) {
	    	    				int k = superi;
	    			int pix;
      for(int j=0;j<dim2;j=j+1)
      {
        for(int i=dim1-d;i<dim1;i++)
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
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        int vox=0;
        int vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(int ii=imin;ii<=imax;ii++)
          for(int jj=jmin;jj<=jmax;jj++)
            for(int kk=kmin;kk<=kmax;kk++)
	    {
	      tab[vox]= (int)(m1[kk][jj][ii] & 0xffff);
	      vox++;
	    }
        sort(tab);
        int num=nbvox/2;
        pix=tab[num];
        m2[k][j][i]=(short) pix;
	
        }
     }
     }
		}

		};
		}
		startAndJoin(threads);
}











	public void run(ImageProcessor ip) {

		int dimz =stack.getSize();
		int dimy = stack.getWidth();
		int dimx = stack.getHeight();


		GenericDialog dia = new GenericDialog("3D labeling:", IJ.getInstance());	
		dia.addNumericField("size for median filter", valPixel, 0);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		int taille = (int) dia.getNextNumber();

		if(stack.getProcessor(1) instanceof ShortProcessor)
		{
			nbhist=65536;
			short[][][] vol = new short [dimz][dimy][dimx];	
			short[][][] mean = new short [dimz][dimy][dimx];	
			stacktovol(stack,vol,dimx,dimy,dimz);
			treatment(vol,dimx,dimy,dimz,mean,taille);
			borderk(vol,dimx,dimy,dimz,mean,taille);
			borderi(vol,dimx,dimy,dimz,mean,taille);
			vol=null;
			ImageStack stack2 = new ImageStack(stack.getWidth(), stack.getHeight());
			voltostack(mean,stack2,dimx,dimy,dimz);		
			new ImagePlus("median3D", stack2).show();
			mean=null;
		}
		else
		{
			nbhist=256;
			byte[][][] vol = new byte [dimz][dimy][dimx];	
			byte[][][] mean = new byte [dimz][dimy][dimx];	
			stacktovol(stack,vol,dimx,dimy,dimz);
			treatment(vol,dimx,dimy,dimz,mean,taille);
			borderk(vol,dimx,dimy,dimz,mean,taille);
			borderj(vol,dimx,dimy,dimz,mean,taille);
			borderi(vol,dimx,dimy,dimz,mean,taille);
			vol=null;
			ImageStack stack2 = new ImageStack(stack.getWidth(), stack.getHeight());
			voltostack(mean,stack2,dimx,dimy,dimz);		
			new ImagePlus("median3D", stack2).show();
			mean=null;
		}
		
		


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
	

}

