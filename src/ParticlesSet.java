
import ij.IJ;
import ij.ImageStack;
import ij.process.ByteProcessor;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ParticlesSet {
	
	private String name;
	private ArrayList<Particle> particles;
	protected final Thread[] threads= newThreadArray();

	public ParticlesSet() {
		super();
		this.name = "";
		this.particles = new ArrayList<Particle>();
	}
	
	public ParticlesSet(String name) {
		super();
		this.name = name;
		this.particles = new ArrayList<Particle>();
	}
	
	public ParticlesSet(String name, ArrayList<Particle> particles) {
		super();
		this.name = name;
		this.particles = particles;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Particle> getParticles() {
		return particles;
	}

	public void setParticles(ArrayList<Particle> particles) {
		this.particles = particles;
	}

	public Particle getParticle(int i) {
		if(this.particles.size()>i)
			return particles.get(i);
		return null;
	}

	public void setParticle(int i, Particle particle) {
		this.particles.set(i, particle);
	}

	public void addParticle(Particle particle) {
		this.particles.add(particle);
	}
	
	public int Size() {
		return particles.size();
	}
	
	public void removeParticle(int i)
	{
		this.particles.remove(i);
	}
	
	public double[] findExtrema(String getField)
	{
		double min=100000000.;
		double max = -10000000.;
		Class<? extends Particle> part = new Particle().getClass();
		Method m = null;
		try {
			m = part.getMethod(getField);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<this.particles.size(); i++)
		{ 
			String str = null;
			try {
				str = m.invoke(this.particles.get(i)).toString();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			double val = Double.valueOf(str).doubleValue();
			if (val<min)
				min=val;
			if (val>max)
				max=val;
		}
		double values[] = new double[2];
		values[0]=min;
		values[1]=max;
		return values;
	}
	
	public ParticlesSet subSetCenter(double Xmin, double Xmax, double Ymin, double Ymax, double Zmin, double Zmax, boolean keepSelection)
	{
		ParticlesSet subset = new ParticlesSet(this.name+"subset");
		int newglobal=1;
		for(int i=0; i<this.particles.size();i++)
		{
			double X=this.particles.get(i).getCoord().getX();
			double Y=this.particles.get(i).getCoord().getY();
			double Z=this.particles.get(i).getCoord().getZ();
			
			if((X>Xmin)&&(X<Xmax)&&(Y>Ymin)&&(Y<Ymax)&&(Z>Zmin)&&(Z<Zmax)&&keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
			else if(((X<Xmin)||(X>Xmax)||(Y>Ymin)||(Y<Ymax)||(Z>Zmin)||(Z<Zmax))&&!keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
		}
		return subset;
	}
	
	public ParticlesSet subSetDbox(double Xmin, double Xmax, double Ymin, double Ymax, double Zmin, double Zmax, boolean keepSelection)
	{
		ParticlesSet subset = new ParticlesSet(this.name+"subset");
		int newglobal=1;
		for(int i=0; i<this.particles.size();i++)
		{
			double X=this.particles.get(i).getDx();
			double Y=this.particles.get(i).getDy();
			double Z=this.particles.get(i).getDz();
			
			if((X>Xmin)&&(X<Xmax)&&(Y>Ymin)&&(Y<Ymax)&&(Z>Zmin)&&(Z<Zmax)&&keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
			else if(((X<Xmin)||(X>Xmax)||(Y>Ymin)||(Y<Ymax)||(Z>Zmin)||(Z<Zmax))&&!keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
		}
		return subset;
	}
	
	public ParticlesSet subSetShape(double Xmin, double Xmax, double Ymin, double Ymax, double Zmin, double Zmax, boolean keepSelection)
	{
		ParticlesSet subset = new ParticlesSet(this.name+"subset");
		int newglobal=1;
		for(int i=0; i<this.particles.size();i++)
		{
			double X=this.particles.get(i).getFab();
			double Y=this.particles.get(i).getFac();
			double Z=this.particles.get(i).getFbc();
			
			if((X>Xmin)&&(X<Xmax)&&(Y>Ymin)&&(Y<Ymax)&&(Z>Zmin)&&(Z<Zmax)&&keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
			else if(((X<Xmin)||(X>Xmax)||(Y>Ymin)||(Y<Ymax)||(Z>Zmin)||(Z<Zmax))&&!keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
		}
		return subset;
	}
	
	public ParticlesSet subSetVolume(double Vmin, double Vmax, boolean keepSelection)
	{
		ParticlesSet subset = new ParticlesSet(this.name+"subset");
		int newglobal=1;
		for(int i=0; i<this.particles.size();i++)
		{
			double V=this.particles.get(i).getVolume();
			if(((V>Vmin)&&(V<Vmax))&&keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
			else if(((V<Vmin)||(V>Vmax))&&!keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;				
			}
		}
		return subset;
	}

	public ParticlesSet subSetSphericity(double Vmin, double Vmax, boolean keepSelection)
	{
		ParticlesSet subset = new ParticlesSet(this.name+"subset");
		int newglobal=1;
		for(int i=0; i<this.particles.size();i++)
		{
			double V=this.particles.get(i).getSphericity();
			if(((V>Vmin)&&(V<Vmax))&&keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
			else if(((V<Vmin)||(V>Vmax))&&!keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;				
			}
		}
		return subset;
	}
	
	public ParticlesSet subSetColor(String color, boolean keepSelection)
	{
		ParticlesSet subset = new ParticlesSet(this.name+"subset");
		int newglobal=1;
		
		String[] numbers = color.split(";");
		for(int i=0; i<this.particles.size();i++)
		{
			double V=this.particles.get(i).getColor();
			boolean insideSelection=false;
			for(int j=1; j<numbers.length; j++)
			{
				if((V==Integer.valueOf(numbers[i])))
				{
					insideSelection=true;
				}
			}
			if(insideSelection&&keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
			else if(!insideSelection&&!keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;				
			}
		}
		return subset;
	}
	
	public ParticlesSet subSetBoundary(boolean bound, boolean notbound, boolean keepSelection)
	{
		ParticlesSet subset = new ParticlesSet(this.name+"subset");
		int newglobal=1;
		for(int i=0; i<this.particles.size();i++)
		{
			boolean V=this.particles.get(i).isBoundary();
			if(((V&&bound)||(!V&&notbound))&&keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
			else if(((!V^bound)&&(V^notbound))&&!keepSelection)
			{
				subset.addParticle(this.particles.get(i));
				subset.particles.get(newglobal-1).setGlobalColor(newglobal);
				newglobal++;
			}
				
		}
		return subset;
	}

	
	public int globalNumber(int current)
	{
		for(int i=0; i<this.particles.size();i++)
		{
			if((this.particles.get(i)!=null)&&(this.particles.get(i).getGlobalColor()==0))
			{
				current++;
				this.particles.get(i).setGlobalColor(current);
			}
		}
		return current;
	}

	public ParticlesSet rankByVolume()
	{
		ParticlesSet ranked = new ParticlesSet(this.getName());
		while(this.particles.size()>0)
		{
			double maxVol=0;
			int best=0;
			for(int i=0; i<this.particles.size();i++)
			{
				if(this.particles.get(i).getVolume()>maxVol)
				{
					maxVol=this.particles.get(i).getVolume();
					best=i;
				}
			}
			ranked.addParticle(this.particles.get(best));
			this.removeParticle(best);
		}
		return ranked;
	}
	
	public Particle findParticle(int color)
	{
		int i=0;
		while(i<this.particles.size())
		{
			if(this.particles.get(i).getColor()==color)
				return this.getParticle(i);
			i++;
		}
		return null;
	}
	
	public Particle findGlobalColorParticle(int color)
	{
		int i=0;
		while(i<this.particles.size())
		{
			if(this.particles.get(i).getGlobalColor()==color)
				return this.getParticle(i);
			i++;
		}
		return null;
	}
	

	


	
	public void writeToFile(String fileName)
	{
		PrintWriter pw = null;
		try
		{
			FileOutputStream fos = new FileOutputStream(fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			pw = new PrintWriter(bos);
		}
		
		catch (IOException e) 
		{
			IJ.log("pb" + e);
			return;
		}
		
		for(int j=0; j<this.particles.size(); j++)
		{
			if(this.particles.get(j)!=null)
			{
				pw.println(this.particles.get(j).toString());
			}
			else
			{
				pw.println("");
			}	
		}
		pw.close();
	}
	


	
	public String PosToString()
	{
		String colorLine = "";
		
		for(int j=0; j<this.particles.size(); j++)
		{
			if(this.particles.get(j)!=null)
			{
				colorLine +=this.particles.get(j).getCoord().getX() + " " + this.particles.get(j).getCoord().getY() + " " + this.particles.get(j).getCoord().getZ() +" ";
			}
			else
			{
				colorLine +="0 0 0 ";
			}	
		}
		return colorLine;
	}
	
	public ImageStack recolor(ImageStack stack, int numInitialPart){
		
		final ImageStack stack1 = duplicate(stack);
		final AtomicInteger ai = new AtomicInteger(1);
		int[] val = new int[numInitialPart+1];
		for(int i=0; i<numInitialPart+1; i++)
		{
			val[i]=0;
		}
		for(int i=0; i<this.particles.size(); i++)
		{
			val[this.particles.get(i).getColor()]=this.particles.get(i).getGlobalColor();
		}
		
		
		final int[] value  = val;
		final int dimX = stack1.getWidth();
		final int dimY = stack1.getHeight();
		final int dimZ = stack1.getSize();
		
		
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

	public ImageStack recolor_norenumber(ImageStack stack, int numInitialPart){
		
		final ImageStack stack1 = duplicate(stack);
		final AtomicInteger ai = new AtomicInteger(1);
		int[] val = new int[numInitialPart+1];
		for(int i=0; i<numInitialPart+1; i++)
		{
			val[i]=0;
		}
		for(int i=0; i<this.particles.size(); i++)
		{
			if(this.particles.get(i).getGlobalColor()==0)
				val[this.particles.get(i).getColor()]=0;
			else
				val[this.particles.get(i).getColor()]=this.particles.get(i).getColor();
		}
		
		
		final int[] value  = val;
		final int dimX = stack1.getWidth();
		final int dimY = stack1.getHeight();
		final int dimZ = stack1.getSize();
		
		
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
	
	public static ImageStack duplicate(ImageStack src) {
	    int xSize = src.getWidth();
	    int ySize = src.getHeight();
	    int zSize = src.getSize();

	    ImageStack dest = new ImageStack(xSize, ySize);
	    for (int z = 1; z <= zSize; ++z) {
	      dest.addSlice(src.getSliceLabel(z),
	          src.getProcessor(z).duplicate());
	    }

	    dest.setColorModel(src.getColorModel());

	    return dest;
	  }
	   
	   public static ImageStack duplicateEmpty(ImageStack src) {
		    int xSize = src.getWidth();
		    int ySize = src.getHeight();
		    int zSize = src.getSize();

		    ImageStack dest = new ImageStack(xSize, ySize);
		    for (int z = 1; z <= zSize; ++z) {
		      dest.addSlice(src.getSliceLabel(z),
		          src.getProcessor(z).createProcessor(xSize, ySize));
		    }

		    dest.setColorModel(src.getColorModel());

		    return dest;
		 }
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
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


