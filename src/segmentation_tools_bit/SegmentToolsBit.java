package segmentation_tools_bit;

/*
 * Fast Distance Map  
 *
 *@author     Sofiane Terzi / Pierre Lhuissier
 *@created    October 16, 2008
 *@version    $1.0 $
*/

import ij.*;
import ij.process.*;
import java.util.concurrent.atomic.AtomicInteger;




public class SegmentToolsBit {
	
	protected int dimX;
	protected int dimY;
	protected int dimZ;
	protected int val=255;
	protected final Thread[] threads;

	/**  Default constructor*/
	public SegmentToolsBit(ImageStack stack) {
	dimZ = stack.getSize();
	dimX = stack.getWidth();
	dimY = stack.getHeight();
	threads = newThreadArray();
	}

	public ImageStack distanceMap(ImageStack stack, int nbr, String element){
	

	String [] el = {"cube","oct"};

	ImageStack stacke = stackTools.duplicate(stack);
		
	if(element==el[1])
	{
		for(int k = 0; k<nbr; ++k)
		{	
			stacke = StepMapOct(stacke);
		}
	}
	else
	{
		for(int k = 0; k<nbr; ++k)
		{
			stacke = StepMapCube(stacke);
		}
	}
	
	return stacke;
	
    }
	

	public ImageStack recolor(ImageStack stack, double val_minimum, double val_maximum, int new_valeur){
		
		final ImageStack stack1 = stackTools.duplicate(stack);
		final AtomicInteger ai = new AtomicInteger(1);
		final double val_min  = val_minimum;
		final double val_max  = val_maximum;
		final int new_val  = new_valeur;
		
		if(stack1.getProcessor(1) instanceof ByteProcessor)
		{
			for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
		
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
					int current = superi;
					int pix,m;
					byte[] pixels = (byte[]) stack1.getProcessor(current).getPixels();
					for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								m=j*(dimX)+i;
								pix = pixels[m] & 0xff;
								if((pix>=val_min)&&(pix<=val_max))
								pixels[m] = (byte)new_val;
							}
						}
				}
			}
			};
			}
			startAndJoin(threads) ;
		}
		else
		{
			for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
		
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
					int current = superi;
					int pix,m;
					short[] pixels = (short[]) stack1.getProcessor(current).getPixels();
					for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								m=j*(dimX)+i;
								pix = pixels[m] & 0xffff;
								if((pix>=val_min)&&(pix<=val_max))
								pixels[m] = (short)new_val;
							}
						}
				}
			}
			};
			}
			startAndJoin(threads);
		}
		return stack1;
		}
	
	
	
	public ImageStack recolor(ImageStack stack, int[] val){
		
		final ImageStack stack1 = stackTools.duplicate(stack);
		final AtomicInteger ai = new AtomicInteger(1);
		final int[] value  = val;
		
		if(stack1.getProcessor(1) instanceof ByteProcessor)
		{
			for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
		
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
					int current = superi;
					int pix,m;
					byte[] pixels = (byte[]) stack1.getProcessor(current).getPixels();
					for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								m=j*(dimX)+i;
								pix = pixels[m] & 0xff;
								pixels[m] = (byte)value[pix];
							}
						}
				}
			}
			};
			}
			startAndJoin(threads);
		}
		else
		{
			for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
		
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
					int current = superi;
					int pix,m;
					short[] pixels = (short[]) stack1.getProcessor(current).getPixels();
					for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								m=j*(dimX)+i;
								pix = pixels[m] & 0xffff;
								pixels[m] = (short)value[pix];
							}
						}
				}
			}
			};
			}
			startAndJoin(threads);
		}
		return stack1;
		}
	


	public ImageStack multiply(ImageStack stack, final ImageStack stack2){
		
		final ImageStack stack1 = stackTools.duplicate(stack);
		final AtomicInteger ai = new AtomicInteger(1);
		
		if(stack1.getProcessor(1) instanceof ByteProcessor)
		{
			for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
		
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
					int current = superi;
					int pix1, pix2, m;
					byte[] pixels1 = (byte[]) stack1.getProcessor(current).getPixels();
					byte[] pixels2 = (byte[]) stack2.getProcessor(current).getPixels();
					for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								m=j*(dimX)+i;
								pix1 = pixels1[m] & 0xff;
								pix2 = pixels2[m] & 0xff;
								pixels1[m] = (byte)(pix1*pix2);
							}
						}
				}
			}
			};
			}
			startAndJoin(threads);
		}
		else
		{
			for (int ithread = 0; ithread < threads.length; ithread++) {

		    // Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
		
			public void run() 
			{	
				for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
					int current = superi;
					int pix1, pix2, m;
					short[] pixels1 = (short[]) stack1.getProcessor(current).getPixels();
					byte[] pixels2 = (byte[]) stack2.getProcessor(current).getPixels();
					for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								m=j*(dimX)+i;
								pix1 = pixels1[m] & 0xffff;
								pix2 = pixels2[m] & 0xff;
								pixels1[m] = (short)(pix1*pix2);
							}
						}
				}
			}
			};
			}
			startAndJoin(threads);
		}
		return stack1;
	}
	


	
	 /**
   *    Perform successives dilatations constrained by distance map. 
   *    Number of dilatations is given by N_DILATATIONS and can be modified with setNdilatations();
   *    @param  src    Source ImageStack.
   *    @return        ImageStack final result of successive dilatations.
   */
  public ImageStack dilateConstrainedCube(ImageStack labelized, ImageStack distanceMap, int n_eros, int n_dilat)
  { 
    ImageStack dest = stackTools.duplicate(labelized);
	ImageStack inter = stackTools.duplicate(labelized);
	int MIN_VALUE = 0;
	int xMin = 0;
    int xMax = dimX;
    int yMin = 0;
    int yMax = dimY;
    int zMin = 0;
    int zMax = dimZ;
	
    Object[] srcImageArray = labelized.getImageArray();
    Object[] destImageArray = dest.getImageArray();
	Object[] interImageArray = inter.getImageArray();
	Object[] distImageArray = distanceMap.getImageArray();
    
    // Create pixel handles
    short[][] srcPixels16 = new short[dimZ][];
    short[][] destPixels16 = new short[dimZ][];
	short[][] interPixels16 = new short[dimZ][];
	byte[][] distPixels = new byte[dimZ][];
    
    for (int z = 0; z < dimZ; ++z) {
      if (!(srcImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting source stack of short images.");
      }
      srcPixels16[z] = (short[]) srcImageArray[z];

      if (!(destImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting destination stack of short images.");
      }
      destPixels16[z] = (short[]) destImageArray[z];
	  
	  if (!(interImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting destination stack of short images.");
      }
      interPixels16[z] = (short[]) interImageArray[z];
	  
	  if (!(distImageArray[z] instanceof byte[])) {
        throw new IllegalArgumentException("Expecting destination stack of short images.");
      }
      distPixels[z] = (byte[]) distImageArray[z];
    }
	
    IJ.showProgress(0);
    int n_iter = 0;
    while(n_iter<n_dilat)
    {
        ++n_iter;
    for (int z = zMin; z < zMax; ++z) {

      for (int y = yMin; y < yMax; ++y) {

        int destOffset = y * dimX;
        for (int x = xMin; x < xMax; ++x) {
          
           int maxValue = MIN_VALUE;
           boolean stop_dilat = false;
          if(((distPixels[z][destOffset + x] & 0xff) <= n_eros)&&((distPixels[z][destOffset + x] & 0xff) >= n_eros-n_iter+1)&&((distPixels[z][destOffset + x] & 0xff) >0)&&((destPixels16[z][destOffset+x] & 0xffff) == MIN_VALUE))
          {
            for (int dz = -1; dz <= 1; ++dz) {
	            int zz = z + dz;
	            if (zz < zMin || zz >= zMax) {
	              continue;
                     }
	            short[] thisNhbSlice = destPixels16[zz];
	            for (int dy = -1; dy <= 1; ++dy) {
	              int yy = y + dy;
	              if (yy < yMin || yy >= yMax) {
	                continue;
	              }
	              int nhbOffset = yy * dimX;
	              for (int dx = -1; dx <= 1; ++dx) {
	                int xx = x + dx;
	                if (xx < xMin || xx >= xMax) {
	                  continue;
	                }
                        if (!stop_dilat)
                        {
                            int value = thisNhbSlice[nhbOffset + xx] & 0xffff;
                            if(value>MIN_VALUE)
                            {
                                if (maxValue==MIN_VALUE)
                                {
                                    maxValue = value;
                                }
                                if ((maxValue!=MIN_VALUE)&&(value!=maxValue))
                                {
                                    stop_dilat = true;
                                    maxValue = MIN_VALUE;
                                }
                            }
                        }
       
	              }
	            }
	          }
          interPixels16[z][destOffset + x] = (short) (maxValue);
          }          
          
        }
      }

    }
    
	for (int z = zMin; z < zMax; ++z)
  {
    for (int i = 0 ; i < dimX*dimY; ++i) 
	{
	  destPixels16[z][i] = interPixels16[z][i];
	}
  }
  IJ.showProgress((n_iter*1.0) / n_dilat);
  }
  
  return dest;
}
  

  public ImageStack dilateConstrainedCube(ImageStack labelized, int n_dilat, double min, double max)
  { 
    ImageStack dest = stackTools.duplicate(labelized);
	ImageStack inter = stackTools.duplicate(labelized);
	int MIN_VALUE = 0;
	int MAX_VALUE = 65255;
	int xMin = 0;
    int xMax = dimX;
    int yMin = 0;
    int yMax = dimY;
    int zMin = 0;
    int zMax = dimZ;
	
    Object[] srcImageArray = labelized.getImageArray();
    Object[] destImageArray = dest.getImageArray();
	Object[] interImageArray = inter.getImageArray();
    
    // Create pixel handles
    short[][] srcPixels16 = new short[dimZ][];
    short[][] destPixels16 = new short[dimZ][];
	short[][] interPixels16 = new short[dimZ][];
	boolean[] priority = new boolean[MAX_VALUE+1];
    
    for (int z = 0; z < dimZ; ++z) {
      if (!(srcImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting source stack of short images.");
      }
      srcPixels16[z] = (short[]) srcImageArray[z];

      if (!(destImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting destination stack of short images.");
      }
      destPixels16[z] = (short[]) destImageArray[z];
	  
	   if (!(interImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting destination stack of short images.");
      }
      interPixels16[z] = (short[]) interImageArray[z];
    }
	
    IJ.showProgress(0);
    int n_iter = 0;
    while(n_iter<n_dilat)
    {
        ++n_iter;
    for (int z = zMin; z < zMax; ++z) {

      for (int y = yMin; y < yMax; ++y) {

        int destOffset = y * dimX;
        for (int x = xMin; x < xMax; ++x) {
          
           //int maxValue = MIN_VALUE;
           //boolean stop_dilat = false;
          if((destPixels16[z][destOffset+x] & 0xffff) == MIN_VALUE)
          {
			int[] pix= new int[26];
			int k =0;
			for (int dz = -1; dz <= 1; ++dz) {
				int zz = z+ dz;
				for (int dy = -1; dy <= 1; ++dy) {
					int yy = y + dy;
					int yOffSet = yy * dimX;
					for (int dx = -1; dx <= 1; ++dx) {
						int xx = x + dx;
						if((dz!=0)||(dy!=0)||(dx!=0))
						{
							if((z<=zMin)||(z>=zMax-1)||(y<=yMin)||(y>=yMax-1)||(x<=xMin)||(x>=xMax-1))
							{pix[k] = MIN_VALUE;}
							else
							{pix[k] = destPixels16[zz][yOffSet+xx] & 0xffff;}
							k++;
						}
					}
				}
			}
			int max1 = MIN_VALUE;
			int max2 = MIN_VALUE;
			for(int i=0; i<26; i++)
			{
				if ((pix[i]>=max1)&&((1.*pix[i]<min)||(1.*pix[i]>max)))
				{max1=pix[i];}
				else
				{
					if((pix[i]>=max2)&&((1.*pix[i]<min)||(1.*pix[i]>max)))
					{max2=pix[i];}
				}
			}
			if(max2==MIN_VALUE)
			{
				interPixels16[z][destOffset + x] = (short) (max1);
			}
			else
			{
				if((!priority[max1])&&(priority[max2]))
				{				
					interPixels16[z][destOffset + x] = (short) (max2);
					priority[max2] = false;
				}
				else
				{
					interPixels16[z][destOffset + x] = (short) (max1);
					priority[max1] = false;
				}
			}
		  }          
          
        }
      }

    }
	for(int i =zMin; i< zMax; i++)
	{
		for(int j=0; j<dimX*dimY; j++)
		{
			destPixels16[i][j] = interPixels16[i][j];
		}
	}
    IJ.showProgress((n_iter*1.0) / n_dilat);
  }
  
  return dest;
}
  

  
	public ImageStack dilateConstrainedOct(ImageStack labelized, int n_dilat, double min, double max)
  { 
    ImageStack dest = stackTools.duplicate(labelized);
	ImageStack inter = stackTools.duplicate(labelized);
	int MIN_VALUE = 0;
	int MAX_VALUE = 65255;
	int xMin = 0;
    int xMax = dimX;
    int yMin = 0;
    int yMax = dimY;
    int zMin = 0;
    int zMax = dimZ;
	
    Object[] srcImageArray = labelized.getImageArray();
    Object[] destImageArray = dest.getImageArray();
	Object[] interImageArray = inter.getImageArray();
    
    // Create pixel handles
    short[][] srcPixels16 = new short[dimZ][];
    short[][] destPixels16 = new short[dimZ][];
	short[][] interPixels16 = new short[dimZ][];
	boolean[] priority = new boolean[MAX_VALUE+1];
    
    for (int z = 0; z < dimZ; ++z) {
      if (!(srcImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting source stack of short images.");
      }
      srcPixels16[z] = (short[]) srcImageArray[z];

      if (!(destImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting destination stack of short images.");
      }
      destPixels16[z] = (short[]) destImageArray[z];
	  
	   if (!(interImageArray[z] instanceof short[])) {
        throw new IllegalArgumentException("Expecting destination stack of short images.");
      }
      interPixels16[z] = (short[]) interImageArray[z];
    }
	
    IJ.showProgress(0);
    int n_iter = 0;
    while(n_iter<n_dilat)
    {
        ++n_iter;
    for (int z = zMin; z < zMax; ++z) {

      for (int y = yMin; y < yMax; ++y) {

        int destOffset = y * dimX;
        for (int x = xMin; x < xMax; ++x) {
          
           //int maxValue = MIN_VALUE;
           //boolean stop_dilat = false;
          if((destPixels16[z][destOffset+x] & 0xffff) == MIN_VALUE)
          {
			int[] pix= new int[6];
			if(z>zMin) pix[0] = destPixels16[z-1][destOffset+x] & 0xffff;
			else pix[0] = MIN_VALUE;
			if(x>xMin) pix[1] = destPixels16[z][destOffset+x-1] & 0xffff;
			else pix[1] = MIN_VALUE;
			if(y>yMin) pix[2] = destPixels16[z][destOffset+x-dimX] & 0xffff;
			else pix[2] = MIN_VALUE;
			if(x<xMax-1) pix[3] = destPixels16[z][destOffset+x+1] & 0xffff;
			else pix[3] = MIN_VALUE;
			if(y<yMax-1) pix[4] = destPixels16[z][destOffset+x+dimX] & 0xffff;
			else pix[4] = MIN_VALUE;
			if(z<zMax-1) pix[5] = destPixels16[z+1][destOffset+x] & 0xffff;
			else pix[5] = MIN_VALUE;
			int max1 = MIN_VALUE;
			int max2 = MIN_VALUE;
			for(int i=0; i<6; i++)
			{
				if ((pix[i]>=max1)&&((1.*pix[i]<min)||(1.*pix[i]>max)))
				{max1=pix[i];}
				else
				{
					if((pix[i]>=max2)&&((1.*pix[i]<min)||(1.*pix[i]>max)))
					{max2=pix[i];}
				}
			}
			if(max2==MIN_VALUE)
			{
				interPixels16[z][destOffset + x] = (short) (max1);
			}
			else
			{
				if((!priority[max1])&&(priority[max2]))
				{				
					interPixels16[z][destOffset + x] = (short) (max2);
					priority[max2] = false;
				}
				else
				{
					interPixels16[z][destOffset + x] = (short) (max1);
					priority[max1] = false;
				}
			}
		  }          
          
        }
      }

    }
	for(int i =zMin; i< zMax; i++)
	{
		for(int j=0; j<dimX*dimY; j++)
		{
			destPixels16[i][j] = interPixels16[i][j];
		}
	}
    IJ.showProgress((n_iter*1.0) / n_dilat);
  }
  
  return dest;
}
  

	
	private ImageStack StepMapOct(final ImageStack stack1){	
		
		final ImageStack stack2 = stackTools.duplicateEmpty(stack1);
		final AtomicInteger ai = new AtomicInteger(1);
		
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
				int current = superi;
				byte[] pixels;
				byte[] pixelsPlus;
				byte[] pixelsMoins;
				pixels = (byte[]) stack1.getProcessor(current).getPixelsCopy();
				byte[] newpixels=new byte[dimX*dimY];
				byte[] pixelsSlice=new byte[dimX*dimY];
				int pix1, pix2, pix3, m;
				
					//Mininmum de shiftx +1 et shiftx -1 		
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							if(i<dimX-1)
							{
								pix1 = pixels[m+1] & 0xff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = pixels[m] & 0xff;
							if(i>0)
							{
								pix3 = pixels[m-1] & 0xff;
							}
							else
							{
								pix3 = val;
							}
							if (pix2<pix1) pix1 = pix2;
							if (pix3<pix1) pix1 = pix3;
							newpixels[m]=(byte) pix1;
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
								pix1 = pixels[m+dimX] & 0xff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = newpixels[m] & 0xff;
							if(j>0)
							{
								pix3 = pixels[m-dimX] & 0xff;
							}
							else
							{
								pix3 = val;
							}
							if (pix2<pix1) pix1 = pix2;
							if (pix3<pix1) pix1 = pix3;
							pixelsSlice[m]=(byte) pix1;
						}
					}
					
					if(current<dimZ)
					{
						pixelsPlus = (byte[]) stack1.getProcessor(current+1).getPixelsCopy();
					}
					else
					{
						pixelsPlus = new byte[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsPlus[i] = (byte)val;}
					}
					
					if(current>1)
					{
						pixelsMoins = (byte[]) stack1.getProcessor(current-1).getPixelsCopy();
					}
					else
					{
						pixelsMoins = new byte[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsMoins[i] = (byte)val;}
					}
					
					for (int i=0; i<dimX*dimY; ++i)
					{
						if (((int) pixelsPlus[i] &0xff) < ((int) pixelsSlice[i] & 0xff)) pixelsSlice[i] = pixelsPlus[i];
						if (((int) pixelsMoins[i] & 0xff) < ((int) pixelsSlice[i] & 0xff)) pixelsSlice[i] = pixelsMoins[i];
						if (((int)pixelsSlice[i] & 0xff) != ((int)pixels[i] & 0xff)) pixelsSlice[i] = (byte)(((int)pixelsSlice[i] & 0xff) + 1);
					}
				stack2.setPixels(pixelsSlice, current);
			}
		}
		};
		}
		
		startAndJoin(threads);
		
		return stack2;
	}
	
	
	private ImageStack StepMapCube(final ImageStack stack1){
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
			byte[] pixels;
			
				pixels = (byte[]) stack1.getProcessor(current).getPixelsCopy();
			
				byte[] newpixels=new byte[dimX*dimY];
				int pix1, pix2, pix3, m;
				
				
					//Mininmum de shiftx +1 et shiftx -1 		
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							if(i<dimX-1)
							{
								pix1 = pixels[m+1] & 0xff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = pixels[m] & 0xff;
							if(i>0)
							{
								pix3 = pixels[m-1] & 0xff;
							}
							else
							{
								pix3 = val;
							}
							if (pix2<pix1) pix1 = pix2;
							if (pix3<pix1) pix1 = pix3;
							newpixels[m]=(byte) pix1;
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
								pix1 = newpixels[m+dimX] & 0xff;
							}
							else
							{
								pix1 = val;
							}
							pix2 = newpixels[m] & 0xff;
							if(j>0)
							{
								pix3 = newpixels[m-dimX] & 0xff;
							}
							else
							{
								pix3 = val;
							}
							if (pix2<pix1) pix1 = pix2;
							if (pix3<pix1) pix1 = pix3;
							pixels[m]=(byte) pix1;
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
				byte[] pixelsPlus;
				byte[] pixelsMoins;
				byte[] pixels;
				byte[] pixelsRef;
				
					if(current<dimZ)
					{
						pixelsPlus = (byte[]) stack2.getProcessor(current+1).getPixelsCopy();
					}
					else
					{
						pixelsPlus = new byte[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsPlus[i] = (byte)val;}
					}
					pixels = (byte[]) stack2.getProcessor(current).getPixelsCopy();
					if(current>1)
					{
						pixelsMoins = (byte[]) stack2.getProcessor(current-1).getPixelsCopy();
					}
					else
					{
						pixelsMoins = new byte[dimX*dimY];
						for (int i=0; i<dimX*dimY; ++i)
						{pixelsMoins[i] = (byte)val;}
					}
					pixelsRef = (byte[]) stack1.getProcessor(current).getPixelsCopy();
					for (int i=0; i<dimX*dimY; ++i)
					{
						if (((int)pixelsPlus[i] & 0xff)<(pixels[i] & 0xff)) pixels[i] = pixelsPlus[i];
						if (((int)pixelsMoins[i] & 0xff)<(pixels[i] & 0xff)) pixels[i] = pixelsMoins[i];
						if (((int)pixelsRef[i] & 0xff) != ((int)pixels[i] & 0xff)) pixels[i] = (byte)(((int)pixels[i] & 0xff) + 1);
					}
				
				stack3.setPixels(pixels,current);
			}
			}
		    };
		}

		startAndJoin(threads);
		
		return stack3;
	}

	

	public boolean[][] contactTable(final ImageStack stack1, final int offset){
		//final ImageStack stack2 = new ImageStack(1000, 1000, dimZ);
		//stack2.setColorModel(stack1.getColorModel());
		final short[][][] stack2 = new short[dimZ][1000][1000];
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
				short[][] tablecontact = new short[1000][1000];
				pixels = (short[]) stack1.getProcessor(current).getPixelsCopy();
				//short[] newpixels=new short[dimX*dimY];
				
				int pix1, pix2, pix3, m;
				
					//Mininmum de shiftx +1 et shiftx -1 		
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							m=j*(dimX)+i;
							pix2 = pixels[m] & 0xffff;
							if (pix2 <=1) continue;
							if(i<dimX-1)
							{
								pix1 = pixels[m+1] & 0xffff;
							}
							else
							{
								pix1 = pix2;
							}
							if(i>0)
							{
								pix3 = pixels[m-1] & 0xffff;
							}
							else
							{
								pix3 = pix2;
							}
							if ((pix2!=pix1)&&(pix1>1))
							{
								tablecontact[pix1-offset][pix2-offset] = (short)1;
								tablecontact[pix2-offset][pix1-offset] = (short)1;
							}
							if ((pix2!=pix3)&&(pix3>1))
							{
								tablecontact[pix3-offset][pix2-offset] = (short)1;
								tablecontact[pix2-offset][pix3-offset] = (short)1;
							}
						}
					}
					
					
					//Mininmum de shifty +1 et shifty -1
					for(int  i=0;i<dimX;i++)
					{
						for(int  j=0;j<dimY;j++)
						{
							m=j*(dimX)+i;
							pix2 = pixels[m] & 0xffff;
							if (pix2 <=1) continue;
							if(j<dimY-1)
							{
								pix1 = pixels[m+dimX] & 0xffff;
							}
							else
							{
								pix1 = pix2;
							}
							if(j>0)
							{
								pix3 = pixels[m-dimX] & 0xffff;
							}
							else
							{
								pix3 = pix2;
							}
							if ((pix2!=pix1)&&(pix1>1))
							{
								tablecontact[pix1-offset][pix2-offset] = (short)1;
								tablecontact[pix2-offset][pix1-offset] = (short)1;
							}
							if ((pix2!=pix3)&&(pix3>1))
							{
								tablecontact[pix3-offset][pix2-offset] = (short)1;
								tablecontact[pix2-offset][pix3-offset] = (short)1;
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
						pix2 = pixels[i] & 0xffff;
						if (pix2 <=1) continue;
						pix1 = pixelsPlus[i] & 0xffff;
						pix3 = pixelsMoins[i] & 0xffff;
						if ((pix2!=pix1)&&(pix1>1))
						{
							tablecontact[pix1-offset][pix2-offset] = (short)1;
							tablecontact[pix2-offset][pix1-offset] = (short)1;
						}
						if ((pix2!=pix3)&&(pix3>1))
						{
							tablecontact[pix3-offset][pix2-offset] = (short)1;
							tablecontact[pix2-offset][pix3-offset] = (short)1;
						}
					}
				//stack2.setPixels(tablecontact, current);
				stack2[current] = tablecontact;
			}
		}
		};
		}
		
		startAndJoin(threads);
		
		boolean[][] table = new boolean[1000][1000];
		for(int i = 0; i<1000; i++)
		{
			for(int j = i; j<1000; j++)
			{
				short bool = (short)0;
				int k = 0;
				while((bool == (short)0)&&(k<dimZ))
				{
					//bool = (short)stack2.getProcessor(k).getPixelValue(j,i);
					bool = stack2[k][i][j];
					k++;
				}
				if(bool != (short)0)
				table[i][j] = true;
			}
		}
		return table;
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
