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
//  requires the type of neighbouring rule (6 or 18 or 26 )
//  generates a short int volum
//  Max number of entities is 65500



public class Labeling_3D implements PlugInFilter
{

	/* ------------------------------------------------------------------ */
	/*															VARIABLES															*/
	/* ------------------------------------------------------------------ */

	static int valVoxel=0;	// Minimum size of the object
	static int valPixel=255;	// Color of objects

	int test=1;

	int pix_valPixel=0;
	int pix_0 =0;

	int nbpix,nbrestant;

	int color = 1;
	byte val;

  String [] connectivity = {"6","18","26"};
	String[] periodicChoices = {"x", "y", "z"};
	String[] periodicHeading = {"periodicity", "", ""};
	boolean[] periodic = {false, false, false};
	String [] border = {"yes","no"};

	int size;
	
	protected final Thread[] threads= newThreadArray();	// for parallelization

	ImagePlus imp;
	protected ImageStack stack,stack2;



//	************************************************************************	//
//	************************************************************************	//



	/**
	 * This method sets up the plugin filter for use.
	 *
	 * @param arg
	 *			a string, can be an empty string. The plugin can be installed
	 *			more than once, so that each of the commands can call the same
	 *			plugin class with a different argument.
	 * @param imp
	 *			This is handled by ImageJ and the currently active image is
	 *			passed
	 *
	 * @return	a flag word that represents the filters capabilities (i.e.
	 *			which types of images it can handle).
	 *
	 * @see Labeling_3D#run
	 */
	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_8G+STACK_REQUIRED;
	}



//	************************************************************************	//
//	************************************************************************	//



	/**
	 * This method runs the plugin, what it is implemented here is what the plugin 
	 * actually does.
	 *
	 * @param ip
	 *			Image processor. The processor can be modified directly or a new 
	 *			processor and a new image can be based on its data, so that the
	 *			original image is left unchanged. The original image is locked
	 *			while the plugin is running.
	 *
	 *	@see Labeling_3D#connect2D_6
	 *	@see Labeling_3D#connect2D_26
	 *	@see Labeling_3D#connect3Db_6
	 *	@see Labeling_3D#connect3D_18
	 *	@see Labeling_3D#connect3D_26
	 */
	public void run(ImageProcessor ip)
	{

		/* ------------------------------------------------------------------ */
		/*															DIALOG BOX														*/
		/* ------------------------------------------------------------------ */

		GenericDialog dia = new GenericDialog("3D labeling:", IJ.getInstance());	
		dia.addNumericField("color to label (0 or 255)", valPixel, 0);
		dia.addNumericField("minimum volume to label in voxel", valVoxel, 0);
		dia.addChoice("3D connectivity",connectivity,connectivity[0]);
		dia.addCheckboxGroup(1, 3, periodicChoices, periodic, periodicHeading);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		valPixel = (int) dia.getNextNumber();
		valVoxel = (int) dia.getNextNumber();
		int nb=0;


		/* ------------------------------------------------------------------ */
		/*																	CORE															*/
		/* ------------------------------------------------------------------ */

		// Verify color of objects
		if((valPixel==0) || (valPixel==255))
		{
			// Connectivity
			String numberconnect = connectivity[dia.getNextChoiceIndex()];

			// Periodicity
			for ( int i = 0 ; i < 3 ; i++ )
			{
				periodic[i] = dia.getNextBoolean();
			}
		
			// Output
			stack2 = newShortEmpty(stack);
			
			if(numberconnect=="6")
			{
					int[] maxval=connect2D_6(stack, stack2);
					nb=connect3Db_6(stack2,maxval);
			}
			else if(numberconnect=="18")
			{
					int[] maxval=connect2D_26(stack, stack2);
					nb=connect3D_18(stack2,maxval);
			}	
			else
			{
					int[] maxval=connect2D_26(stack, stack2);
					nb=connect3D_26(stack2,maxval);
			}

			new ImagePlus("Labelled stack with " + nb + " objects", stack2).show();
		}
		else 
		{
			IJ.showMessage("value should be 0 or 255");
		}
	} // END run()
	


//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Label every objects on each slices
	 *
	 * @param stackBW
	 *					The input stack
	 * @param stackLab
	 *					The output stack
	 *
	 * @return a matrix with all objects for each slices
	 *
	 * @see Labeling_3D#connect2D
	 * @see Labeling_3D#startAndJoin
	 */
	public int[] connect2D_6(final ImageStack stackBW, final ImageStack stackLab)
	{	
			
		/* ------------------------------------------------------------------ */
		/*															VARIABLES															*/
		/* ------------------------------------------------------------------ */

		final AtomicInteger ai = new AtomicInteger(1);	// For parallelization

		final int dimX = stackBW.getWidth();
		final int dimY = stackBW.getHeight();
		final int dimZ = stackBW.getSize();

		final int[] maxval = new int[dimZ+1];
		
		/* ------------------------------------------------------------------ */
		/*									DETERMINE EVERY SINGLE OBJECTS										*/
		/* ------------------------------------------------------------------ */

		for (int ithread = 0; ithread < threads.length; ithread++)
		{

			// Concurrently run in as many threads as CPUs
			threads[ithread] = new Thread()
			{
		
				public void run() 
				{
					// objects are independant in each slices, they can be done simultaneously
					for (int superi = ai.getAndIncrement(); superi <=dimZ; superi = ai.getAndIncrement())
					{
						int k = superi; // = z (from 1 to dimZ)

						// corr will contain an array for each object
						ArrayList<ArrayList<Integer>> corr = new ArrayList<ArrayList<Integer>>();
						corr.add(new ArrayList<Integer>());

						int current=0;	// Index of objects

						ImageProcessor ipBW = stackBW.getProcessor(k);
						ImageProcessor ipLab = stackLab.getProcessor(k);

						// Loop over each pixel in the slice
						for( int j=0; j<dimY; j++)
						{
							for( int i=0; i<dimX; i++)
							{
								// Record the value of the current pixel and its neighbors
								// i,j go up, only need to check sup and left neighbors
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
									// If the pixel is an object with no neighbors, create a new
									// object with index "current"
									if(left==0)
									{
										if(sup==0)
										{
											current++;
											corr.add(new ArrayList<Integer>());
											corr.get(current).add(current);
										}
										// Else if the pixel have a neighbor, create a new object
										// and linked to this neighbor
										// Remarq: how "sup" would be > "current"?
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
									// Don't need to check left neigbors, because if left neigbor
									// is not empty, "current" won't evolve and both pixel will be
									// part of the same object
									ipLab.putPixel(i,j,current);	
								}
							}
						}

						// For periodic condition, just check if a pixel in one side correspond
						// to a pixel on the other side
						if(periodic[0])
						{
							for( int j=0; j<dimY; j++)
							{
								int pixelp = (int) (ipLab.getPixelValue(0,j));
								int pixelm = (int) (ipLab.getPixelValue(dimX-1,j));
								if(pixelp!=pixelm && pixelp>0 && pixelm>0)
								{
									corr.get(pixelp).add(pixelm);
									corr.get(pixelm).add(pixelp);
								}
							}
						}
						if(periodic[1])
						{
							for( int i=0; i<dimX; i++)
							{
								int pixelp = (int) (ipLab.getPixelValue(i,0));
								int pixelm = (int) (ipLab.getPixelValue(i,dimY-1));
								if(pixelp!=pixelm && pixelp>0 && pixelm>0)
								{
									corr.get(pixelp).add(pixelm);
									corr.get(pixelm).add(pixelp);
								}
							}
						}
						ipLab.createImage();
						// end of periodicity
			
		/* ------------------------------------------------------------------ */
		/*													CONNECT OBJECTS														*/
		/* ------------------------------------------------------------------ */

						int actu=0;	// To renumber objects correctly

						// Loop over single objects
						for(int i=1; i<=current;i++)
						{ 
							if(corr.get(i).get(0)==i)
							{
								actu++;
								connect2D(corr,actu,i);
							}
						}
					
		/* ------------------------------------------------------------------ */
		/*													PRINT IMAGE																*/
		/* ------------------------------------------------------------------ */

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

		startAndJoin(threads);	// For parallelization

		return maxval;	// Number of object
	}



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Label connected objects in 3D with 6 neighbors
	 *
	 * @param stackLab
	 *					The input stack with objects on each slices
	 * @param maxval
	 *					A matrix with the number of object for each slices
	 *
	 *
	 * @see Labeling_3D#connect3Db
	 * @see Labeling_3D#connect3Dc
	 * @see Labeling_3D#startAndJoin
	 * @see Labeling_3D#connect1D
	 */
	public int connect3Db_6(final ImageStack stackLab, final int[] maxval)
	{	
		
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
		
		for (int ithread = 0; ithread < threads.length; ithread++)
		{

			// Concurrently run in as many threads as CPUs
			threads[ithread] = new Thread()
			{
				
				public void run() 
				{

					for (int superi = ai.getAndIncrement(); superi <=dimZ; superi = ai.getAndIncrement())
					{
						int k = superi;	// Slice number
						
						// corr.get(k) = array with the number of object for the slice k
						for(int i=0; i<=maxval[k]; i++)
						{
							corr.get(k).add(new ArrayList<Integer>());
							corr.get(k).get(i).add(0);
						}
						
						if(k<dimZ)
						{
							// idem but with slice k+1
							for(int i=0; i<=maxval[k+1]; i++)
							{
								corri.get(k+1).add(new ArrayList<Integer>());
								corri.get(k+1).get(i).add(0);
							}
							ImageProcessor ip1 = stackLab.getProcessor(k);
							ImageProcessor ip2 = stackLab.getProcessor(k+1);

							// Loop over every pixels
							for( int j=0; j<dimY; j++)
							{
								for( int i=0; i<dimX; i++)
								{
									int val2 =(int) (ip2.getPixelValue(i,j));
									// If there is an object in the slice k+1
									if (val2!=0)
									{
										int val1=(int) (ip1.getPixelValue(i,j));
										// And if this object is not already recorded
										if (!corr.get(k).get(val1).contains(val2))
										{
											// Declare those objects connected
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

		startAndJoin(threads);	// For parallelization

		// For periodic condition, just check if a pixel in one side correspond
		// to a pixel on the other side
		if(periodic[2])
		{
			ImageProcessor ip1 = stackLab.getProcessor(dimZ);
			ImageProcessor ip2 = stackLab.getProcessor(1);
			for(int i=0; i<=maxval[1]; i++)
			{
				corri.get(1).add(new ArrayList<Integer>());
				corri.get(1).get(i).add(0);
			}
			for( int j=0; j<dimY; j++ )
			{
				for( int i=0; i<dimX; i++ )
				{
					int val2 = (int) (ip2.getPixelValue(i,j));
					if (val2!=0)
					{
						int val1 = (int) (ip1.getPixelValue(i,j));
						if(!corr.get(dimZ).get(val1).contains(val2))
						{
							corr.get(dimZ).get(val1).add(val2);
							corri.get(1).get(val2).add(-val1);
						}
					}
				}
			}

			// Add corri to corr for the first image
			for(int j=1; j<corri.get(1).size();j++)
			{
				for(int l=1; l<corri.get(1).get(j).size();l++)
				{
					corr.get(1).get(j).add(corri.get(1).get(j).get(l));
				}
			}
		} // end periodicity


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
		
		// connect will be use to count objects 
		final ArrayList<ArrayList<Integer>> connect = new ArrayList<ArrayList<Integer>>();
		connect.add(new ArrayList<Integer>());
		int actu=0;
		if(!periodic[2])
		{
			for(int i=1; i<=dimZ; i++)
			{
				for(int j=1; j<corr.get(i).size();j++)
				{
					// corr.get(i).get(j).get(0) will be modify in connect3Db
					if(corr.get(i).get(j).get(0)==0)
					{
						// Number of object
						actu++;
						connect.add(new ArrayList<Integer>());
						connect.get(actu).add(actu);
						connect3Db(corr,connect,actu,i,j);
					}
				}
			}
		}
		else // if periodic along z, use connect3Dc instead
		{
			for(int i=1; i<=dimZ; i++)
			{
				for(int j=1; j<corr.get(i).size();j++)
				{
					if(corr.get(i).get(j).get(0)==0)
					{
						actu++;
						connect.add(new ArrayList<Integer>());
						connect.get(actu).add(actu);
						// If an object goes through the entire stack, there is a bug with
						// connectivity of the first image, The last parameter (0,1) indicates
						// that the structure is periodic (1) or not (0). While calculating
						// the first image, the periodicity is set to 0.
						if( j == 1 )
							connect3Dc(corr,connect,actu,i,j,1);
						else
							connect3Dc(corr,connect,actu,i,j,0);
					}
				}
			}
		}
		
		// Renumber object correctly
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
		
		// valVoxel == 0, so why this condition?
		if(valVoxel!=0)
		{
			final int[][] vol  = new int[dimZ][newactu+1];
			final AtomicInteger ai3 = new AtomicInteger(1);
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
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

			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
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
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
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
								} // end for i
							} // end for j
						} // end for superi
					} // end run()
				}; // end Thread()
			} // end for ithread
			startAndJoin(threads);
		
			return newactu; // number of objects
		} // end if valVoxel=0
	} // END connect3D_6



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Generate a Stack of 2D labelized objects,
	 * see connect2D_6 for more comments !!!
	 *
	 * @param stackLab
	 *					The output stack with objects on each slices
	 * @param stackBW
	 *					The input stack
	 *
	 * @return the number of objects per slice
	 *
	 *
	 * @see Labeling_3D#connect2D
	 * @see Labeling_3D#startAndJoin
	 */
//
	public int[] connect2D_26(final ImageStack stackBW, final ImageStack stackLab)
	{	
		
		final AtomicInteger ai = new AtomicInteger(1);
		final int dimX = stack.getWidth();
		final int dimY = stack.getHeight();
		final int dimZ = stack.getSize();
		final int[] maxval = new int[dimZ+1];
		
		for (int ithread = 0; ithread < threads.length; ithread++)
		{
			// Concurrently run in as many threads as CPUs
			threads[ithread] = new Thread()
			{
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
									// Already initialize, maybe not necessary...
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
										} // end if i < dimX-1
									} // end if j > 0

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
												} // end for n
											} // end for m
										} // end if sup[0] < current
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
												} // end for n
											} // end for m
										} // end if sup[0] >= current && sup[0] != 0
									} // end if left == 0
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
											} // end for n
										} // end for m
									} // end if left != 0
									ipLab.putPixel(i,j,current);	
								} // end if val == valPixel
							} // end for i
						} // end for j

						if(periodic[0])
						{
							// Pixels in corners will be taking into account when defining neighbors
							// pixels.
							for( int j=0; j<dimY; j++)
							{
								int up;
								int middle;
								int down;
								int pixel = (int) ipLab.getPixelValue(dimX-1,j);
									
								if( pixel > 0 )
								{
									if (j < dimY-1)
									{
										up = (int) (ipLab.getPixelValue(0,j+1));
										if( up!=pixel && pixel>0 && up>0 )
										{
											corr.get(pixel).add(up);
											corr.get(up).add(pixel);
										}
									} // end if j < dimY-1
									if (j > 0)
									{
										down = (int) (ipLab.getPixelValue(0,j-1));
										if( down != pixel && down > 0 )
										{
											corr.get(pixel).add(down);
											corr.get(down).add(pixel);
										}
									} // end if j > 0
									middle = (int) (ipLab.getPixelValue(0,j));
									if( middle != pixel && middle > 0 )
									{
										corr.get(pixel).add(middle);
										corr.get(middle).add(pixel);
									}
								} // end if pixel > 0
							} // end for j
						} // end if periodic[0]

						if(periodic[1])
						{
							// Pixels in corners will be taking into account when defining neighbors
							// pixels.
							for( int i=0; i<dimX; i++)
							{
								int left;
								int middle;
								int right;
								int pixel = (int) ipLab.getPixelValue(i,dimY-1);
									
								if( pixel > 0 )
								{
									if( i < dimX-1 )
									{
										left = (int) (ipLab.getPixelValue(i+1,0));
										if( left!=pixel && pixel>0 && left>0 )
										{
											corr.get(pixel).add(left);
											corr.get(left).add(pixel);
										}
									} // end if i < dimX-1
									if (i > 0)
									{
										right = (int) (ipLab.getPixelValue(i-1,0));
										if( right != pixel && right > 0 )
										{
											corr.get(pixel).add(right);
											corr.get(right).add(pixel);
										}
									} // end if i > 0
									middle = (int) (ipLab.getPixelValue(i,0));
									if( middle != pixel && middle > 0 )
									{
										corr.get(pixel).add(middle);
										corr.get(middle).add(pixel);
									}
								} // end if pixel > 0
							} // end for i
						} // end if periodic[1]
						ipLab.createImage();

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
								{
									ipLab.putPixel(i,j,corr.get(pix).get(0));
								}
							} // end for i
						} // end for j
						maxval[k]=actu;
					} // end for superi
				} // end run()
			}; // end Thread()
		} // end for ithread
		startAndJoin(threads);
		return maxval;
	} // END METHOD



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Label connected objects in 3D with 18 neighbors
	 * See connect3D_6 for more comments!!!
	 *
	 * @param stackLab
	 *					The input stack with objects on each slices
	 * @param maxval
	 *					A matrix with the number of object for each slices
	 *
	 *
	 * @see Labeling_3D#connect3Db
	 * @see Labeling_3D#connect3Dc
	 * @see Labeling_3D#startAndJoin
	 * @see Labeling_3D#connect1D
	 */
	public int connect3D_18(final ImageStack stackLab, final int[] maxval)
	{	
		
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
		
		for (int ithread = 0; ithread < threads.length; ithread++)
		{
			// Concurrently run in as many threads as CPUs
			threads[ithread] = new Thread()
			{
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
												p=(int) (ip2.getPixelValue(i,j-1));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										if(j==0&&periodic[1])
										{
												p=(int) (ip2.getPixelValue(i,dimY-1));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										if(i>0)
										{
												p=(int) (ip2.getPixelValue(i-1,j));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										if(i==0&&periodic[0])
										{
												p=(int) (ip2.getPixelValue(dimX-1,j));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										if(j<dimY-1)
										{
												p=(int) (ip2.getPixelValue(i,j+1));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										if(j==dimY-1&&periodic[1])
										{
												p=(int) (ip2.getPixelValue(i,0));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										if(i<dimX-1)
										{
												p=(int) (ip2.getPixelValue(i+1,j));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										if(i==0&&periodic[0])
										{
												p=(int) (ip2.getPixelValue(0,j));
												if((p!=0)&&(!sup.contains(p)))
														sup.add(p);
										}
										p=(int) (ip2.getPixelValue(i,j));
										if((p!=0)&&(!sup.contains(p)))
												sup.add(p);

										for(int m=0;m<sup.size();m++)
										{
											if (!corr.get(k).get(val1).contains(sup.get(m)))
											{
												corr.get(k).get(val1).add(sup.get(m));
												corri.get(k+1).get(sup.get(m)).add(-val1);
											}
										}	// end for m				
									} // end if val1 != 0
								} // end for i
							} // end for j
						} // end if k < dimZ
					} // end for superi
				} // end for run()
			}; // end for Thread()
		} // end for ithread
		startAndJoin(threads);

		// For periodic condition, just check if a pixel in one side correspond
		// to a pixel on the other side
		if(periodic[2])
		{
			ImageProcessor ip1 = stackLab.getProcessor(dimZ);
			ImageProcessor ip2 = stackLab.getProcessor(1);
			for(int i=0; i<=maxval[1]; i++)
			{
				corri.get(1).add(new ArrayList<Integer>());
				corri.get(1).get(i).add(0);
			}
			for( int j=0; j<dimY; j++ )
			{
				for( int i=0; i<dimX; i++ )
				{
					int val1=(int) (ip1.getPixelValue(i,j));
					if(val1!=0)
						{
							ArrayList<Integer> sup = new ArrayList<Integer>();
							int p=0;
							if(j>0)
							{
								p=(int) (ip2.getPixelValue(i,j-1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(j==0&&periodic[1])
							{
								p=(int) (ip2.getPixelValue(i,dimY-1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(i>0)
							{
								p=(int) (ip2.getPixelValue(i-1,j));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(i==0&&periodic[0])
							{
								p=(int) (ip2.getPixelValue(dimX-1,j));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(j<dimY-1)
							{
								p=(int) (ip2.getPixelValue(i,j+1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(j==dimY-1&&periodic[1])
							{
								p=(int) (ip2.getPixelValue(i,0));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(i<dimX-1)
							{
								p=(int) (ip2.getPixelValue(i+1,j));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							if(i==0&&periodic[0])
							{
								p=(int) (ip2.getPixelValue(0,j));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							p=(int) (ip2.getPixelValue(i,j));
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);

							for(int m=0;m<sup.size();m++)
							{
								if (!corr.get(dimZ).get(val1).contains(sup.get(m)))
								{
									corr.get(dimZ).get(val1).add(sup.get(m));
									corri.get(1).get(sup.get(m)).add(-val1);
								}
							} // end for m			
						} // end if val1 != 0
					} // end for i
				} // end for j

			// Add corri to corr for the first image
			for(int j=1; j<corri.get(1).size();j++)
			{
				for(int l=1; l<corri.get(1).get(j).size();l++)
				{
					corr.get(1).get(j).add(corri.get(1).get(j).get(l));
				}
			} // end for j
		}	// end if periodic[2]

		for(int i=2; i<=dimZ; i++)
		{
			for(int j=1; j<corri.get(i).size();j++)
			{
				for(int l=1; l<corri.get(i).get(j).size();l++)
				{
					corr.get(i).get(j).add(corri.get(i).get(j).get(l));
				}
			} // end for i
		} // end for j
		
		final ArrayList<ArrayList<Integer>> connect = new ArrayList<ArrayList<Integer>>();
		connect.add(new ArrayList<Integer>());
		int actu=0;
		if(!periodic[2])
		{
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
				} // end for j
			} // end for i
		} // end if !periodic[2]
		else
		{
			for(int i=1; i<=dimZ; i++)
			{
				//IJ.log("z="+i);
				for(int j=1; j<corr.get(i).size();j++)
				{
					if(corr.get(i).get(j).get(0)==0)
					{
						actu++;
						connect.add(new ArrayList<Integer>());
						connect.get(actu).add(actu);
						//IJ.log("actu="+actu);
						if( j == 1 )
							connect3Dc(corr,connect,actu,i,j,1);
						else
							connect3Dc(corr,connect,actu,i,j,0);
					}
				} // end for j
			} // end for i
		} // end if periodic[2]

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
		} // end for i
		
		if(valVoxel!=0)
		{
			final int[][] vol  = new int[dimZ][newactu+1];
			final AtomicInteger ai3 = new AtomicInteger(1);
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
				
					public void run() 
					{	
						for (int superi = ai3.getAndIncrement(); superi <= dimZ; superi = ai3.getAndIncrement())
						{
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
							} // end for j
						} // end for superi
					} // end run()
				}; // end Thread()
			} // end for ithread
			startAndJoin(threads);
		
			int[] v = new int[newactu+1];
			for(int i=0; i<dimZ; i++)
			{
				for(int j=1; j<=newactu; j++)
				{
					v[j]+=vol[i][j];
				}
			} // end for i
			
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
			} // end for i

			final AtomicInteger ai2 = new AtomicInteger(1);
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
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
							} // end for j
						} // end for superi
					} // end run()
				}; // end Thread()
			} // end for ithread
			startAndJoin(threads);
			return finalactu;
		} // end if valVoxel != 0
		else
		{
			final AtomicInteger ai2 = new AtomicInteger(1);
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
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
							} // end for j
						} // end for superi
					} // end run()
				}; // end Thread()
			} // end for ithread
			startAndJoin(threads);
			return newactu;
		} // end if valVoxel == 0
	} // END METHOD



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Label connected objects in 3D with 26 neighbors
	 * See connect3D_6 for more comments!!!
	 *
	 * @param stackLab
	 *					The input stack with objects on each slices
	 * @param maxval
	 *					A matrix with the number of object for each slices
	 *
	 *
	 * @see Labeling_3D#connect3Db
	 * @see Labeling_3D#connect3Dc
	 * @see Labeling_3D#startAndJoin
	 * @see Labeling_3D#connect1D
	 */
	public int connect3D_26(final ImageStack stackLab, final int[] maxval)
	{	
		
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
		
		for (int ithread = 0; ithread < threads.length; ithread++)
		{
			// Concurrently run in as many threads as CPUs
			threads[ithread] = new Thread()
			{
			
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
											else if(i==0 && periodic[0])
											{
												p=(int) (ip2.getPixelValue(dimX-1,j-1));
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
											else if(i==dimX-1 && periodic[0])
											{
												p=(int)(ip2.getPixelValue(dimX-1,j-1));
												if((p!=0)&&(!sup.contains(p)))
													sup.add(p);
											}
										} // end if j > 0
										else if(j==0 && periodic[1])
										{
											if(i>0)
											{
												p=(int) (ip2.getPixelValue(i-1,dimY-1));
											}
											else if(i==0 && periodic[0])
											{
												p=(int) (ip2.getPixelValue(dimX-1,dimY-1));
											}
											if((p!=0)&&(!sup.contains(p)))
												sup.add(p);
											p=(int) (ip2.getPixelValue(i,dimY-1));
											if((p!=0)&&(!sup.contains(p)))
												sup.add(p);
											if(i<dimX-1)
											{
												p=(int)(ip2.getPixelValue(i+1,dimY-1));
												if((p!=0)&&(!sup.contains(p)))
													sup.add(p);
											}
											else if(i==dimX-1 && periodic[0])
											{
												p=(int)(ip2.getPixelValue(0,dimY-1));
												if((p!=0)&&(!sup.contains(p)))
													sup.add(p);
											}
										} // end if j == 0 && periodic[1]

										if(i>0)
										{
											p=(int) (ip2.getPixelValue(i-1,j));
										}
										else if (i==0 && periodic[0])
										{
											p=(int) (ip2.getPixelValue(dimX-1,j));
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
										else if(i==dimX-1 && periodic[0])
										{
											p=(int)(ip2.getPixelValue(0,j));
											if((p!=0)&&(!sup.contains(p)))
												sup.add(p);
										}
										
										if(j<dimY-1)
										{
											if(i>0)
											{
												p=(int) (ip2.getPixelValue(i-1,j+1));
											}
											else if(i==0 && periodic[0]) 
											{
												p=(int) (ip2.getPixelValue(dimX-1,j+1));
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
											else if(i==dimX-1 && periodic[0])
											{
												p=(int)(ip2.getPixelValue(0,j+1));
												if((p!=0)&&(!sup.contains(p)))
													sup.add(p);
											}
										} // end if j < dimY-1
										else if(j==dimY-1 && periodic[1])
										{
											if(i>0)
											{
												p=(int) (ip2.getPixelValue(i-1,0));
											}
											else if(i==0 && periodic[0])
											{
												p=(int) (ip2.getPixelValue(dimX-1,0));
											}
											if((p!=0)&&(!sup.contains(p)))
												sup.add(p);
											p=(int) (ip2.getPixelValue(i,0));
											if((p!=0)&&(!sup.contains(p)))
												sup.add(p);
											if(i<dimX-1)
											{
												p=(int)(ip2.getPixelValue(i+1,0));
												if((p!=0)&&(!sup.contains(p)))
													sup.add(p);
											}
											else if(i==dimX-1 && periodic[0])
											{
												p=(int)(ip2.getPixelValue(0,0));
												if((p!=0)&&(!sup.contains(p)))
													sup.add(p);
											}
										} // end if j == dimY-1 && periodic[1]

										for(int m=0;m<sup.size();m++)
										{
											if (!corr.get(k).get(val1).contains(sup.get(m)))
											{
												corr.get(k).get(val1).add(sup.get(m));
												corri.get(k+1).get(sup.get(m)).add(-val1);
											}
										}	// end for m				
									} // end if val1 != 0
								} // end for i
							} // end for j
						} // end if k < dimZ
					} // end for superi 
				} // end run()
			}; // end Thread()
		} // end for ithread
		startAndJoin(threads);

		// For periodic condition, just check if a pixel in one side correspond
		// to a pixel on the other side
		if(periodic[2])
		{
			ImageProcessor ip1 = stackLab.getProcessor(dimZ);
			ImageProcessor ip2 = stackLab.getProcessor(1);
			for(int i=0; i<=maxval[1]; i++)
			{
				corri.get(1).add(new ArrayList<Integer>());
				corri.get(1).get(i).add(0);
			}
			for( int j=0; j<dimY; j++ )
			{
				for( int i=0; i<dimX; i++ )
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
							else if(i==0 && periodic[0])
							{
								p=(int) (ip2.getPixelValue(dimX-1,j-1));
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
							else if(i==dimX-1 && periodic[0])
							{
								p=(int)(ip2.getPixelValue(dimX-1,j-1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
						} // end if j > 0
						else if(j==0 && periodic[1])
						{
							if(i>0)
							{
								p=(int) (ip2.getPixelValue(i-1,dimY-1));
							}
							else if(i==0 && periodic[0])
							{
								p=(int) (ip2.getPixelValue(dimX-1,dimY-1));
							}
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);
							p=(int) (ip2.getPixelValue(i,dimY-1));
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);
							if(i<dimX-1)
							{
								p=(int)(ip2.getPixelValue(i+1,dimY-1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							else if(i==dimX-1 && periodic[0])
							{
								p=(int)(ip2.getPixelValue(0,dimY-1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
						} // end if j == 0 && periodic[1]

						if(i>0)
						{
							p=(int) (ip2.getPixelValue(i-1,j));
						}
						else if (i==0 && periodic[0])
						{
							p=(int) (ip2.getPixelValue(dimX-1,j));
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
						else if(i==dimX-1 && periodic[0])
						{
							p=(int)(ip2.getPixelValue(0,j));
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);
						}
						
						if(j<dimY-1)
						{
							if(i>0)
							{
								p=(int) (ip2.getPixelValue(i-1,j+1));
							}
							else if(i==0 && periodic[0]) 
							{
								p=(int) (ip2.getPixelValue(dimX-1,j+1));
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
							else if(i==dimX-1 && periodic[0])
							{
								p=(int)(ip2.getPixelValue(0,j+1));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
						} // end if j < dimY-1
						else if(j==dimY-1 && periodic[1])
						{
							if(i>0)
							{
								p=(int) (ip2.getPixelValue(i-1,0));
							}
							else if(i==0 && periodic[0])
							{
								p=(int) (ip2.getPixelValue(dimX-1,0));
							}
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);
							p=(int) (ip2.getPixelValue(i,0));
							if((p!=0)&&(!sup.contains(p)))
								sup.add(p);
							if(i<dimX-1)
							{
								p=(int)(ip2.getPixelValue(i+1,0));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
							else if(i==dimX-1 && periodic[0])
							{
								p=(int)(ip2.getPixelValue(0,0));
								if((p!=0)&&(!sup.contains(p)))
									sup.add(p);
							}
						} // end if j == dimY-1 && periodic[1]

						for(int m=0;m<sup.size();m++)
						{
							if (!corr.get(dimZ).get(val1).contains(sup.get(m)))
							{
								corr.get(dimZ).get(val1).add(sup.get(m));
								corri.get(1).get(sup.get(m)).add(-val1);
							}
						}	// end for m				
					} // end if val1 != 0
				} // end for i
			} // end for j

			// Add corri to corr for the first image
			for(int j=1; j<corri.get(1).size();j++)
			{
				for(int l=1; l<corri.get(1).get(j).size();l++)
				{
					corr.get(1).get(j).add(corri.get(1).get(j).get(l));
				}
			} // end for j
		} // end if periodic[2]

		for(int i=2; i<=dimZ; i++)
		{
			for(int j=1; j<corri.get(i).size();j++)
			{
				for(int l=1; l<corri.get(i).get(j).size();l++)
				{
					corr.get(i).get(j).add(corri.get(i).get(j).get(l));
				}
			} // end for j
		} // end for i
		
		final ArrayList<ArrayList<Integer>> connect = new ArrayList<ArrayList<Integer>>();
		connect.add(new ArrayList<Integer>());
		int actu=0;
		if(!periodic[2])
		{
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
				} // end for j
			} // end for i
		} // end if !periodic[2]
		else
		{
			for(int i=1; i<=dimZ; i++)
			{
				for(int j=1; j<corr.get(i).size();j++)
				{
					if(corr.get(i).get(j).get(0)==0)
					{
						actu++;
						connect.add(new ArrayList<Integer>());
						connect.get(actu).add(actu);
						if( j == 1 )
							connect3Dc(corr,connect,actu,i,j,1);
						else
							connect3Dc(corr,connect,actu,i,j,0);
					}
				} // end for j
			} // end for i
		} // end if periodic[2]
		
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
		} // end for i
		
		if(valVoxel!=0)
		{
			final int[][] vol  = new int[dimZ][newactu+1];
			final AtomicInteger ai3 = new AtomicInteger(1);
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
					public void run() 
					{	
						for (int superi = ai3.getAndIncrement(); superi <= dimZ; superi = ai3.getAndIncrement())
						{
							int k = superi;
							ImageProcessor ipLab = stackLab.getProcessor(k);
							for(int  j=0;j<dimY;j++)
							{
								for(int  i=0;i<dimX;i++)
								{
									int pix=ipLab.getPixel(i,j);
									if(pix!=0)
									{
										vol[k-1][newconnect[(corr.get(k).get(pix).get(0))]]++;
									}
								} // end for i
							} // end for j
						} // end for superi
					} // end run()
				}; // end Thread()
			} // end for ithread
			startAndJoin(threads);
		
			int[] v = new int[newactu+1];
			for(int i=0; i<dimZ; i++)
			{
				for(int j=1; j<=newactu; j++)
				{
					v[j]+=vol[i][j];
				}
			} // end for i
			
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
			} // end for i
			final AtomicInteger ai2 = new AtomicInteger(1);
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
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
									{
										ipLab.putPixel(i,j,finalconnect[newconnect[(corr.get(k).get(pix).get(0))]]);
									}
								}	// end for i
							} // end for j
						} // end for superi
					} // end run()
				}; // end Thread()
			} // end for ithread
			startAndJoin(threads);
			return finalactu;
		} // end if valVoxel != 0
		else
		{
			final AtomicInteger ai2 = new AtomicInteger(1);
			for (int ithread = 0; ithread < threads.length; ithread++)
			{
				// Concurrently run in as many threads as CPUs
				threads[ithread] = new Thread()
				{
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
							} // end for i
						} // end for superi
					} // end run()
				}; // end Thread()
			} // end for ithread
			startAndJoin(threads);
			return newactu;
		} // end if valVoxel == 0
	} // END METHOD



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Find connecting number in a 2D array (object)(connect to)
	 *
	 * @param connect
	 *					The matrix containing objects and their connections
	 * @param invest
	 *					The object to invest
	 *
	 */
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
	} // END METHOD



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Find connecting number in a 3D array (object)(connect to)
	 *
	 * @param corr
	 *					The matrix containing objects and their connections
	 * @param label
	 *					The actualized number of object
	 * @param invest
	 *					The object to invest
	 *
	 */
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
		} // end for j
	} // END METHOD



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Find connecting number in a 3D array (object)(connect to) with no periodic
	 * conditions
	 *
	 * @param corr
	 *					The matrix containing objects and their connections
	 * @param connect
	 *					connection between objects
	 * @param label
	 *					The actualized number of object
	 * @param line
	 *					The slice studied
	 * @param invest
	 *					The object to invest
	 *
	 */
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
		} // end for j
	} // END METHOD



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Find connecting number in a 3D array (object)(connect to) with no periodic
	 * conditions
	 *
	 * @param corr
	 *					The matrix containing objects and their connections
	 * @param connect
	 *					connection between objects
	 * @param label
	 *					The actualized number of object
	 * @param line
	 *					The slice studied
	 * @param invest
	 *					The object to invest
	 *
	 */
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
			} // end if val > 0 && line < corr.size()-1
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
			} // end if val < 0 && line > 2
		} // end for j
	} // END METHOD




//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Find connecting number in a 3D array (object)(connect to) with periodic
	 * conditions
	 *
	 * @param corr
	 *					The matrix containing objects and their connections
	 * @param connect
	 *					connection between objects
	 * @param label
	 *					The actualized number of object
	 * @param line
	 *					The slice studied
	 * @param invest
	 *					The object to invest
	 *
	 */
	void connect3Dc(ArrayList<ArrayList<ArrayList<Integer>>> corr, ArrayList<ArrayList<Integer>> connect, int label, int line, int invest, int first)
	{
		corr.get(line).get(invest).set(0,label);
		for(int j=1; j<corr.get(line).get(invest).size();j++)
		{
			int val=corr.get(line).get(invest).get(j);
			if((val>0))
			{
				if(line<corr.size()-1)
				{
					int current=corr.get(line+1).get(val).get(0);
					if (current==0)
					{
						connect3Dc(corr, connect, label,line+1,val,first);
					}
					else if(current !=label)
					{
						if(connect.get(label).get(0)>current)
							connect.get(label).set(0,current);
					}
				} // end if line < corr.size()-1
				else if( line == corr.size()-1 && first == 0)
				{
					int current=corr.get(1).get(val).get(0);
					if (current==0)
					{
						connect3Dc(corr, connect, label,1,val,first);
					}
					else if(current !=label)
					{
						if(connect.get(label).get(0)>current)
							connect.get(label).set(0,current);
					}
				} // end if liine == corr.size()-1 && first == 0
			}
			else if((val<0))
			{
				if((line>2))
				{
					int current=corr.get(line-1).get(-val).get(0);
					if (current==0)
					{
						connect3Dc(corr, connect, label,line-1,-val,first);
					}
					else if(current !=label)
					{
						if(connect.get(label).get(0)>current)
						{
							connect.get(label).set(0,current);
						}
					} // end if current != label
				} // end if line > 2
				else if( line > 2 && first == 0 )
				{
					int current=corr.get(corr.size()-1).get(-val).get(0);
					if (current==0)
					{
						connect3Dc(corr, connect, label,corr.size()-1,-val,first);
					}
					else if(current !=label)
					{
						if(connect.get(label).get(0)>current)
						{
							connect.get(label).set(0,current);
						}
					} // end if current != label
				} // end if line > 2 && first == 0
			} // end if val < 0
		} // end for j
	} // END METHOD



//	************************************************************************	//
//	************************************************************************	//
	


	/**
	 * Create a copy of the working stack
	 *
	 * @param src
	 *					The input stack
	 *
	 */
	public static ImageStack newShortEmpty(ImageStack src)
	{
    int xSize = src.getWidth();
    int ySize = src.getHeight();
    int zSize = src.getSize();

    ImageStack dest = new ImageStack(xSize, ySize);
    for (int z = 1; z <= zSize; ++z)
		{
      dest.addSlice(src.getSliceLabel(z),
          new ShortProcessor(xSize, ySize));
    }
    dest.setColorModel(src.getColorModel());
    return dest;
  }



//	************************************************************************	//
//	************************************************************************	//



/** 
 * Create a Thread[] array as large as the number of processors available.
 *
 * 
 * @author	Stephan Preibisch's Multithreading.java class.
 * 
 * See http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD
 *
 */
private Thread[] newThreadArray() {
  int n_cpus = Runtime.getRuntime().availableProcessors();
	return new Thread[n_cpus];
}



//	************************************************************************	//
//	************************************************************************	//



/** 
 * Start all given threads and wait on each of them until all are done.
 * 
 * @author	Stephan Preibisch's Multithreading.java class. See:
 * 
 * See http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD
 *
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
		}
		catch (InterruptedException ie)
		{
				throw new RuntimeException(ie);
		}
	} // END METHOD

} // END CLASS
