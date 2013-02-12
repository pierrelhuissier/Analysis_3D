/**
 * Labeling 3D 
 * version v1.1 - 06/09/2012
 * Pierre Lhuissier - Luc Salvo 
 * SIMaP/GPM2 - Grenoble University - CNRS - France
 */

import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

//  Stack labeling
//  labelize a binarized (0/255) volum 
//  requires the type of neighbouring rule (6 or 26 )
//  generates a short int volum
//  Max number of entities is 65500


public class Labeling_3D implements PlugInFilter {

	protected ImageStack stack,stack2;
	static int valVoxel=0;
	static int valPixel=255;
	int test=1;
	int pix_valPixel=0;
	int pix_0 =0;
	int nbpix,nbrestant;
	int color = 1;
	byte val;
    String [] connectivity = {"6","26"};
    String [] border = {"yes","no"};
	int size;
	
	protected final Thread[] threads= newThreadArray();

	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_8G+STACK_REQUIRED;
	}

	public void run(ImageProcessor ip) {

		GenericDialog dia = new GenericDialog("3D labeling:", IJ.getInstance());	
		dia.addNumericField("color to label (0 or 255)", valPixel, 0);
		dia.addNumericField("minimum volume to label in voxel", valVoxel, 0);
		dia.addChoice("3D connectivity",connectivity,connectivity[0]);
		//dia.addChoice("border",border,border[0]);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		valPixel = (int) dia.getNextNumber();
		valVoxel = (int) dia.getNextNumber();
		int nb=0;
		if((valPixel==0) || (valPixel==255))
		{
			String numberconnect = connectivity[dia.getNextChoiceIndex()];
			//String bord = border[dia.getNextChoiceIndex()];
		
			stack2 = newShortEmpty(stack);
			
			if(numberconnect=="6")
			{
					int[] maxval=connect2D_6(stack, stack2);
					nb=connect3Db_6(stack2,maxval);
			}
			else
			{
					int[] maxval=connect2D_26(stack, stack2);
					nb=connect3D_26(stack2,maxval);
			}	

			new ImagePlus("Labelled stack with " +nb + " objects", stack2).show();
		}
		else IJ.showMessage("value should be 0 or 255");

}
	
	
public int[] connect2D_6(final ImageStack stackBW, final ImageStack stackLab){	
		
	final AtomicInteger ai = new AtomicInteger(1);
	final int dimX = stackBW.getWidth();
	final int dimY = stackBW.getHeight();
	final int dimZ = stackBW.getSize();
	final int[] maxval = new int[dimZ+1];
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	// Concurrently run in as many threads as CPUs

	threads[ithread] = new Thread() {
	
	public void run() 
	{	
		for (int superi = ai.getAndIncrement(); superi <=dimZ; superi = ai.getAndIncrement())
		{
			int k = superi;
			ArrayList<ArrayList<Integer>> corr = new ArrayList<ArrayList<Integer>>();
			corr.add(new ArrayList<Integer>());
			int current=0;
			ImageProcessor ipBW = stackBW.getProcessor(k);
			ImageProcessor ipLab = stackLab.getProcessor(k);
			for( int j=0; j<dimY; j++)
			{
				for( int i=0; i<dimX; i++)
				{
					int val =(int) (ipBW.getPixelValue(i,j));
					if (val==valPixel)
					{
						int sup=0;
						if(j>0)
						{
							 sup=(int) (ipLab.getPixelValue(i,j-1));
						}
						int left=0;
						if(i>0)
						{
							 left=(int) (ipLab.getPixelValue(i-1,j));
						}
						if(left==0)
						{
							if(sup==0)
							{
								current++;
								corr.add(new ArrayList<Integer>());
								corr.get(current).add(current);
							}
							else if(sup<current)
							{
								current++;
								corr.add(new ArrayList<Integer>());
								corr.get(current).add(current);
								if(!corr.get(sup).contains(current))
								{
									corr.get(sup).add(current);
									corr.get(current).add(sup);
								}
							}
						}
						else
						{
							if((sup>0)&&(sup<current)&&(!corr.get(sup).contains(current)))
							{
										corr.get(sup).add(current);
										corr.get(current).add(sup);
							}
						}
						ipLab.putPixel(i,j,current);	
					}
//					else
//					{
//						ipLab.putPixel(i,j,0);
//					}
				}
			}

			int actu=0;
			for(int i=1; i<=current;i++)
			{
				if(corr.get(i).get(0)==i)
				{
					actu++;
					connect2D(corr,actu,i);
				}
			}
			
			int pix;
			for(int  j=0;j<dimY;j++)
			{
				for(int  i=0;i<dimX;i++)
				{
						pix=ipLab.getPixel(i,j);
						if(pix!=0)
							ipLab.putPixel(i,j,corr.get(pix).get(0));
				}
			}
			maxval[k]=actu;
		}
		}
		};
		}
		startAndJoin(threads);
		return maxval;
	}

public int connect3D_6(final ImageStack stackLab, final int[] maxval){	
	
	final AtomicInteger ai = new AtomicInteger(1);
	final int dimX = stackLab.getWidth();
	final int dimY = stackLab.getHeight();
	final int dimZ = stackLab.getSize();
	final ArrayList<ArrayList<ArrayList<Integer>>> corr = new ArrayList<ArrayList<ArrayList<Integer>>>();
	for(int k=0; k<=dimZ; k++)
	{
		corr.add(new ArrayList<ArrayList<Integer>>());
	}
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	// Concurrently run in as many threads as CPUs

	threads[ithread] = new Thread() {
	
	public void run() 
	{	
		for (int superi = ai.getAndIncrement(); superi <=dimZ; superi = ai.getAndIncrement())
		{
			int k = superi;
			
			for(int i=0; i<=maxval[k]; i++)
			{
				corr.get(k).add(new ArrayList<Integer>());
				corr.get(k).get(i).add(0);
			}
			
			if(k<dimZ)
			{
				ImageProcessor ip1 = stackLab.getProcessor(k);
				ImageProcessor ip2 = stackLab.getProcessor(k+1);
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<dimX; i++)
					{
						int val2 =(int) (ip2.getPixelValue(i,j));
						if (val2!=0)
						{
							int val1=(int) (ip1.getPixelValue(i,j));
							if (!corr.get(k).get(val1).contains(val2))
								corr.get(k).get(val1).add(val2);
						}
					}
				}
			}
		}
	}
	};
	}

	startAndJoin(threads);
	
	final ArrayList<ArrayList<Integer>> connect = new ArrayList<ArrayList<Integer>>();
	connect.add(new ArrayList<Integer>());
	int actu=0;
	for(int i=1; i<=dimZ; i++)
	{
		for(int j=1; j<corr.get(i).size();j++)
		{
			if(corr.get(i).get(j).get(0)==0)
			{
				actu++;
				connect.add(new ArrayList<Integer>());
				connect.get(actu).add(actu);
				connect3Db(corr,connect,actu,i,j);
			}
		}
	}
	for(int j=1; j<corr.get(dimZ).size();j++)
	{
		if(corr.get(dimZ).get(j).get(0)==0)
		{
			actu++;
			connect.add(new ArrayList<Integer>());
			connect.get(actu).add(actu);
			corr.get(dimZ).get(j).set(0,actu);
		}
	}
	
	int newactu=0;
	final int[] newconnect = new int[actu+1];
	for(int i=1; i<=actu; i++)
	{
		if(connect.get(i).get(0)==i)
		{
			newactu++;
			newconnect[i]=newactu;
		}
		else
		{
			newconnect[i]=newconnect[connect1D(connect,connect.get(i).get(0))];
		}
	}
	
	if(valVoxel!=0)
	{
		final int[][] vol  = new int[dimZ][newactu+1];
		final AtomicInteger ai3 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai3.getAndIncrement(); superi <= dimZ; superi = ai3.getAndIncrement()) {
				int k = superi;
				ImageProcessor ipLab = stackLab.getProcessor(k);
				for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							int pix=ipLab.getPixel(i,j);
							if(pix!=0)
								vol[k][newconnect[(corr.get(k).get(pix).get(0))]]++;
						}
					}
			}
		}
		};
		}
		startAndJoin(threads);
	
		int[] v = new int[newactu+1];
		for(int i=0; i<dimZ; i++)
		{
			for(int j=1; j<=newactu; j++)
			{
				v[j]+=vol[i][j];
			}
		}
		
		final int[] finalconnect = new int[newactu+1];
		int finalactu=0;
		for(int i=1; i<=newactu; i++)
		{
			if(v[i]<valVoxel)
			{
				finalconnect[i]=0;
			}
			else
			{
				finalactu++;
				finalconnect[i]=finalactu;
			}
		}
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

			// Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
			
			public void run() 
			{	
				for (int superi = ai2.getAndIncrement(); superi <=dimZ; superi = ai2.getAndIncrement())
				{
					int k = superi;
					int pix;
					ImageProcessor ipLab = stackLab.getProcessor(k);
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							pix=ipLab.getPixel(i,j);
							if(pix!=0)
								ipLab.putPixel(i,j,finalconnect[newconnect[(corr.get(k).get(pix).get(0))]]);
						}	
					}
				}
			}
			};
		}
		startAndJoin(threads);
		return finalactu;
	}
	else
	{
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

			// Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
			
				public void run() 
				{	
					for (int superi = ai2.getAndIncrement(); superi <=dimZ; superi = ai2.getAndIncrement())
					{
						int k = superi;
						int pix;
						ImageProcessor ipLab = stackLab.getProcessor(k);
						for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								pix=ipLab.getPixel(i,j);
								if(pix!=0)
									ipLab.putPixel(i,j,newconnect[(corr.get(k).get(pix).get(0))]);
							}
						}
					}
				}
			};
		}
		startAndJoin(threads);
	
		return newactu;
	}
}


public int connect3Db_6(final ImageStack stackLab, final int[] maxval){	
	
	final AtomicInteger ai = new AtomicInteger(1);
	final int dimX = stackLab.getWidth();
	final int dimY = stackLab.getHeight();
	final int dimZ = stackLab.getSize();
	final ArrayList<ArrayList<ArrayList<Integer>>> corr = new ArrayList<ArrayList<ArrayList<Integer>>>();
	final ArrayList<ArrayList<ArrayList<Integer>>> corri = new ArrayList<ArrayList<ArrayList<Integer>>>();
	for(int k=0; k<=dimZ; k++)
	{
		corr.add(new ArrayList<ArrayList<Integer>>());
		corri.add(new ArrayList<ArrayList<Integer>>());
	}
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	// Concurrently run in as many threads as CPUs

	threads[ithread] = new Thread() {
	
	public void run() 
	{	
		for (int superi = ai.getAndIncrement(); superi <=dimZ; superi = ai.getAndIncrement())
		{
			int k = superi;
			
			for(int i=0; i<=maxval[k]; i++)
			{
				corr.get(k).add(new ArrayList<Integer>());
				corr.get(k).get(i).add(0);
			}
			
			if(k<dimZ)
			{
				for(int i=0; i<=maxval[k+1]; i++)
				{
					corri.get(k+1).add(new ArrayList<Integer>());
					corri.get(k+1).get(i).add(0);
				}
				ImageProcessor ip1 = stackLab.getProcessor(k);
				ImageProcessor ip2 = stackLab.getProcessor(k+1);
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<dimX; i++)
					{
						int val2 =(int) (ip2.getPixelValue(i,j));
						if (val2!=0)
						{
							int val1=(int) (ip1.getPixelValue(i,j));
							if (!corr.get(k).get(val1).contains(val2))
							{
								corr.get(k).get(val1).add(val2);
								corri.get(k+1).get(val2).add(-val1);
							}
						}
					}
				}
			}
		}
	}
	};
	}

	startAndJoin(threads);
	
	for(int i=2; i<=dimZ; i++)
	{
		for(int j=1; j<corri.get(i).size();j++)
		{
			for(int l=1; l<corri.get(i).get(j).size();l++)
			{
				corr.get(i).get(j).add(corri.get(i).get(j).get(l));
			}
		}
	}
	final ArrayList<ArrayList<Integer>> connect = new ArrayList<ArrayList<Integer>>();
	connect.add(new ArrayList<Integer>());
	int actu=0;
	for(int i=1; i<=dimZ; i++)
	{
		for(int j=1; j<corr.get(i).size();j++)
		{
			if(corr.get(i).get(j).get(0)==0)
			{
				actu++;
				connect.add(new ArrayList<Integer>());
				connect.get(actu).add(actu);
				connect3Db(corr,connect,actu,i,j);
			}
		}
	}
//	for(int j=1; j<corr.get(dimZ).size();j++)
//	{
//		if(corr.get(dimZ).get(j).get(0)==0)
//		{
//			actu++;
//			connect.add(new ArrayList<Integer>());
//			connect.get(actu).add(actu);
//			corr.get(dimZ).get(j).set(0,actu);
//		}
//	}
	
	int newactu=0;
	final int[] newconnect = new int[actu+1];
	for(int i=1; i<=actu; i++)
	{
		if(connect.get(i).get(0)==i)
		{
			newactu++;
			newconnect[i]=newactu;
		}
		else
		{
			newconnect[i]=newconnect[connect1D(connect,connect.get(i).get(0))];
		}
	}
	
	if(valVoxel!=0)
	{
		final int[][] vol  = new int[dimZ][newactu+1];
		final AtomicInteger ai3 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai3.getAndIncrement(); superi <= dimZ; superi = ai3.getAndIncrement())
			{
				int k = superi;
				ImageProcessor ipLab = stackLab.getProcessor(k);
				for(int j=0;j<dimY;j++)
				{
					for(int  i=0;i<dimX;i++)
					{
						int pix=ipLab.getPixel(i,j);
						if(pix!=0)
							vol[k-1][newconnect[(corr.get(k).get(pix).get(0))]]++;
					}
				}
			}
		}
		};
		}
		startAndJoin(threads);
	
		int[] v = new int[newactu+1];
		for(int i=0; i<dimZ; i++)
		{
			for(int j=1; j<=newactu; j++)
			{
				v[j]+=vol[i][j];
			}
		}
		
		final int[] finalconnect = new int[newactu+1];
		int finalactu=0;
		for(int i=1; i<=newactu; i++)
		{
			if(v[i]<valVoxel)
			{
				finalconnect[i]=0;
			}
			else
			{
				finalactu++;
				finalconnect[i]=finalactu;
			}
		}
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

			// Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
			
			public void run() 
			{	
				for (int superi = ai2.getAndIncrement(); superi <=dimZ; superi = ai2.getAndIncrement())
				{
					int k = superi;
					int pix;
					ImageProcessor ipLab = stackLab.getProcessor(k);
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							pix=ipLab.getPixel(i,j);
							if(pix!=0)
								ipLab.putPixel(i,j,finalconnect[newconnect[(corr.get(k).get(pix).get(0))]]);
						}	
					}
				}
			}
			};
		}
		startAndJoin(threads);
		return finalactu;
	}
	else
	{
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

			// Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
			
				public void run() 
				{	
					for (int superi = ai2.getAndIncrement(); superi <=dimZ; superi = ai2.getAndIncrement())
					{
						int k = superi;
						int pix;
						ImageProcessor ipLab = stackLab.getProcessor(k);
						for(int  j=0;j<dimY;j++)
						{
							for(int  i=0;i<dimX;i++)
							{
								pix=ipLab.getPixel(i,j);
								if(pix!=0)
									ipLab.putPixel(i,j,newconnect[(corr.get(k).get(pix).get(0))]);
							}
						}
					}
				}
			};
		}
		startAndJoin(threads);
	
		return newactu;
	}
}

public int[] connect2D_26(final ImageStack stackBW, final ImageStack stackLab){	
	
	final AtomicInteger ai = new AtomicInteger(1);
	final int dimX = stack.getWidth();
	final int dimY = stack.getHeight();
	final int dimZ = stack.getSize();
	final int[] maxval = new int[dimZ+1];
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	// Concurrently run in as many threads as CPUs

	threads[ithread] = new Thread() {
	
	public void run() 
	{	
		for (int superi = ai.getAndIncrement(); superi <=dimZ; superi = ai.getAndIncrement())
		{
			int k = superi;
			ArrayList<ArrayList<Integer>> corr = new ArrayList<ArrayList<Integer>>();
			corr.add(new ArrayList<Integer>());
			int current=0;
			ImageProcessor ipBW = stackBW.getProcessor(k);
			ImageProcessor ipLab = stackLab.getProcessor(k);
			for( int j=0; j<dimY; j++)
			{
				for( int i=0; i<dimX; i++)
				{
					int val =(int) (ipBW.getPixelValue(i,j));
					if (val==valPixel)
					{
						int[] sup= new int[3];
						sup[0]=0;
						sup[1]=0;
						sup[2]=0;
						if(j>0)
						{
							if(i>0)
							{
							 sup[0]=(int) (ipLab.getPixelValue(i-1,j-1));
							}
							int p=(int) (ipLab.getPixelValue(i,j-1));
							if (p>sup[0])
							{
								sup[1]=sup[0];
								sup[0]=p;
							}
							else
							{
								sup[1]=p;
							}
							if(i<dimX-1)
							{
								p=(int) (ipLab.getPixelValue(i+1,j-1));
								if (p>sup[0])
								{
									sup[2]=sup[1];
									sup[1]=sup[0];
									sup[0]=p;
								}
								else if(p>sup[1])
								{
									sup[2]=sup[1];
									sup[1]=p;
								}
								else
								{
									sup[2]=p;
								}
							}
						}
						int left=0;
						if(i>0)
						{
							 left=(int) (ipLab.getPixelValue(i-1,j));
						}
						if(left==0)
						{
							if(sup[0]==0)
							{
								current++;
								corr.add(new ArrayList<Integer>());
								corr.get(current).add(current);
							}
							else if(sup[0]<current)
							{
								current++;
								corr.add(new ArrayList<Integer>());
								corr.get(current).add(current);
								for(int m=0; m<3; m++)
								{
									if((sup[m]>0)&&(!corr.get(sup[m]).contains(current)))
									{
										corr.get(sup[m]).add(current);
										corr.get(current).add(sup[m]);
									}
									for(int n=m+1; n<3; n++)
									{
										if((sup[n]>0)&&(!corr.get(sup[n]).contains(sup[m])))
										{
											corr.get(sup[n]).add(sup[m]);
											corr.get(sup[m]).add(sup[n]);
										}
									}
								}
							}
							else
							{
								for(int m=0; m<3; m++)
								{
									if((sup[m]>0)&&(sup[m]<current)&&(!corr.get(sup[m]).contains(current)))
									{
										corr.get(sup[m]).add(current);
										corr.get(current).add(sup[m]);
									}
									for(int n=m+1; n<3; n++)
									{
										if((sup[n]>0)&&(sup[n]<current)&&(!corr.get(sup[n]).contains(sup[m])))
										{
											corr.get(sup[n]).add(sup[m]);
											corr.get(sup[m]).add(sup[n]);
										}
									}
								}
							}
						}
						else
						{
							for(int m=0; m<3; m++)
							{
								if((sup[m]>0)&&(sup[m]<current)&&(!corr.get(sup[m]).contains(current)))
								{
									corr.get(sup[m]).add(current);
									corr.get(current).add(sup[m]);
								}
								for(int n=m+1; n<3; n++)
								{
									if((sup[n]>0)&&(sup[n]<current)&&(!corr.get(sup[n]).contains(sup[m])))
									{
										corr.get(sup[n]).add(sup[m]);
										corr.get(sup[m]).add(sup[n]);
									}
								}
							}
						}
						ipLab.putPixel(i,j,current);	
					}
				}
			}
			
			int actu=0;
			for(int i=1; i<=current;i++)
			{
				if(corr.get(i).get(0)==i)
				{
					actu++;
					connect2D(corr,actu,i);
				}
			}
			
			int pix;
			for(int  j=0;j<dimY;j++)
			{
				for(int  i=0;i<dimX;i++)
				{
						pix=ipLab.getPixel(i,j);
						if(pix!=0)
							ipLab.putPixel(i,j,corr.get(pix).get(0));
				}
			}
			maxval[k]=actu;
		}
		}
		};
		}
		startAndJoin(threads);
		return maxval;
	}


public int connect3D_26(final ImageStack stackLab, final int[] maxval){	
	
	final AtomicInteger ai = new AtomicInteger(1);
	final int dimX = stackLab.getWidth();
	final int dimY = stackLab.getHeight();
	final int dimZ = stackLab.getSize();
	final ArrayList<ArrayList<ArrayList<Integer>>> corr = new ArrayList<ArrayList<ArrayList<Integer>>>();
	final ArrayList<ArrayList<ArrayList<Integer>>> corri = new ArrayList<ArrayList<ArrayList<Integer>>>();
	for(int k=0; k<=dimZ; k++)
	{
		corr.add(new ArrayList<ArrayList<Integer>>());
		corri.add(new ArrayList<ArrayList<Integer>>());
	}
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	// Concurrently run in as many threads as CPUs

	threads[ithread] = new Thread() {
	
	public void run() 
	{	
		for (int superi = ai.getAndIncrement(); superi <=dimZ; superi = ai.getAndIncrement())
		{
			int k = superi;
			
			for(int i=0; i<=maxval[k]; i++)
			{
				corr.get(k).add(new ArrayList<Integer>());
				corr.get(k).get(i).add(0);
			}
			
			if(k<dimZ)
			{
				for(int i=0; i<=maxval[k+1]; i++)
				{
					corri.get(k+1).add(new ArrayList<Integer>());
					corri.get(k+1).get(i).add(0);
				}
				ImageProcessor ip1 = stackLab.getProcessor(k);
				ImageProcessor ip2 = stackLab.getProcessor(k+1);
				for( int j=0; j<dimY; j++)
				{
					for( int i=0; i<dimX; i++)
					{
						int val1=(int) (ip1.getPixelValue(i,j));
						if(val1!=0)
						{
							ArrayList<Integer> sup = new ArrayList<Integer>();
							int p=0;
							if(j>0)
							{
								if(i>0)
								{
									p=(int) (ip2.getPixelValue(i-1,j-1));
								}
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
								p=(int) (ip2.getPixelValue(i,j-1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
								if(i<dimX-1)
								{
									p=(int)(ip2.getPixelValue(i+1,j-1));
									if((p!=0)&&(!sup.contains(p)))
										sup.add(p);
								}
							}
							if(i>0)
							{
								p=(int) (ip2.getPixelValue(i-1,j));
							}
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);
							p=(int) (ip2.getPixelValue(i,j));
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);
							if(i<dimX-1)
							{
								p=(int)(ip2.getPixelValue(i+1,j));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(j<dimY-1)
							{
								if(i>0)
								{
									p=(int) (ip2.getPixelValue(i-1,j+1));
								}
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
								p=(int) (ip2.getPixelValue(i,j+1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
								if(i<dimX-1)
								{
									p=(int)(ip2.getPixelValue(i+1,j+1));
									if((p!=0)&&(!sup.contains(p)))
										sup.add(p);
								}
							}

							for(int m=0;m<sup.size();m++)
							{
								if (!corr.get(k).get(val1).contains(sup.get(m)))
								{
									corr.get(k).get(val1).add(sup.get(m));
									corri.get(k+1).get(sup.get(m)).add(-val1);
								}
							}					
						}
					}
				}
			}
		}
	}
	};
	}

	startAndJoin(threads);
	
	for(int i=2; i<=dimZ; i++)
	{
		for(int j=1; j<corri.get(i).size();j++)
		{
			for(int l=1; l<corri.get(i).get(j).size();l++)
			{
				corr.get(i).get(j).add(corri.get(i).get(j).get(l));
			}
		}
	}
	
	final ArrayList<ArrayList<Integer>> connect = new ArrayList<ArrayList<Integer>>();
	connect.add(new ArrayList<Integer>());
	int actu=0;
	for(int i=1; i<=dimZ; i++)
	{
		for(int j=1; j<corr.get(i).size();j++)
		{
			if(corr.get(i).get(j).get(0)==0)
			{
				actu++;
				connect.add(new ArrayList<Integer>());
				connect.get(actu).add(actu);
				connect3Db(corr,connect,actu,i,j);
			}
		}
	}

//	for(int j=1; j<corr.get(dimZ).size();j++)
//	{
//		if(corr.get(dimZ).get(j).get(0)==0)
//		{
//			actu++;
//			connect.add(new ArrayList<Integer>());
//			connect.get(actu).add(actu);
//			corr.get(dimZ).get(j).set(0,actu);
//		}
//	}
	
	int newactu=0;
	final int[] newconnect = new int[actu+1];
	for(int i=1; i<=actu; i++)
	{
		if(connect.get(i).get(0)==i)
		{
			newactu++;
			newconnect[i]=newactu;
		}
		else
		{
			newconnect[i]=newconnect[connect1D(connect,connect.get(i).get(0))];
		}
	}
	
	if(valVoxel!=0)
	{
		final int[][] vol  = new int[dimZ][newactu+1];
		final AtomicInteger ai3 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai3.getAndIncrement(); superi <= dimZ; superi = ai3.getAndIncrement()) {
				int k = superi;
				ImageProcessor ipLab = stackLab.getProcessor(k);
				for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							int pix=ipLab.getPixel(i,j);
							if(pix!=0)
								vol[k-1][newconnect[(corr.get(k).get(pix).get(0))]]++;
						}
					}
			}
		}
		};
		}
		startAndJoin(threads);
	
		int[] v = new int[newactu+1];
		for(int i=0; i<dimZ; i++)
		{
			for(int j=1; j<=newactu; j++)
			{
				v[j]+=vol[i][j];
			}
		}
		
		final int[] finalconnect = new int[newactu+1];
		int finalactu=0;
		for(int i=1; i<=newactu; i++)
		{
			if(v[i]<valVoxel)
			{
				finalconnect[i]=0;
			}
			else
			{
				finalactu++;
				finalconnect[i]=finalactu;
			}
		}
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

			// Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
			
			public void run() 
			{	
				for (int superi = ai2.getAndIncrement(); superi <=dimZ; superi = ai2.getAndIncrement())
				{
					int k = superi;
					int pix;
					ImageProcessor ipLab = stackLab.getProcessor(k);
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							pix=ipLab.getPixel(i,j);
							if(pix!=0)
								ipLab.putPixel(i,j,finalconnect[newconnect[(corr.get(k).get(pix).get(0))]]);
						}	
					}
				}
			}
			};
		}
		startAndJoin(threads);
		return finalactu;
	}
	else
	{
		final AtomicInteger ai2 = new AtomicInteger(1);
		for (int ithread = 0; ithread < threads.length; ithread++) {

			// Concurrently run in as many threads as CPUs

			threads[ithread] = new Thread() {
			
			public void run() 
			{	
				for (int superi = ai2.getAndIncrement(); superi <=dimZ; superi = ai2.getAndIncrement())
				{
					int k = superi;
					int pix;
					ImageProcessor ipLab = stackLab.getProcessor(k);
					for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							pix=ipLab.getPixel(i,j);
							if(pix!=0)
								ipLab.putPixel(i,j,newconnect[(corr.get(k).get(pix).get(0))]);
						}	
					}
				}
			}
			};
		}
		startAndJoin(threads);
		return newactu;
	}
}


int connect1D(final ArrayList<ArrayList<Integer>> connect, int invest)
{
	if(connect.get(invest).get(0)==invest)
	{
		return invest;
	}
	else
	{
		return connect1D(connect,connect.get(invest).get(0));
	}	
}


void connect2D(final ArrayList<ArrayList<Integer>> corr, int label, int invest)
{
	corr.get(invest).set(0,label);
	for(int j=1; j<corr.get(invest).size();j++)
	{
		int current=corr.get(invest).get(j);
		if (current!=label)
		{
			corr.get(invest).set(j,label);
			connect2D(corr,label,current);
		}
	}
	
}

void connect3D(ArrayList<ArrayList<ArrayList<Integer>>> corr, ArrayList<ArrayList<Integer>> connect, int label, int line, int invest)
{
	corr.get(line).get(invest).set(0,label);
	if(line<corr.size()-1)
	for(int j=1; j<corr.get(line).get(invest).size();j++)
	{
			int current=corr.get(line+1).get(corr.get(line).get(invest).get(j)).get(0);
			if (current==0)
			{
				connect3D(corr, connect, label,line+1,corr.get(line).get(invest).get(j));
			}
			else if(current !=label)
			{
				if(connect.get(label).get(0)>current)
					connect.get(label).set(0,current);
			}
	}
}

void connect3Db(ArrayList<ArrayList<ArrayList<Integer>>> corr, ArrayList<ArrayList<Integer>> connect, int label, int line, int invest)
{
	corr.get(line).get(invest).set(0,label);
	for(int j=1; j<corr.get(line).get(invest).size();j++)
	{
		int val=corr.get(line).get(invest).get(j);
		if((val>0)&&(line<corr.size()-1))
		{
			int current=corr.get(line+1).get(val).get(0);
			if (current==0)
			{
				connect3Db(corr, connect, label,line+1,val);
			}
			else if(current !=label)
			{
				if(connect.get(label).get(0)>current)
					connect.get(label).set(0,current);
			}
		}
		else if((val<0)&&(line>2))
		{
			int current=corr.get(line-1).get(-val).get(0);
			if (current==0)
			{
				connect3Db(corr, connect, label,line-1,-val);
			}
			else if(current !=label)
			{
				if(connect.get(label).get(0)>current)
					connect.get(label).set(0,current);
			}
		}
	}
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

