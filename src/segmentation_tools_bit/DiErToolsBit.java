package segmentation_tools_bit;

/*
 * Fast Distance Map  
 *
 *@author     Sofiane Terzi / Pierre Lhuissier
 *@created    October 16, 2008
 *@version    $1.0 $
*/


import ij.*;
import ij.process.ByteProcessor;

import java.awt.image.ColorModel;
import java.util.concurrent.atomic.AtomicInteger;



public class DiErToolsBit{
	
	protected int dimX;
	protected int dimY;
	protected int dimZ;
	protected int nX;
	protected int S;
	protected int val=255;
	protected int lg =64;
	protected int off;
	protected long[] data;
	protected ColorModel cm;
	protected final Thread[] threads;

	/**  Default constructor*/
	public DiErToolsBit(ImageStack stack) {
		dimZ = stack.getSize();
		dimX = stack.getWidth();
		dimY = stack.getHeight();
		off = dimX % lg; 
		if( off > 0)
		{
			nX=(dimX - off)/lg+1;
		}
		else
		{
			nX=dimX/lg;
		}
		S=nX*dimY*dimZ;
		data = readLong(stack);
		threads = newThreadArray();
	}

	
/*************************** Bit conversion ********************************/	
	public long readOneLong(byte[] pix) 
	{
			long l = 0x00000000000000000000L;
			long[] ll = new long[lg];
			for(int i = 0; i<64; i++)
			{
				ll[i] = ((long)(pix[i] & 0x01) << (lg -1 -i));
				l |= ll[i];
			}
			return l;
	}
		
	public byte[] writeOneLong(long l)
		{
			byte[] pix= new byte[lg];
			for(int i = 0; i<lg; i++)
			{
				pix[i] = (byte)(255*((l >> (lg -1 -i)) & 0x01));
			}
			return pix;
		}
		
	public void setStack(ImageStack stack)
		{
			data=readLong(stack);
		}
		
	public ImageStack getStack()
		{
			return writeLong(data);
		}
		
	public int getSize()
		{
			return data.length;
		}
		
	public void setData(long[] data_in)
		{
			this.data = null;
			this.data = new long[data_in.length];
			for(int i=0; i<data_in.length; i++){this.data[i]=data_in[i];}
		}
		
	public long[] getData()
		{
			long[] data_out = new long[this.data.length];
			for(int i=0; i<this.data.length; i++){data_out[i]=this.data[i];}
			return data_out;
		}
		
	long[] readLong(final ImageStack st)
		{
			int n=0;
			long[] m = new long[S];
			byte[] pixloc= new byte[lg];
			for(int i=1; i<=dimZ; i++)
			{
				byte[] pix=(byte[])st.getProcessor(i).getPixels();
				for(int j=0; j<dimY; j++)
				{
					for(int k=0; k<nX-1; k++)
					{
						int offset=j*dimX+lg*k;
						for(int l=0; l<lg; l++)
						{
							pixloc[l]=pix[offset+l];
						}
						m[n]=readOneLong(pixloc);
						n++;
					}
					
					for(int l =0; l<lg; l++)
					{
						int offset=j*dimX+lg*(nX-1);
						if(offset+l<pix.length)
						{
							pixloc[l]=pix[offset+l];
						}
						else
						{
							pixloc[l]=pix[pix.length-1];
						}	
						m[n]=readOneLong(pixloc);
					}
					n++;
				}
				pix = null;
			}
			pixloc=null;
			return m;
		}
		
	ImageStack writeLong(long[] m)
		{
			int n=0;
			ImageStack st = new ImageStack(dimX,dimY);
			for(int i=1; i<=dimZ; i++)
			{
				byte[] pix= new byte[dimX*dimY];
				for(int j=0; j<dimY; j++)
				{
					byte[] pixloc;
					for(int k=0; k<nX-1; k++)
					{
						pixloc= writeOneLong(m[n]);						
						int offset=j*dimX+lg*k;
						for(int l=0; l<lg; l++)
						{
							pix[offset+l]=pixloc[l];
						}
						n++;
					}
					int offset=j*dimX;
					pixloc= writeOneLong(m[n]);						
					for(int l=(nX-1)*lg; l<dimX; l++)
					{
						pix[offset+l]=pixloc[l-lg*(nX-1)];
					}
					n++;
				}
				ByteProcessor ip = new ByteProcessor(dimX, dimY, pix, cm);
				st.addSlice("", ip);
			}
			return st;
		}

	public ImageStack testRW(){
		return this.writeLong(data);
	}
	
	
	public void erode(int nbr, String element){
		String [] el = {"cube","oct"};
	
		//ImageStack stacke = stackTools.duplicate(stack);
			
		if(element==el[1])
		{
			for(int k = 0; k<nbr; ++k)
			{	
				data = StepErOct(data);
				System.gc();
//				data = StepErOctTest(data);
			}
		}
		else
		{
			for(int k = 0; k<nbr; ++k)
			{
				data = StepErCube(data);
			}
		}
		
	    }
		
		public void dilate(int nbr, String element){
		
	
		String [] el = {"cube","oct"};
	
		//ImageStack stacke = stackTools.duplicate(stack);
			
		if(element==el[1])
		{
			for(int k = 0; k<nbr; ++k)
			{	
				data = StepDiOct(data);
				System.gc();
//				data = StepDiOctTest(data);
			}
		}
		else
		{
			for(int k = 0; k<nbr; ++k)
			{
				data = StepDiCube(data);
			}
		}
	}

		public long[] StepErOctTest(final long[] data_in){	
			
			final long[] data_out = new long[data_in.length];
			
			final AtomicInteger ai = new AtomicInteger(0);
			
			for (int ithread = 0; ithread < threads.length; ithread++) {
			
			final int pitch = dimZ /threads.length; 
		    // Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
		
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi < threads.length; superi = ai.getAndIncrement())
				{
					int kk = superi;
					for(int k=kk*pitch; (k<(kk+1*pitch))&&(k<dimZ); k++)
					{
					long current,previous,next,in,out,up,down;
					for( int j=0; j<dimY; j++)
					{
						for( int i=0; i<nX; i++)
						{
							/*
							 current = data_in[i+j*nX+k*nX*dimY];
							if (i==0){ previous = (current << 1) | (current & 0x0000000000000001L);}
							else{ previous = (current << 1) | (data_in[(i-1)+j*nX+k*nX*dimY] >> (lg-1));}
							if (i==nX-1) { next = (current >> 1) | (current & 0x8000000000000000L);}
							else {next = (current >> 1) | (data_in[(i+1)+j*nX+k*nX*dimY] << (lg-1));}
							if (j==0){ up = current;}
							else {up = data_in[i+(j-1)*nX+k*nX*dimY];}
							if (j==dimY-1) {down = current;}
							else {down = data_in[i+(j+1)*nX+k*nX*dimY];}
							if (k==0) {out = current;}
							else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
							if (k==dimZ-1) {in = current;}
							else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
							data_out[i+j*nX+k*nX*dimY] = current & previous & next & up & down & in & out;
							 */
							current = data_in[i+j*nX+k*nX*dimY];
							if (i==0){ previous = ((current >> 1)& 0x7FFFFFFFFFFFFFFFL) | (current & 0x8000000000000000L);}
							else{ previous = ((current >> 1) & 0x7FFFFFFFFFFFFFFFL) | ((data_in[(i-1)+j*nX+k*nX*dimY] << (lg-1)) & 0x8000000000000000L);}
							if (i==nX-1) { next = ((current << 1) & 0xFFFFFFFFFFFFFFFEL) | (current & 0x0000000000000001L);}
							else {next = ((current << 1)  & 0xFFFFFFFFFFFFFFFEL) | ((data_in[(i+1)+j*nX+k*nX*dimY] >> (lg-1))& 0x0000000000000001L);}
							if (j==0){ up = current;}
							else {up = data_in[i+(j-1)*nX+k*nX*dimY];}
							if (j==dimY-1) {down = current;}
							else {down = data_in[i+(j+1)*nX+k*nX*dimY];}
							if (k==0) {out = current;}
							else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
							if (k==dimZ-1) {in = current;}
							else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
							data_out[i+j*nX+k*nX*dimY] = current & previous & next & up & down & in & out;	
						}
					}
					}
				}
			}
			};
			}
			startAndJoin(threads);
			
			return data_out;
		}
		
	public long[] StepErOct(final long[] data_in){	
		
		final long[] data_out = new long[data_in.length];
		
		final AtomicInteger ai = new AtomicInteger(0);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement())
			{
				int k = superi;
				long current,previous,next,in,out,up,down;
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{
						/*
						 current = data_in[i+j*nX+k*nX*dimY];
						if (i==0){ previous = (current << 1) | (current & 0x0000000000000001L);}
						else{ previous = (current << 1) | (data_in[(i-1)+j*nX+k*nX*dimY] >> (lg-1));}
						if (i==nX-1) { next = (current >> 1) | (current & 0x8000000000000000L);}
						else {next = (current >> 1) | (data_in[(i+1)+j*nX+k*nX*dimY] << (lg-1));}
						if (j==0){ up = current;}
						else {up = data_in[i+(j-1)*nX+k*nX*dimY];}
						if (j==dimY-1) {down = current;}
						else {down = data_in[i+(j+1)*nX+k*nX*dimY];}
						if (k==0) {out = current;}
						else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
						if (k==dimZ-1) {in = current;}
						else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
						data_out[i+j*nX+k*nX*dimY] = current & previous & next & up & down & in & out;
						 */
						current = data_in[i+j*nX+k*nX*dimY];
						if (i==0){ previous = ((current >> 1)& 0x7FFFFFFFFFFFFFFFL) | (current & 0x8000000000000000L);}
						else{ previous = ((current >> 1) & 0x7FFFFFFFFFFFFFFFL) | ((data_in[(i-1)+j*nX+k*nX*dimY] << (lg-1)) & 0x8000000000000000L);}
						if (i==nX-1) { next = ((current << 1) & 0xFFFFFFFFFFFFFFFEL) | (current & 0x0000000000000001L);}
						else {next = ((current << 1)  & 0xFFFFFFFFFFFFFFFEL) | ((data_in[(i+1)+j*nX+k*nX*dimY] >> (lg-1))& 0x0000000000000001L);}
						if (j==0){ up = current;}
						else {up = data_in[i+(j-1)*nX+k*nX*dimY];}
						if (j==dimY-1) {down = current;}
						else {down = data_in[i+(j+1)*nX+k*nX*dimY];}
						if (k==0) {out = current;}
						else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
						if (k==dimZ-1) {in = current;}
						else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
						data_out[i+j*nX+k*nX*dimY] = current & previous & next & up & down & in & out;	
					}
				}
			}
		}
		};
		}
		startAndJoin(threads);
		
		return data_out;
	}
	
	public long[] StepErCube(final long[] data_in){	
		final long[] data_out = new long[data_in.length];
		final long[] data_1 = new long[data_in.length];
		
		final AtomicInteger ai = new AtomicInteger(0);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement())
			{
				int k = superi;
				long current,previous,next,up,down;
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{
						current = data_in[i+j*nX+k*nX*dimY];
						if (i==0){ previous = ((current >> 1)& 0x7FFFFFFFFFFFFFFFL) | (current & 0x8000000000000000L);}
						else{ previous = ((current >> 1) & 0x7FFFFFFFFFFFFFFFL) | ((data_in[(i-1)+j*nX+k*nX*dimY] << (lg-1)) & 0x8000000000000000L);}
						if (i==nX-1) { next = ((current << 1) & 0xFFFFFFFFFFFFFFFEL) | (current & 0x0000000000000001L);}
						else {next = ((current << 1)  & 0xFFFFFFFFFFFFFFFEL) | ((data_in[(i+1)+j*nX+k*nX*dimY] >> (lg-1))& 0x0000000000000001L);}
						data_1[i+j*nX+k*nX*dimY] = current & previous & next;
					}
				}
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{		
						current = data_1[i+j*nX+k*nX*dimY];
						if (j==0){ up = current;}
						else {up = data_1[i+(j-1)*nX+k*nX*dimY];}
						if (j==dimY-1) {down = current;}
						else {down = data_1[i+(j+1)*nX+k*nX*dimY];}
						data_in[i+j*nX+k*nX*dimY] = current & up & down;
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
			for (int superi = ai2.getAndIncrement(); superi < dimZ; superi = ai2.getAndIncrement())
			{
				int k = superi;
				long current,in,out;
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{
						current = data_in[i+j*nX+k*nX*dimY];
						if (k==0) {out = current;}
						else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
						if (k==dimZ-1) {in = current;}
						else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
						data_out[i+j*nX+k*nX*dimY] = current & in & out;
					}
				}
			}
		}
		};
		}
		startAndJoin(threads);
			
		return data_out;
	}
	

	public long[] StepDiOct(final long[] data_in){	
		
		final long[] data_out = new long[data_in.length];
		
		final AtomicInteger ai = new AtomicInteger(0);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement())
			{
				int k = superi;
				long current,previous,next,in,out,up,down;
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{
						current = data_in[i+j*nX+k*nX*dimY];
						if (i==0){ previous = ((current >> 1)& 0x7FFFFFFFFFFFFFFFL) | (current & 0x8000000000000000L);}
						else{ previous = ((current >> 1) & 0x7FFFFFFFFFFFFFFFL) | ((data_in[(i-1)+j*nX+k*nX*dimY] << (lg-1)) & 0x8000000000000000L);}
						if (i==nX-1) { next = ((current << 1) & 0xFFFFFFFFFFFFFFFEL) | (current & 0x0000000000000001L);}
						else {next = ((current << 1)  & 0xFFFFFFFFFFFFFFFEL) | ((data_in[(i+1)+j*nX+k*nX*dimY] >> (lg-1))& 0x0000000000000001L);}
						if (j==0){ up = current;}
						else {up = data_in[i+(j-1)*nX+k*nX*dimY];}
						if (j==dimY-1) {down = current;}
						else {down = data_in[i+(j+1)*nX+k*nX*dimY];}
						if (k==0) {out = current;}
						else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
						if (k==dimZ-1) {in = current;}
						else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
						data_out[i+j*nX+k*nX*dimY] = current | previous | next | up | down | in | out;
					}
				}
			}
		}
		};
		}
		startAndJoin(threads);
		
		return data_out;
	}

	public long[] StepDiOctTest(final long[] data_in){	
		
		final long[] data_out = new long[data_in.length];
		
		final AtomicInteger ai = new AtomicInteger(0);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

			
		final int pitch = dimZ /threads.length; 
		   // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
		
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < threads.length; superi = ai.getAndIncrement())
			{
				int kk = superi;
				for(int k=kk*pitch; (k<(kk+1*pitch))&&(k<dimZ); k++)
				{	
			
				long current,previous,next,in,out,up,down;
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{
						current = data_in[i+j*nX+k*nX*dimY];
						if (i==0){ previous = ((current >> 1)& 0x7FFFFFFFFFFFFFFFL) | (current & 0x8000000000000000L);}
						else{ previous = ((current >> 1) & 0x7FFFFFFFFFFFFFFFL) | ((data_in[(i-1)+j*nX+k*nX*dimY] << (lg-1)) & 0x8000000000000000L);}
						if (i==nX-1) { next = ((current << 1) & 0xFFFFFFFFFFFFFFFEL) | (current & 0x0000000000000001L);}
						else {next = ((current << 1)  & 0xFFFFFFFFFFFFFFFEL) | ((data_in[(i+1)+j*nX+k*nX*dimY] >> (lg-1))& 0x0000000000000001L);}
						if (j==0){ up = current;}
						else {up = data_in[i+(j-1)*nX+k*nX*dimY];}
						if (j==dimY-1) {down = current;}
						else {down = data_in[i+(j+1)*nX+k*nX*dimY];}
						if (k==0) {out = current;}
						else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
						if (k==dimZ-1) {in = current;}
						else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
						data_out[i+j*nX+k*nX*dimY] = current | previous | next | up | down | in | out;
					}
				}
				}
			}
		}
		};
		}
		startAndJoin(threads);
		
		return data_out;
	}
	
	public long[] StepDiCube(final long[] data_in){	
		final long[] data_out = new long[data_in.length];
		final long[] data_1 = new long[data_in.length];
		
		final AtomicInteger ai = new AtomicInteger(0);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement())
			{
				int k = superi;
				long current,previous,next,up,down;
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{
						current = data_in[i+j*nX+k*nX*dimY];
						if (i==0){ previous = ((current >> 1)& 0x7FFFFFFFFFFFFFFFL) | (current & 0x8000000000000000L);}
						else{ previous = ((current >> 1) & 0x7FFFFFFFFFFFFFFFL) | ((data_in[(i-1)+j*nX+k*nX*dimY] << (lg-1)) & 0x8000000000000000L);}
						if (i==nX-1) { next = ((current << 1) & 0xFFFFFFFFFFFFFFFEL) | (current & 0x0000000000000001L);}
						else {next = ((current << 1)  & 0xFFFFFFFFFFFFFFFEL) | ((data_in[(i+1)+j*nX+k*nX*dimY] >> (lg-1))& 0x0000000000000001L);}
						data_1[i+j*nX+k*nX*dimY] = current | previous | next;
					}
				}
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{		
						current = data_1[i+j*nX+k*nX*dimY];
						if (j==0){ up = current;}
						else {up = data_1[i+(j-1)*nX+k*nX*dimY];}
						if (j==dimY-1) {down = current;}
						else {down = data_1[i+(j+1)*nX+k*nX*dimY];}
						data_in[i+j*nX+k*nX*dimY] = current | up | down;
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
			for (int superi = ai2.getAndIncrement(); superi < dimZ; superi = ai2.getAndIncrement()) //Pierre 2011/10/06 superi<=dimZ
			{
				int k = superi;
				long current,in,out;
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<nX; i++)
					{
						current = data_in[i+j*nX+k*nX*dimY];
						if (k==0) {out = current;}
						else {out = data_in[i+j*nX+(k-1)*nX*dimY];}
						if (k==dimZ-1) {in = current;}
						else {in = data_in[i+j*nX+(k+1)*nX*dimY];}
						data_out[i+j*nX+k*nX*dimY] = current | in | out;
					}
				}
			}
		}
		};
		}
		startAndJoin(threads);
			
		return data_out;
	}

	public int pixelsCount(final long[] data_in){
		
		final AtomicInteger ai = new AtomicInteger(0);
		final int[] nb = new int[dimZ];
		
		for (int ithread = 0; ithread < threads.length; ithread++) {
		    // Concurrently run in as many threads as CPUs
			threads[ithread] = new Thread() {
			
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
					int k = superi;
					int nbr = 0;
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<nX-1;i++)
						{
							long current = data_in[k*nX*dimY + nX*j +i];
							for(int l=0; l<lg; l++)
							{
								nbr+= (int)((current >> l) & 0x0001);
							}
						}
						long current = data_in[k*nX*dimY + nX*(j+1)-1];
						for(int l=lg-1; l>lg-1-off; l--)
						{
							nbr+= (int)((current >> l) & 0x0001);
						}
					}
					nb[k] = nbr;
				}
			}
			};
			}
			startAndJoin(threads);
			int total=0;
			for(int i = 0; i<dimZ; ++i)
			{ total += nb[i];}
		return total;
	}
	
	public int pixelsCount(){
		
		final AtomicInteger ai = new AtomicInteger(0);
		final int[] nb = new int[dimZ];
		
		for (int ithread = 0; ithread < threads.length; ithread++) {
		    // Concurrently run in as many threads as CPUs
			threads[ithread] = new Thread() {
			
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
					int k = superi;
					int nbr = 0;
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<nX-1;i++)
						{
							long current = data[k*nX*dimY + nX*j +i];
							/**if((i==0)&&(j==3)&&(k==0))
							{
								IJ.showMessage("long="+(current >> (63) & 0x0000000000000001L)+(current >> (62) & 0x0000000000000001L)+(current >> (61) & 0x0000000000000001L)+(current >> (60) & 0x0000000000000001L)+(current >> (59) & 0x0000000000000001L)+(current >> (58) & 0x0000000000000001L)+"...");
							}*/
							for(int l=0; l<lg; l++)
							{
								nbr+= (int)((current >> l) & 0x0001);
							}
						}
						long current = data[k*nX*dimY + nX*(j+1)-1];
						for(int l=lg-1; l>lg-1-off; l--)
						{
							nbr+= (int)((current >> l) & 0x0001);
						}
					}
					nb[k] = nbr;
				}
			}
			};
			}
			startAndJoin(threads);
			int total=0;
			for(int i = 0; i<dimZ; ++i)
			{ total += nb[i];}
		return total;
	}
	
/*
	public ImageStack erodeExcept(ImageStack stack, int nbr, String element, double min, double max){
	

	String [] el = {"cube","oct"};

	//ImageStack stacke = stackTools.duplicate(stack);
		
	if(element==el[1])
	{
		for(int k = 0; k<nbr; ++k)
		{	
			stack = StepErOctExcept(stack, min, max);
		}
	}
	else
	{
		for(int k = 0; k<nbr; ++k)
		{
			stack = StepErCubeExcept(stack, min, max);
		}
	}
	
	return stack;
	
    }
	
	public ImageStack dilateExcept(ImageStack stack, int nbr, String element, double min, double max){
	

	String [] el = {"cube","oct"};

	ImageStack stacke = stackTools.duplicate(stack);
		
	if(element==el[1])
	{
		for(int k = 0; k<nbr; ++k)
		{	
			stacke = StepDiOctExcept(stacke, min, max);
		}
	}
	else
	{
		for(int k = 0; k<nbr; ++k)
		{
			stacke = StepDiCubeExcept(stacke, min, max);
		}
	}
	
	return stacke;
	
    }
	
	
	public ImageStack StepErOctExcept(final ImageStack stack1, final double min, final double max){	
		
		final ImageStack stack2 = stackTools.duplicateEmpty(stack1);
		final AtomicInteger ai = new AtomicInteger(1);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
				int current = superi;
				short[] pixels;
				short[] pixelsPlus;
				short[] pixelsMoins;
				pixels = (short[]) stack1.getProcessor(current).getPixelsCopy();
				short[] newpixels=new short[dimX*dimY];
				short[] pixelsSlice=new short[dimX*dimY];
				int pix1, pix2, pix3, m;
				
					//Mininmum de shiftx +1 et shiftx -1 		
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							if(i<dimX-1)
							{
								pix1 = pixels[m+1] & 0xffff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = pixels[m] & 0xffff;
							if(i>0)
							{
								pix3 = pixels[m-1] & 0xffff;
							}
							else
							{
								pix3 = val;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1<pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3<pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
					
					
					//Mininmum de shifty +1 et shifty -1
					for(int  i=0;i<dimX;i++)
					{
						for(int  j=0;j<dimY;j++)
						{
							m=j*(dimX)+i;
							if(j<dimY-1)
							{
								pix1 = pixels[m+dimX] & 0xffff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = newpixels[m] & 0xffff;
							if(j>0)
							{
								pix3 = pixels[m-dimX] & 0xffff;
							}
							else
							{
								pix3 = val;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1<pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3<pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
					
					if(current<dimZ)
					{
						pixelsPlus = (short[]) stack1.getProcessor(current+1).getPixelsCopy();
					}
					else
					{
						pixelsPlus = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsPlus[i] = (short)val;}
					}
					
					if(current>1)
					{
						pixelsMoins = (short[]) stack1.getProcessor(current-1).getPixelsCopy();
					}
					else
					{
						pixelsMoins = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsMoins[i] = (short)val;}
					}
					
					for (int i=0; i<dimX*dimY; ++i)
					{
						pix1 = (int) pixelsPlus[i] &0xffff;
						pix2 = (int) pixelsSlice[i] & 0xffff;
						pix3 = (int) pixelsMoins[i] & 0xffff;
						if ((pix2>min)&&(pix2<max))
						{
							pixelsSlice[i]=(short) pix2;
						}
						else
						{
							if ((pix1<pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
							if ((pix3<pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
							pixelsSlice[i]=(short) pix2;
						}
					}
				stack2.setPixels(pixelsSlice, current);
			}
		}
		};
		}
		
		startAndJoin(threads);
		
		return stack2;
	}
	
	public ImageStack StepErCubeExcept(final ImageStack stack1, final double min, final double max){
		final ImageStack stack2 = stackTools.duplicateEmpty(stack1);
		final ImageStack stack3 = stackTools.duplicateEmpty(stack1);
		final AtomicInteger ai = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
		
		public void run() 
		{	
		for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
			int current = superi;
			short[] pixels;
			
				pixels = (short[]) stack1.getProcessor(current).getPixelsCopy();
			
				short[] newpixels=new short[dimX*dimY];
				int pix1, pix2, pix3, m;
				
				
					//Mininmum de shiftx +1 et shiftx -1 		
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							if(i<dimX-1)
							{
								pix1 = pixels[m+1] & 0xffff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = pixels[m] & 0xffff;
							if(i>0)
							{
								pix3 = pixels[m-1] & 0xffff;
							}
							else
							{
								pix3 = val;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1<pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3<pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
					
					
					//Mininmum de shifty +1 et shifty -1
					for(int  i=0;i<dimX;i++)
					{
						for(int  j=0;j<dimY;j++)
						{
							m=j*(dimX)+i;
							if(j<dimY-1)
							{
								pix1 = newpixels[m+dimX] & 0xffff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = newpixels[m] & 0xffff;
							if(j>0)
							{
								pix3 = newpixels[m-dimX] & 0xffff;
							}
							else
							{
								pix3 = val;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1<pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3<pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
				
				stack2.setPixels(pixels, current);
			}
		}
		};
		}
		
		startAndJoin(threads);
		
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

		    threads[ithread] = new Thread() {

			public void run() {
			for (int superi = ai2.getAndIncrement(); superi <= dimZ; superi = ai2.getAndIncrement()) {
				int current = superi;
				int pix1, pix2, pix3;
				short[] pixelsPlus;
				short[] pixelsMoins;
				short[] pixels;
					if(current<dimZ)
					{
						pixelsPlus = (short[]) stack2.getProcessor(current+1).getPixelsCopy();
					}
					else
					{
						pixelsPlus = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsPlus[i] = (short)val;}
					}
					pixels = (short[]) stack2.getProcessor(current).getPixelsCopy();
					if(current>1)
					{
						pixelsMoins = (short[]) stack2.getProcessor(current-1).getPixelsCopy();
					}
					else
					{
						pixelsMoins = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsMoins[i] = (short)val;}
					}
					for (int i=0; i<dimX*dimY; ++i)
					{
						pix1 = (int) pixelsPlus[i] &0xffff;
						pix2 = (int) pixels[i] & 0xffff;
						pix3 = (int) pixelsMoins[i] & 0xffff;
						if ((pix2>min)&&(pix2<max))
						{
							pixels[i]=(short) pix2;
						}
						else
						{
							if ((pix1<pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
							if ((pix3<pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
							pixels[i]=(short) pix2;
						}
					}
				stack3.setPixels(pixels,current);
			}
			}
		    };
		}

		startAndJoin(threads);
		
		return stack3;
	}

	public ImageStack StepDiOctExcept(final ImageStack stack1, final double min, final double max){	
		
		final ImageStack stack2 = stackTools.duplicateEmpty(stack1);
		final AtomicInteger ai = new AtomicInteger(1);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
				int current = superi;
				short[] pixels;
				short[] pixelsPlus;
				short[] pixelsMoins;
				pixels = (short[]) stack1.getProcessor(current).getPixelsCopy();
				short[] newpixels=new short[dimX*dimY];
				short[] pixelsSlice=new short[dimX*dimY];
				int pix1, pix2, pix3, m;
				
					//Mininmum de shiftx +1 et shiftx -1 		
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							if(i<dimX-1)
							{
								pix1 = pixels[m+1] & 0xffff;
							}
							else
							{
								pix1 = 0;
							}
							pix2 = pixels[m] & 0xffff;
							if(i>0)
							{
								pix3 = pixels[m-1] & 0xffff;
							}
							else
							{
								pix3 = 0;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1>pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3>pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
					
					
					//Mininmum de shifty +1 et shifty -1
					for(int  i=0;i<dimX;i++)
					{
						for(int  j=0;j<dimY;j++)
						{
							m=j*(dimX)+i;
							if(j<dimY-1)
							{
								pix1 = pixels[m+dimX] & 0xffff;
							}
							else
							{
								pix1 = 0;
							}
							pix2 = newpixels[m] & 0xffff;
							if(j>0)
							{
								pix3 = pixels[m-dimX] & 0xffff;
							}
							else
							{
								pix3 = 0;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1>pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3>pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
					
					if(current<dimZ)
					{
						pixelsPlus = (short[]) stack1.getProcessor(current+1).getPixelsCopy();
					}
					else
					{
						pixelsPlus = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsPlus[i] = (short)0;}
					}
					
					if(current>1)
					{
						pixelsMoins = (short[]) stack1.getProcessor(current-1).getPixelsCopy();
					}
					else
					{
						pixelsMoins = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsMoins[i] = (short)0;}
					}
					
					for (int i=0; i<dimX*dimY; ++i)
					{
						pix1 = (int) pixelsPlus[i] &0xffff;
						pix2 = (int) pixelsSlice[i] & 0xffff;
						pix3 = (int) pixelsMoins[i] & 0xffff;
						if ((pix2>min)&&(pix2<max))
						{
							pixelsSlice[i]=(short) pix2;
						}
						else
						{
							if ((pix1>pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
							if ((pix3>pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
							pixelsSlice[i]=(short) pix2;
						}
					}
				stack2.setPixels(pixelsSlice, current);
			}
		}
		};
		}
		
		startAndJoin(threads);
		
		return stack2;
	}
	
	public ImageStack StepDiCubeExcept(final ImageStack stack1, final double min, final double max){
		final ImageStack stack2 = stackTools.duplicateEmpty(stack1);
		final ImageStack stack3 = stackTools.duplicateEmpty(stack1);
		final AtomicInteger ai = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
		
		public void run() 
		{	
		for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
			int current = superi;
			short[] pixels;
			
				pixels = (short[]) stack1.getProcessor(current).getPixelsCopy();
			
				short[] newpixels=new short[dimX*dimY];
				int pix1, pix2, pix3, m;
				
				
					//Mininmum de shiftx +1 et shiftx -1 		
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							if(i<dimX-1)
							{
								pix1 = pixels[m+1] & 0xffff;
							}
							else
							{
								pix1 = 0;
							}
							pix2 = pixels[m] & 0xffff;
							if(i>0)
							{
								pix3 = pixels[m-1] & 0xffff;
							}
							else
							{
								pix3 = 0;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1>pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3>pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
					
					
					//Mininmum de shifty +1 et shifty -1
					for(int  i=0;i<dimX;i++)
					{
						for(int  j=0;j<dimY;j++)
						{
							m=j*(dimX)+i;
							if(j<dimY-1)
							{
								pix1 = newpixels[m+dimX] & 0xffff;
							}
							else
							{
								pix1 = 0;
							}
							pix2 = newpixels[m] & 0xffff;
							if(j>0)
							{
								pix3 = newpixels[m-dimX] & 0xffff;
							}
							else
							{
								pix3 = 0;
							}
							if ((pix2>min)&&(pix2<max))
							{
								newpixels[m]=(short) pix2;
							}
							else
							{
								if ((pix1>pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
								if ((pix3>pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
								newpixels[m]=(short) pix2;
							}
						}
					}
				
				stack2.setPixels(pixels, current);
			}
		}
		};
		}
		
		startAndJoin(threads);
		
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

		    threads[ithread] = new Thread() {

			public void run() {
			for (int superi = ai2.getAndIncrement(); superi <= dimZ; superi = ai2.getAndIncrement()) {
				int current = superi;
				int pix1, pix2, pix3;
				short[] pixelsPlus;
				short[] pixelsMoins;
				short[] pixels;
					if(current<dimZ)
					{
						pixelsPlus = (short[]) stack2.getProcessor(current+1).getPixelsCopy();
					}
					else
					{
						pixelsPlus = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsPlus[i] = (short)0;}
					}
					pixels = (short[]) stack2.getProcessor(current).getPixelsCopy();
					if(current>1)
					{
						pixelsMoins = (short[]) stack2.getProcessor(current-1).getPixelsCopy();
					}
					else
					{
						pixelsMoins = new short[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsMoins[i] = (short)0;}
					}
					for (int i=0; i<dimX*dimY; ++i)
					{
						pix1 = (int) pixelsPlus[i] &0xffff;
						pix2 = (int) pixels[i] & 0xffff;
						pix3 = (int) pixelsMoins[i] & 0xffff;
						if ((pix2>min)&&(pix2<max))
						{
							pixels[i]=(short) pix2;
						}
						else
						{
							if ((pix1>pix2)&&((pix1<min)||(pix1>max))) pix2 = pix1;
							if ((pix3>pix2)&&((pix3<min)||(pix3>max))) pix2 = pix3;
							pixels[i]=(short) pix2;
						}
					}
				
				stack3.setPixels(pixels,current);
			}
			}
		    };
		}

		startAndJoin(threads);
		
		return stack3;
	}

*/	

	/*
	  long readOneLong(byte[] pix)
	 
	{
		long l = 0x00;
		long[] ll = new long[8];
		for(int i = 0; i<8; i++)
		{
			ll[i] = (long)(((0x80L & (int)pix[i]) + (0xFFL & (int)pix[i])) << (8*(7-i)));
			l |= ll[i];
		}
		return l;
	}
	
	byte[] writeOneLong(long l)
	{
		byte b;
		byte[] pix= new byte[8];
		b = (byte)((l & 0xFF00000000000000L) >> 56);
		pix[0]=(byte)(b + (b & 0x80));
		b = (byte)((l & 0x00FF000000000000L) >> 48);
		pix[1]=(byte)(b + (b & 0x80));
		b = (byte)((l & 0x0000FF0000000000L) >> 40);
		pix[2]=(byte)(b + (b & 0x80));
		b = (byte)((l & 0x000000FF00000000L) >> 32);
		pix[3]=(byte)(b + (b & 0x80));
		b = (byte)((l & 0x00000000FF000000L) >> 24);
		pix[4]=(byte)(b + (b & 0x80));
		b = (byte)((l & 0x0000000000FF0000L) >> 86);
		pix[5]=(byte)(b + (b & 0x80));
		b = (byte)((l & 0x000000000000FF00L) >> 8);
		pix[6]=(byte)(b + (b & 0x80));
		b = (byte)((l & 0x00000000000000FFL));
		pix[7]=(byte)(b + (b & 0x80));
		
		return pix;
	}
	
	long[] readLong(final ImageStack st)
	{
		int n=0;
		long[] m = new long[S];
		for(int i=1; i<=dimZ; i++)
		{
			byte[] pix=(byte[])st.getProcessor(i).getPixels();
			for(int j=0; j<dimY; j++)
			{
				for(int k=0; k<nX-1; k++)
				{
					byte[] pixloc= new byte[8];
					int offset=j*dimX+8*k;
					for(int l=0; l<8; l++)
					{
						pixloc[l]=pix[offset+l];
					}
					m[n]=readOneLong(pixloc);
					n++;
				}
				
				byte[] pixloc= new byte[8];
				for(int l =0; l<8; l++)
				{
					int offset=j*dimX+8*(nX-1);
					if(offset+l<pix.length)
					{
						pixloc[l]=pix[offset+l];
					}
					else
					{
						pixloc[l]=0;
					}	
					m[n]=readOneLong(pixloc);
				}
				n++;
			}
		}
		return m;
	}
	
	ImageStack writeLong(long[] m)
	{
		int n=0;
		ImageStack st = new ImageStack(dimX,dimY);
		for(int i=1; i<=dimZ; i++)
		{
			byte[] pix= new byte[dimX*dimY];
			for(int j=0; j<dimY; j++)
			{
				byte b;
				for(int k=0; k<nX-1; k++)
				{
						
						int offset=j*dimX+8*k;
						b = (byte)((m[n] & 0xFF00000000000000L) >> 56);
						pix[offset]=(byte)(b + (b & 0x80));
						b = (byte)((m[n] & 0x00FF000000000000L) >> 48);
						pix[offset+1]=(byte)(b + (b & 0x80));
						b = (byte)((m[n] & 0x0000FF0000000000L) >> 40);
						pix[offset+2]=(byte)(b + (b & 0x80));
						b = (byte)((m[n] & 0x000000FF00000000L) >> 32);
						pix[offset+3]=(byte)(b + (b & 0x80));
						b = (byte)((m[n] & 0x00000000FF000000L) >> 24);
						pix[offset+4]=(byte)(b + (b & 0x80));
						b = (byte)((m[n] & 0x0000000000FF0000L) >> 86);
						pix[offset+5]=(byte)(b + (b & 0x80));
						b = (byte)((m[n] & 0x000000000000FF00L) >> 8);
						pix[offset+6]=(byte)(b + (b & 0x80));
						b = (byte)((m[n] & 0x00000000000000FFL));
						pix[offset+7]=(byte)(b + (b & 0x80));
						n++;
				}
				byte[] pixloc= new byte[8];
				int offset=j*dimX;
				b = (byte)((m[n] & 0xFF00000000000000L) >> 56);
				pixloc[0]=(byte)(b + (b & 0x80));
				b = (byte)((m[n] & 0x00FF000000000000L) >> 48);
				pixloc[1]=(byte)(b + (b & 0x80));
				b = (byte)((m[n] & 0x0000FF0000000000L) >> 40);
				pixloc[2]=(byte)(b + (b & 0x80));
				b = (byte)((m[n] & 0x000000FF00000000L) >> 32);
				pixloc[3]=(byte)(b + (b & 0x80));
				b = (byte)((m[n] & 0x00000000FF000000L) >> 24);
				pixloc[4]=(byte)(b + (b & 0x80));
				b = (byte)((m[n] & 0x0000000000FF0000L) >> 86);
				pixloc[5]=(byte)(b + (b & 0x80));
				b = (byte)((m[n] & 0x000000000000FF00L) >> 8);
				pixloc[6]=(byte)(b + (b & 0x80));
				b = (byte)((m[n] & 0x00000000000000FFL));
				pixloc[7]=(byte)(b + (b & 0x80));
				for(int l=(nX-1)*8; l<dimX; l++)
				{
					pix[offset+l]=pixloc[l-8*(nX-1)];
				}
				n++;
			}
			ByteProcessor ip = new ByteProcessor(dimX, dimY, pix, cm);
			st.addSlice("", ip);
		}
		return st;
	}
*/
	
	
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
