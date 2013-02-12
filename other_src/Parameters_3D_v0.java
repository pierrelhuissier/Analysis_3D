import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.io.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Parameters_3D_v0 implements PlugInFilter {

	protected ImageStack stack;
	InputStream pin;
	int nb;
	int dimx,dimy,dimz;
	static int valPixel = 0;
	
	ImagePlus imp;

	protected final Thread[] threads= newThreadArray();

	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_16;
	}



public void run(ImageProcessor ip) {

		dimz =stack.getSize();
		dimy = stack.getWidth();
		dimx = stack.getHeight();

		SaveDialog sd = new SaveDialog("Save Measurements as Text...", "res", ".dat");
        String name = sd.getFileName();
       	if (name == null)
            	return;
        
       	String directory = sd.getDirectory();

		nb=calculnb(stack);//-1;
		IJ.showStatus("Measure parameters for the "+nb+" objects ...");
		if(nb<1)
		{
			IJ.showMessage("volume must be labeled");
		}
		else
		{
       		double[] volume_m = new double[nb];
       		int[] volume_p = new int[nb];
       		double[] surface = new double[nb];
       		double[] surfacenb = new double[nb];
       		double[][][] I = new double[3][3][nb];
       		double[][] J = new double[3][nb];
       		double[][][] dir = new double[3][3][nb];
       		double[] xg = new double[nb];
       		double[] yg = new double[nb];
       		double[] zg = new double[nb]; 
       		byte[] bord = new byte[nb]; 
//       		double[] a = new double[nb];
//       		double[] b = new double[nb];
//       		double[] c = new double[nb];
//       		double[] Fab = new double[nb];
//       		double[] Fac = new double[nb];
//       		double[] Fbc = new double[nb];
//       		double[] sp = new double[nb];
       		double[][] lmin = new double[nb][3];
       		double[][] lmax = new double[nb][3];
       		IJ.showStatus("Measure surfaces ...");
      		calculmarchsurfstack(stack,nb,surface,volume_m);
      		calculmarchsurfstacknb(stack,nb,surfacenb);
      		//calculvolumestack(stack,nb,volume_p);
      		IJ.showStatus("Measure volumes and inertia ...");
      		calculcgstack(stack,nb,volume_p,xg, yg,zg);
            calculinertiestack(stack,nb,xg,yg,zg,I);
            inertie(nb,I,J,dir);
            IJ.showStatus("Measure bounding boxes ...");
            boitestack(stack,nb,xg,yg,zg,dir,lmin,lmax);
            borderstack(stack, nb, bord);
            IJ.showStatus("Save results ...");
            sauvegarde(volume_p,volume_m,surface,surfacenb,xg,yg,zg,J,dir,nb,bord,lmin,lmax,directory,name);
            volume_m = null;
            volume_p = null;
            surface = null;
            xg = null;
            yg = null;
            zg = null;
		}
}

public int calculnb(final ImageStack stack1)
{
	final AtomicInteger ai = new AtomicInteger(1);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final int[] value  = new int[dimZ];
	
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
				int current = superi;
				ImageProcessor ip = stack1.getProcessor(current);
				for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							int val =(int) (ip.getPixelValue(i,j));
							if (val>value[current-1]) value[current-1]=val;
						}
					}
			}
		}
		};
	}
	startAndJoin(threads);
		
	int val=0;
	for(int i=0; i<dimZ; i++)
	{
		if (val<value[i]) val=value[i];
	}
return val;
}

void calculmarchsurfstack(ImageStack stack,int nb, double s[], double v[])
{
	double[] cas = new double [22];	
	double[] tri = new double [256];	
	double[] cas2 = new double [22];	
	double[] tri2 = new double [256];	
	initcas_s2(cas);
	initincrement(tri,cas);
	initcas_v2_luc(cas2);
	initincrement(tri2,cas2);
	calculsurfacemarch2stack(stack,nb,tri,s,tri2,v);
}

void initcas_s2(double []cas)
{
    cas[0]= 0.00000;
    cas[1] = 0.216506;
    cas[2] = 0.707106;
    cas[3] = 0.750000;
    cas[4] = 2.062500;
    cas[5] = 1.149519;
   cas[6] = 0.923613;
    cas[7] = 0.649519;
    cas[8] = 1.000000;
    cas[9] = 1.299039;
    cas[10] = 1.414214;
    cas[11] = 1.573132;
    cas[12] = 1.366026;
    cas[13] = 0.866026;
    cas[14] = 0.560250;
    cas[15] = 0.923613;
   cas[16] = 0.649519;
    cas[17] = 0.707106;
    cas[18] = 0.433013;
    cas[19] = 0.433013;
    cas[20] = 0.216506;
    cas[21] = 0.000000;

}

void initcas_v2_luc(double []cas)
{
    cas[0] = 0.00000;
    cas[1] = 0.020833;
    cas[2] = 0.125;
    cas[3] =0.16666;
    cas[4] = 0.498102;
    cas[5] = 0.354166;
    cas[6] = 0.295047;
    cas[7] =0.479166;
    cas[8] = 0.500000;
    cas[9] = 0.500000;
    cas[10] = 0.750000;
    cas[11] = 0.437500;
    cas[12] =0.625;
    cas[13] = 0.91667;
    cas[14] = 0.75000;
    cas[15] = 0.8541667;
    cas[16] = 0.9375;
    cas[17] =0.875;
    cas[18] =0.958333;
    cas[19] = 0.958333;
    cas[20] = 0.9791667;
    cas[21] = 1.000000;

}

void initincrement(double []tri,double []cas)

{
    tri[0] = cas[0];
    tri[1] = cas[1];
    tri[2] = cas[1];
    tri[3] = cas[2];
    tri[4] = cas[1];
    tri[5] = cas[2];
    tri[6] = cas[3];
    tri[7] = cas[5];
    tri[8] = cas[1];
    tri[9] = cas[3];
    tri[10] = cas[2];

    tri[11] = cas[5];
    tri[12] = cas[2];
    tri[13] = cas[5];
    tri[14] = cas[5];
    tri[15] = cas[8];
    tri[16] = cas[1];
    tri[17] = cas[2];
    tri[18] = cas[3];
    tri[19] = cas[5];
    tri[20] = cas[3];
   

    tri[21] = cas[5];
    tri[22] = cas[7];
    tri[23] = cas[9];
    tri[24] = cas[4];
    tri[25] = cas[6];
    tri[26] = cas[6];
    tri[27] = cas[11];
    tri[28] = cas[6];
    tri[29] = cas[11];
    tri[30] = cas[12];


    tri[31] = cas[14];
    tri[32] = cas[1];
    tri[33] = cas[3];
    tri[34] = cas[2];
    tri[35] = cas[5];
    tri[36] = cas[4];
    tri[37] = cas[6];
    tri[38] = cas[6];
    tri[39] = cas[11];
    tri[40] = cas[3];

    tri[41] = cas[7];
    tri[42] = cas[5];
    tri[43] = cas[9];
    tri[44] = cas[6];
    tri[45] = cas[12];
    tri[46] = cas[11];
    tri[47] = cas[14];
    tri[48] = cas[2];
    tri[49] = cas[5];
    tri[50] = cas[5];

    tri[51] = cas[8];
    tri[52] = cas[6];
    tri[53] = cas[11];
    tri[54] = cas[12];
    tri[55] = cas[14];
    tri[56] = cas[6];
    tri[57] = cas[12];
    tri[58] = cas[11];
    tri[59] = cas[14];
    tri[60] = cas[10];

    tri[61] = cas[15];
    tri[62] = cas[15];
    tri[63] = cas[17];
    tri[64] = cas[1];
    tri[65] = cas[3];
    tri[66] = cas[4];
    tri[67] = cas[6];
    tri[68] = cas[2];
    tri[69] = cas[5];
    tri[70] = cas[6];

    tri[71] = cas[11];
    tri[72] = cas[3];
    tri[73] = cas[7];
    tri[74] = cas[6];
    tri[75] = cas[12];
    tri[76] = cas[5];
    tri[77] = cas[9];
    tri[78] = cas[11];
    tri[79] = cas[14];
    tri[80] = cas[2];

    tri[81] = cas[5];
    tri[82] = cas[6];
    tri[83] = cas[11];
    tri[84] = cas[5];
    tri[85] = cas[8];
    tri[86] = cas[12];
    tri[87] = cas[14];
    tri[88] = cas[6];
    tri[89] = cas[12];
    tri[90] = cas[10];

    tri[91] = cas[15];
    tri[92] = cas[11];
    tri[93] = cas[14];
    tri[94] = cas[15];
    tri[95] = cas[17];
    tri[96] = cas[3];
    tri[97] = cas[7];
    tri[98] = cas[6];
    tri[99] = cas[12];
    tri[100] = cas[6];

    tri[101] = cas[12];
    tri[102] = cas[10];
    tri[103] = cas[15];
    tri[104] = cas[7];
    tri[105] = cas[13];
    tri[106] = cas[12];
    tri[107] = cas[16];
    tri[108] = cas[12];
    tri[109] = cas[16];
    tri[110] = cas[15];
    
    tri[111] = cas[18];
    tri[112] = cas[5];
    tri[113] = cas[9];
    tri[114] = cas[11];
    tri[115] = cas[14];
    tri[116] = cas[11];
    tri[117] = cas[14];
    tri[118] = cas[15];
    tri[119] = cas[17];
    tri[120] = cas[12];
    
    tri[121] = cas[16];
    tri[122] = cas[15];
    tri[123] = cas[18];
    tri[124] = cas[15];
    tri[125] = cas[18];
    tri[126] = cas[19];
    tri[127] = cas[20];
    tri[128] = cas[1];
    tri[129] = cas[4];
    tri[130] = cas[3];
    
    tri[131] = cas[6];
    tri[132] = cas[3];
    tri[133] = cas[6];
    tri[134] = cas[7];
    tri[135] = cas[12];
    tri[136] = cas[2];
    tri[137] = cas[20];
    tri[138] = cas[5];
    tri[139] = cas[11];
    tri[140] = cas[5];
    
    tri[141] = cas[11];
    tri[142] = cas[9];
    tri[143] = cas[14];
    tri[144] = cas[3];
    tri[145] = cas[6];
    tri[146] = cas[7];
    tri[147] = cas[12];
    tri[148] = cas[7];
    tri[149] = cas[12];
    tri[150] = cas[13];
    
    tri[151] = cas[16];
    tri[152] = cas[6];
    tri[153] = cas[10];
    tri[154] = cas[12];
    tri[155] = cas[15];
    tri[156] = cas[12];
    tri[157] = cas[15];
    tri[158] = cas[16];
    tri[159] = cas[18];
    tri[160] = cas[2];

    tri[161] = cas[6];
    tri[162] = cas[5];
    tri[163] = cas[11];
    tri[164] = cas[6];
    tri[165] = cas[10];
    tri[166] = cas[12];
    tri[167] = cas[15];
    tri[168] = cas[5];
    tri[169] = cas[12];
    tri[170] = cas[8];

    tri[171] = cas[14];
    tri[172] = cas[11];
    tri[173] = cas[15];
    tri[174] = cas[14];
    tri[175] = cas[17];
    tri[176] = cas[5];
    tri[177] = cas[11];
    tri[178] = cas[9];
    tri[179] = cas[14];
    tri[180] = cas[12];

    tri[181] = cas[15];
    tri[182] = cas[17];
    tri[183] = cas[18];
    tri[184] = cas[11];
    tri[185] = cas[15];
    tri[186] = cas[14];
    tri[187] = cas[17];
    tri[188] = cas[15];
    tri[189] = cas[19];
    tri[190] = cas[18];

    tri[191] = cas[20];
    tri[192] = cas[2];
    tri[193] = cas[6];
    tri[194] = cas[6];
    tri[195] = cas[10];
    tri[196] = cas[5];
    tri[197] = cas[11];
    tri[198] = cas[12];
    tri[199] = cas[15];
    tri[200] = cas[5];

    tri[201] = cas[12];
    tri[202] = cas[11];
    tri[203] = cas[15];
    tri[204] = cas[8];
    tri[205] = cas[14];
    tri[206] = cas[14];
    tri[207] = cas[17];
    tri[208] = cas[5];
    tri[209] = cas[11];
    tri[210] = cas[12];

    tri[211] = cas[15];
    tri[212] = cas[9];
    tri[213] = cas[14];
    tri[214] = cas[16];
    tri[215] = cas[18];
    tri[216] = cas[11];
    tri[217] = cas[15];
    tri[218] = cas[15];
    tri[219] = cas[19]; 
    tri[220] = cas[14];

    tri[221] = cas[17];
    tri[222] = cas[18];
    tri[223] = cas[20];
    tri[224] = cas[5];
    tri[225] = cas[12];
    tri[226] = cas[11];
    tri[227] = cas[15];
    tri[228] = cas[11];
    tri[229] = cas[15];
    tri[230] = cas[15];

    tri[231] = cas[19];
    tri[232] = cas[9];
    tri[233] = cas[16];
    tri[234] = cas[14];
    tri[235] = cas[18];
    tri[236] = cas[14];
    tri[237] = cas[18];
    tri[238] = cas[17];
    tri[239] = cas[20];
    tri[240] = cas[8];

    tri[241] = cas[14];
    tri[242] = cas[14];
    tri[243] = cas[17];
    tri[244] = cas[14];
    tri[245] = cas[17];
    tri[246] = cas[18];
    tri[247] = cas[20];
    tri[248] = cas[14];
    tri[249] = cas[18];
    tri[250] = cas[17];

    tri[251] = cas[20];
    tri[252] = cas[17];
    tri[253] = cas[20];
    tri[254] = cas[20];
    tri[255] = cas[21];
}

void calculsurfacemarch2stack(final ImageStack stack1, int nb, final double[] tri, double[] s, final double[] tri2, double[] v)
{
	final AtomicInteger ai = new AtomicInteger(-1);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final double[][] vol  = new double[dimZ][nb];
	final double[][] surf  = new double[dimZ][nb];
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip1,ip2;
				if (k>=0)  ip1 = stack1.getProcessor(k+1);
				else ip1 = stack1.getProcessor(10);
				if (k<=dimZ-2) ip2 = stack1.getProcessor(k+2);
				else ip2 = stack1.getProcessor(10);
				for(int j=-1;j<dimY;j++)
				{
						for(int i=-1;i<dimX;i++)
						{
							int [] p = new int[8];
							int p1,p2,p3,p4,p5,p6,p7,p8,ptot;
					        int [] nontab = new int[8];
					        
							ptot=(short)0;
					        if (j<0 || i<0 || k<0) p[0]=(short)0;
					        else p[0]=(int)ip1.getPixelValue(i,j);

					        if (j==dimY-1 || i<0 || k<0) p[1]=(short)0;
					        else p[1]=(int)ip1.getPixelValue(i,j+1);

					        if(k==dimZ-1 || i<0 || j<0) p[2]=(short)0;
					        else p[2]=(int)ip2.getPixelValue(i,j);

					        if(k==dimZ-1 || j==dimY-1 || i<0) p[3]=(short)0;
					              else p[3]=(int)ip2.getPixelValue(i,j+1);

					        if(i==dimX-1 || k<0 || j<0) p[4]=(short)0;
					        else p[4]=(int)ip1.getPixelValue(i+1,j);

					        if(i==dimX-1 || j==dimY-1 || k<0) p[5]=(short)0;
					        else p[5]=(int)ip1.getPixelValue(i+1,j+1);

					        if(k==dimZ-1 || i==dimX-1 || j<0) p[6]=(short)0;
					        else p[6]=(int)ip2.getPixelValue(i+1,j);

					        if(k==dimZ-1 || j==dimY-1 || i==dimX-1) p[7]=(short)0;
					        else p[7]=(int)ip2.getPixelValue(i+1,j+1);
					        
					        int pixcoul=0;

					        for(int l=0;l<8;l++) nontab[l]=0;

					        int cpt =0;
					        
					        // look for different colors
					        for(int l=0;l<8;l++)
					        {
					        	if (p[l]!=0 && appart(p[l],nontab,8)==1)
					        	{
					        		nontab[cpt]=p[l];
					        		cpt++;
					        	}
					        }		

					        for(int mm=0;mm<cpt;mm++)
					        {
					              p1=0;
					              p2=0;
					              p3=0;
					              p4=0;
					              p5=0;
					              p6=0;
					              p7=0;
					              p8=0;

					              if (p[0]!=0 && p[0] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					                  p1=1;
					              }
					                  
					              if (p[1]!=0 && p[1] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					            	  p2=4;
					               }
					               
					              if (p[2]!=0 && p[2] == nontab[mm]) 
					              {
					                  pixcoul=nontab[mm];
					                  p3=2;
					              }
					              
					              if (p[3]!=0 && p[3] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					                  p4=8;
					              }
					              
					              if (p[4]!=0 && p[4] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					                  p5=16;
					              }
					                 
					              if (p[5]!=0 && p[5] == nontab[mm]) 
					              {
					                  pixcoul=nontab[mm];
					                  p6=64;
					              }
					                   
					              if (p[6]!=0 && p[6] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					            	  p7=32;
					              }
					                   
					              if (p[7]!=0 && p[7] == nontab[mm]) 
					              {
					                  pixcoul=nontab[mm];
					                  p8=128;
					              }
					              
					              ptot=(p1+p2+p3+p4+p5+p6+p7+p8);
					              
					              if (pixcoul!=0)
					              {
					            	  //surf[k][(int)(pixcoul-2)]+=tri[(int)(ptot)];	
					            	  //vol[k][(int)(pixcoul-2)]+=tri2[(int)(ptot)];
					            	  surf[k][(int)(pixcoul-1)]+=tri[(int)(ptot)];	
					            	  vol[k][(int)(pixcoul-1)]+=tri2[(int)(ptot)];
					              } 
					        } 
						}
				}			
			}
		}
		};
	}
	startAndJoin(threads);
	for(int i=0; i<dimZ; i++)
	{
		for(int j=0; j<nb; j++)
		{
			v[j]+=vol[i][j];
			s[j]+=surf[i][j];
		}
	}
}

void calculmarchsurfstacknb(ImageStack stack, int nb,double s2[])
{
	double[] cas = new double [22];	
	double[] tri = new double [256];	
	initcas_s2(cas);
	initincrement(tri,cas);
	calculsurfacemarch3stack(stack,nb,tri,s2);
}

void calculsurfacemarch3stack(final ImageStack stack1, int nb, final double[] tri,double[] s2)
{
	final AtomicInteger ai = new AtomicInteger(0);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final double[][] surf  = new double[dimZ][nb];
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ-1; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip1,ip2;
				ip1 = stack1.getProcessor(k+1);
				ip2 = stack1.getProcessor(k+2);
				for(int j=0;j<dimY-1;j++)
				{
						for(int i=0;i<dimX-1;i++)
						{
							int [] p = new int[8];
							int p1,p2,p3,p4,p5,p6,p7,p8,ptot;
					        int [] nontab = new int[8];
					        
							ptot=(short)0;
					        p[0]=(int)ip1.getPixelValue(i,j);
					        p[1]=(int)ip1.getPixelValue(i,j+1);
					        p[2]=(int)ip2.getPixelValue(i,j);
					        p[3]=(int)ip2.getPixelValue(i,j+1);
					        p[4]=(int)ip1.getPixelValue(i+1,j);
					        p[5]=(int)ip1.getPixelValue(i+1,j+1);
					        p[6]=(int)ip2.getPixelValue(i+1,j);
					        p[7]=(int)ip2.getPixelValue(i+1,j+1);
					        
					        int pixcoul=0;

					        for(int l=0;l<8;l++) nontab[l]=0;

					        int cpt =0;
					        
					        // look for different colors
					        for(int l=0;l<8;l++)
					        {
					        	if (p[l]!=0 && appart(p[l],nontab,8)==1)
					        	{
					        		nontab[cpt]=p[l];
					        		cpt++;
					        	}
					        }		

					        for(int mm=0;mm<cpt;mm++)
					        {
					              p1=0;
					              p2=0;
					              p3=0;
					              p4=0;
					              p5=0;
					              p6=0;
					              p7=0;
					              p8=0;

					              if (p[0]!=0 && p[0] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					                  p1=1;
					              }
					                  
					              if (p[1]!=0 && p[1] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					            	  p2=4;
					               }
					               
					              if (p[2]!=0 && p[2] == nontab[mm]) 
					              {
					                  pixcoul=nontab[mm];
					                  p3=2;
					              }
					              
					              if (p[3]!=0 && p[3] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					                  p4=8;
					              }
					              
					              if (p[4]!=0 && p[4] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					                  p5=16;
					              }
					                 
					              if (p[5]!=0 && p[5] == nontab[mm]) 
					              {
					                  pixcoul=nontab[mm];
					                  p6=64;
					              }
					                   
					              if (p[6]!=0 && p[6] == nontab[mm]) 
					              {
					            	  pixcoul=nontab[mm];
					            	  p7=32;
					              }
					                   
					              if (p[7]!=0 && p[7] == nontab[mm]) 
					              {
					                  pixcoul=nontab[mm];
					                  p8=128;
					              }
					              
					              ptot=(p1+p2+p3+p4+p5+p6+p7+p8);
					              
					              if (pixcoul!=0)
					              {
					            	  surf[k][(int)(pixcoul-1)]+=tri[(int)(ptot)];
						              //surf[k][(int)(pixcoul-2)]+=tri[(int)(ptot)];
					              } 
					        } 
						}
				}			
			}
		}
		};
	}
	startAndJoin(threads);
	for(int i=0; i<dimZ; i++)
	{
		for(int j=0; j<nb; j++)
		{
			s2[j]+=surf[i][j];
		}
	}
}

void calculvolumestack(final ImageStack stack1, int nb, int v[])
{
	final AtomicInteger ai = new AtomicInteger(1);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final int[][] vol  = new int[dimZ][nb];

	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi <= dimZ; superi = ai.getAndIncrement()) {
				int current = superi;
				ImageProcessor ip = stack1.getProcessor(current);
				for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							int val =(int) (ip.getPixelValue(i,j));
							//if (val!=0) vol[current-1][val-2]++;
							if (val!=0) vol[current-1][val-1]++;
						}
					}
			}
		}
		};
	}
	startAndJoin(threads);
	for(int i=0; i<dimZ; i++)
	{
		for(int j=0; j<nb; j++)
		{
			v[j]+=vol[i][j];
		}
	}
}

void calculcgstack(final ImageStack stack1, int nb, final int[] v, final double[] xg, final double[] yg, final double[] zg)
{
	final AtomicInteger ai = new AtomicInteger(0);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final int[][] vol  = new int[dimZ][nb];
	final int[][] tmpxg  = new int[dimZ][nb];
	final int[][] tmpyg  = new int[dimZ][nb];
	final int[][] tmpzg  = new int[dimZ][nb];

	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip = stack1.getProcessor(k+1);
				for(int j=0;j<dimY;j++)
					{
						for(int i=0;i<dimX;i++)
						{
							int pix =(int) (ip.getPixelValue(i,j));
							if(pix!=0){
//						          tmpxg[k][pix-2] += j;
//						          tmpyg[k][pix-2] += i;
//						          tmpzg[k][pix-2] += k;
//						          vol[k][pix-2]++;
						          tmpxg[k][pix-1] += i;
						          tmpyg[k][pix-1] += j;
						          tmpzg[k][pix-1] += k;
						          vol[k][pix-1]++;
						    }
						}
					}
			}
		}
		};
	}
	startAndJoin(threads);
	for(int i=0; i<dimZ; i++)
	{
		for(int j=0; j<nb; j++)
		{
			v[j]+=vol[i][j];
			xg[j]+=tmpxg[i][j];
			yg[j]+=tmpyg[i][j];
			zg[j]+=tmpzg[i][j];
		}
	}

	for(int i=0;i<nb;i++)
	{
		xg[i]=(1.0*xg[i]/v[i]);
		yg[i]=(1.0*yg[i]/v[i]);
		zg[i]=(1.0*zg[i]/v[i]);
	}
}

void calculinertiestack(final ImageStack stack1, int nb, final double[] xg, final double[] yg, final double[] zg, double[][][] I)
{
	final AtomicInteger ai = new AtomicInteger(0);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final double[][][][] inert  = new double[dimZ][nb][3][3];

	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip = stack1.getProcessor(k+1);
				for(int j=0;j<dimY;j++)
					{
						for(int i=0;i<dimX;i++)
						{
							int pix =(int) (ip.getPixelValue(i,j));
							if(pix!=0){
//						         inert[k][pix-2][0][0]+=(i-yg[pix-2])*(i-yg[pix-2])+(k-zg[pix-2])*(k-zg[pix-2])+1.0/6.0;
//						         inert[k][pix-2][0][1]-=(j-xg[pix-2])*(i-yg[pix-2]);
//						         inert[k][pix-2][0][2]-=(j-xg[pix-2])*(k-zg[pix-2]);
//						         inert[k][pix-2][1][1]+=(j-xg[pix-2])*(j-xg[pix-2])+(k-zg[pix-2])*(k-zg[pix-2])+1.0/6.0;
//						         inert[k][pix-2][1][2]-=(i-yg[pix-2])*(k-zg[pix-2]);
//						         inert[k][pix-2][2][2]+=(j-xg[pix-2])*(j-xg[pix-2])+(i-yg[pix-2])*(i-yg[pix-2])+1.0/6.0;
						         inert[k][pix-1][0][0]+=(j-yg[pix-1])*(j-yg[pix-1])+(k-zg[pix-1])*(k-zg[pix-1])+1.0/6.0;
						         inert[k][pix-1][0][1]-=(i-xg[pix-1])*(j-yg[pix-1]);
						         inert[k][pix-1][0][2]-=(i-xg[pix-1])*(k-zg[pix-1]);
						         inert[k][pix-1][1][1]+=(i-xg[pix-1])*(i-xg[pix-1])+(k-zg[pix-1])*(k-zg[pix-1])+1.0/6.0;
						         inert[k][pix-1][1][2]-=(j-yg[pix-1])*(k-zg[pix-1]);
						         inert[k][pix-1][2][2]+=(i-xg[pix-1])*(i-xg[pix-1])+(j-yg[pix-1])*(j-yg[pix-1])+1.0/6.0;
						    }
						}
					}
			}
		}
		};
	}
	startAndJoin(threads);
	for(int i=0; i<dimZ; i++)
	{
		for(int j=0; j<nb; j++)
		{
			for(int l=0;l<3;l++)
			{
				for(int m=0; m<=l; m++)
				{
					I[l][m][j]+=inert[i][j][l][m];
				}
			}
		}
		inert[i]=null;	
	}
	for(int j=0; j<nb; j++)
	{	
		I[1][0][j]=I[0][1][j];
		I[2][0][j]=I[0][2][j];
		I[2][1][j]=I[1][2][j];
	}
}

void boitestack(final ImageStack stack1, final int nb, final double[] xg, final double[] yg, final double[] zg, final double[][][] dir, double[][] lmin, double[][] lmax)
{
	final AtomicInteger ai = new AtomicInteger(0);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final double[][][] lmint = new double[dimZ][nb][3];
	final double[][][] lmaxt = new double[dimZ][nb][3];

	for(int k=0; k<nb; k++)
	{
	  lmin[k][0]= 100000;
	  lmin[k][1]= 100000;
	  lmin[k][2]= 100000;
	  lmax[k][0]= -100000;
	  lmax[k][1]= -100000;
	  lmax[k][2]= -100000;
	}
	
	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip = stack1.getProcessor(k+1);
				for(int l=0; l<nb; l++)
				{
				  lmint[k][l][0]= 100000;
				  lmint[k][l][1]= 100000;
				  lmint[k][l][2]= 100000;
				  lmaxt[k][l][0]= -100000;
				  lmaxt[k][l][1]= -100000;
				  lmaxt[k][l][2]= -100000;
				}
				for(int j=0;j<dimY;j++)
					{
						for(int i=0;i<dimX;i++)
						{
							int pix =(int) (ip.getPixelValue(i,j));
							if(pix!=0){
//								double v1 = (i-xg[pix-2])*dir[0][0][pix-2]+(j-yg[pix-2])*dir[1][0][pix-2]+(k-zg[pix-2])*dir[2][0][pix-2];
//							    double v2 = (i-xg[pix-2])*dir[0][1][pix-2]+(j-yg[pix-2])*dir[1][1][pix-2]+(k-zg[pix-2])*dir[2][1][pix-2];
//							    double v3 = (i-xg[pix-2])*dir[0][2][pix-2]+(j-yg[pix-2])*dir[1][2][pix-2]+(k-zg[pix-2])*dir[2][2][pix-2];
//							    if (v1<lmint[k][pix-2][0]) lmint[k][pix-2][0]=v1;
//							    if (v1>lmaxt[k][pix-2][0]) lmaxt[k][pix-2][0]=v1;
//							    if (v2<lmint[k][pix-2][1]) lmint[k][pix-2][1]=v2;
//							    if (v2>lmaxt[k][pix-2][1]) lmaxt[k][pix-2][1]=v2;
//							    if (v3<lmint[k][pix-2][2]) lmint[k][pix-2][2]=v3;
//							    if (v3>lmaxt[k][pix-2][2]) lmaxt[k][pix-2][2]=v3;
							    double v1 = (i-xg[pix-1])*dir[0][0][pix-1]+(j-yg[pix-1])*dir[1][0][pix-1]+(k-zg[pix-1])*dir[2][0][pix-1];
							    double v2 = (i-xg[pix-1])*dir[0][1][pix-1]+(j-yg[pix-1])*dir[1][1][pix-1]+(k-zg[pix-1])*dir[2][1][pix-1];
							    double v3 = (i-xg[pix-1])*dir[0][2][pix-1]+(j-yg[pix-1])*dir[1][2][pix-1]+(k-zg[pix-1])*dir[2][2][pix-1];
							    if (v1<lmint[k][pix-1][0]) lmint[k][pix-1][0]=v1;
							    if (v1>lmaxt[k][pix-1][0]) lmaxt[k][pix-1][0]=v1;
							    if (v2<lmint[k][pix-1][1]) lmint[k][pix-1][1]=v2;
							    if (v2>lmaxt[k][pix-1][1]) lmaxt[k][pix-1][1]=v2;
							    if (v3<lmint[k][pix-1][2]) lmint[k][pix-1][2]=v3;
							    if (v3>lmaxt[k][pix-1][2]) lmaxt[k][pix-1][2]=v3;
						    }
						}
					}
			}
		}
		};
	}
	startAndJoin(threads);
	for(int i=0; i<dimZ; i++)
	{
		for(int j=0; j<nb; j++)
		{
			for(int l=0;l<3;l++)
			{
				if(lmint[i][j][l]<lmin[j][l]) lmin[j][l]=lmint[i][j][l];
				if(lmaxt[i][j][l]>lmax[j][l]) lmax[j][l]=lmaxt[i][j][l];
			}
		}
		lmint[i]=null;
		lmaxt[i]=null;
	}
}

void borderstack(final ImageStack stack1, int nb, byte []b)
{
	final AtomicInteger ai = new AtomicInteger(0);
	final int dimX = stack1.getWidth();
	final int dimY = stack1.getHeight();
	final int dimZ = stack1.getSize();
	final byte[][] bord  = new byte[dimZ][nb];

	for (int ithread = 0; ithread < threads.length; ithread++) {

	    // Concurrently run in as many threads as CPUs

		threads[ithread] = new Thread() {
	
		public void run() 
		{	
			for (int superi = ai.getAndIncrement(); superi < dimZ; superi = ai.getAndIncrement()) {
				int k = superi;
				ImageProcessor ip = stack1.getProcessor(k+1);
				for(int  j=0;j<dimY;j++)
					{
						for(int  i=0;i<dimX;i++)
						{
							int val =(int) (ip.getPixelValue(i,j));
							if ((val != 0) && ( i==0 || j==0 || k==0 || i==dimX-1 || j==dimY-1 || k==dimZ-1))
							    //bord[k][val-2]=1;
								bord[k][val-1]=1;
						}
					}
			}
		}
		};
	}
	startAndJoin(threads);
	for(int i=0; i<dimZ; i++)
	{
		for(int j=0; j<nb; j++)
		{
			b[j]*=(1-bord[i][j]);
		}
	}
	for(int j=0; j<nb; j++)
	{
		b[j]=(byte)(1-b[j]);
	}
}

void sauvegarde(int v1[],double v2[],double s[],double s2[],double xg[],double yg[],double zg[],double [][]J, double [][][] dir, int n,byte[]bord,double[][] lmin,double[][] lmax,String directory, String name)
{
        double sp;

        PrintWriter pw = null;
        try {
            FileOutputStream fos = new FileOutputStream(directory+name);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            pw = new PrintWriter(bos);
        }
        catch (IOException e) {
            return;
        }


       pw.println("nb xg yg zg volpix volmarch surfacemarch surfacemarchnb sphericity I1 I2 I3 vI1x vI1y vI1z  vI2x vI2y vI2z  vI3x vI3y vI3z a b c Fab Fac Fbc xmin xmax ymin ymax zmin zmax dx dy dz border");

        for (int i=0;i<n;i++) {
                         pw.print(i+1);
                         pw.print(" ");
                         pw.print(xg[i]);
                         pw.print(" ");
                         pw.print(yg[i]);
                         pw.print(" ");
                         pw.print(zg[i]);
                         pw.print(" ");
                         pw.print(v1[i]);
                         pw.print(" ");
                         pw.print(v2[i]);
                         pw.print(" ");
                         pw.print(s[i]);
                         pw.print(" ");
                         pw.print(s2[i]);
	     sp = 6*v2[i]*Math.sqrt(3.14159265/(s2[i]*s2[i]*s2[i]));
                         pw.print(" ");
                         pw.print(sp);
                         pw.print(" ");
                         pw.print(J[0][i]);
                         pw.print(" ");
                         pw.print(J[1][i]);
                         pw.print(" ");
                         pw.print(J[2][i]);
                         pw.print(" ");
                         pw.print(dir[0][0][i]);
                         pw.print(" ");
                         pw.print(dir[1][0][i]);
                         pw.print(" ");
                         pw.print(dir[2][0][i]);
                         pw.print(" ");
                         pw.print(dir[0][1][i]);
                         pw.print(" ");
                         pw.print(dir[1][1][i]);
                         pw.print(" ");
                         pw.print(dir[2][1][i]);
                         pw.print(" ");
                         pw.print(dir[0][2][i]);
                         pw.print(" ");
                         pw.print(dir[1][2][i]);
                         pw.print(" ");
                         pw.print(dir[2][2][i]);
                         pw.print(" ");
                         double ma =(J[0][i]+J[1][i]-J[2][i]);
                         double mb = (J[0][i]-J[1][i]+J[2][i]);
                         double mc = (-J[0][i]+J[1][i]+J[2][i]);
                         double b1 = (3*mb*mb/(16*Math.sqrt(ma*mc)));
                 	       b1 = Math.pow(b1,0.2);
                          double a = b1*Math.sqrt(ma/mb);
	       double b = b1;
	       double c=b*Math.sqrt(mc/mb);
	       double Fab =Math.sqrt((J[0][i]+J[1][i]-J[2][i])/(J[0][i]-J[1][i]+J[2][i]));
	       double Fac = Math.sqrt((J[0][i]+J[1][i]-J[2][i])/(-J[0][i]+J[1][i]+J[2][i]));
	       double Fbc = Math.sqrt((J[0][i]-J[1][i]+J[2][i])/(-J[0][i]+J[1][i]+J[2][i]));	     
                         pw.print(a);
                         pw.print(" ");
                         pw.print(b);
                         pw.print(" ");
                         pw.print(c);
                         pw.print(" ");
                         pw.print(Fab);
                         pw.print(" ");
                         pw.print(Fac);
                         pw.print(" ");
                         pw.print(Fbc);
                         pw.print(" ");
                         pw.print(lmin[i][0]-0.5);
                         pw.print(" ");
                         pw.print(lmax[i][0]+0.5);
                         pw.print(" ");
                         pw.print(lmin[i][1]-0.5);
                         pw.print(" ");
                         pw.print(lmax[i][1]+0.5);
                         pw.print(" ");
                         pw.print(lmin[i][2]-0.5);
                         pw.print(" ");
                         pw.print(lmax[i][2]+0.5);
                         pw.print(" ");
	      double dx=lmax[i][0]-lmin[i][0]+1;	
	      double dy=lmax[i][1]-lmin[i][1]+1;	
	      double dz=lmax[i][2]-lmin[i][2]+1;	
	      double a1 = dx;
	      if (dy<a1) a1=dy;
	      if (dz<a1) a1=dz;

	      double a3 = dx;
	      if (dy>a3) a3=dy;
	      if (dz>a3) a3=dz;
	     double a2=dx;

	      if (dx!=a1 && dx !=a3) a2=dx; 
	      if (dy!=a1 && dy !=a3) a2=dy; 
	      if (dz!=a1 && dz !=a3) a2=dz;
	      
          pw.print(" ");
          pw.print(a1);
          pw.print(" ");
          pw.print(a2);
          pw.print(" ");
          pw.print(a3);
          pw.print(" ");
          pw.println(bord[i]);

		}
       pw.close();

}

int appart(int v,int []tab, int n)
{
int val;
val =1;
              for(int i=0;i<n;i++) 
	{
	  if (v==tab[i]) val =val*0;
	  else val =val*1;
	}
	 return val ;
}


void ROTATE(double [][]a, int i, int j, int k, int l, double tau,double s)
{
double g,h;
 
g=a[i][j];
h=a[k][l];
a[i][j]=g-s*(h+g*tau);
a[k][l]=h+s*(g-h*tau);
}

void jacobi(double [][]a ,int n,double [] d,double [][] v)
    /* 	a matrice de depart,
	n taille du systeme,
	d valeurs propres
	v matrice de passage : vecteur propre normalises  */

{ 
	int j,iq,ip,i; 
	double tresh,theta,tau,t,sm,s,h,g,c;
	double []b=new double[3];
	double []z=new double[3];

     
	for (ip=0;ip<n;ip++)
	{
		for (iq=0;iq<n;iq++)
		{
			v[ip][iq]=0.0;
		}
		v[ip][ip]=1.0;
	}
	for (ip=0;ip<n;ip++)
	{
		b[ip]=d[ip]=a[ip][ip];
		z[ip]=0.0;
	}
	//int nrot=0;
	for (i=1;i<=50;i++)
	{
		sm=0.0;
		for (ip=0;ip<n-1;ip++) 
		{
			for (iq=ip+1;iq<n;iq++)
				sm += Math.abs(a[ip][iq]);
		}
		if (sm == 0.0) 
		{
			return;
		}
		if (i < 4)
			tresh=0.2*sm/(n*n);
		else
			tresh=0.0;
		for (ip=0;ip<n-1;ip++) 
		{
			for (iq=ip+1;iq<n;iq++) 
			{
				g=100.0*Math.abs(a[ip][iq]);
				if (i > 4 && Math.abs(d[ip])+g == Math.abs(d[ip])
					&& Math.abs(d[iq])+g == Math.abs(d[iq]))
					a[ip][iq]=0.0;
				else if (Math.abs(a[ip][iq]) > tresh)
				{
					h=d[iq]-d[ip];
					if (Math.abs(h)+g == Math.abs(h))
						t=(a[ip][iq])/h;
					else 
					{
						theta=0.5*h/(a[ip][iq]);
						t=1.0/(Math.abs(theta)+Math.sqrt(1.0+theta*theta));
						if (theta < 0.0) t = -t;
					}
					c=1.0/Math.sqrt(1+t*t);
					s=t*c;
					tau=s/(1.0+c);
					h=t*a[ip][iq];
					z[ip] -= h;
					z[iq] += h;
					d[ip] -= h;
					d[iq] += h;
					a[ip][iq]=0.0;
					for (j=0;j<=ip-1;j++) 
					{
						ROTATE(a,j,ip,j,iq,tau,s);
					}
					for (j=ip+1;j<=iq-1;j++)
					{
						ROTATE(a,ip,j,j,iq,tau,s);
					}
					for (j=iq+1;j<n;j++) 
					{
						ROTATE(a,ip,j,iq,j,tau,s);
					}
					for (j=0;j<n;j++) 
					{
						ROTATE(v,j,ip,j,iq,tau,s);
					}
					//++nrot;
				}
			}
		}
		for (ip=0;ip<n;ip++) 
		{
			b[ip] += z[ip];
			d[ip]=b[ip];
			z[ip]=0.0;
		}
	}
}


void eigsrt(double []d,double [][] v,int n)
{
	int k,j,i;
	double p;
    
	for (i=0;i<(n-1);i++) 
		{
		p=d[k=i];
		for (j=i+1;j<n;j++)
			if (d[j] >= p) p=d[k=j];
		if (k != i) 
			{
			d[k]=d[i];
			d[i]=p;
			for (j=0;j<n;j++) 
				{
				p=v[j][i];
				v[j][i]=v[j][k];
				v[j][k]=p;
				}
			}
		}
}


void transfert1(double[][][] I,int i,double [][] A)
{
   int k,j;
   for(k=0;k<3;k++)
     for(j=0;j<3;j++)
        {
	  A[k][j]=I[k][j][i];
        }
}	

void transfert2(double [][] sol ,int i,double [][][]dir )
{
   int k,j;
   for(k=0;k<3;k++)
     for(j=0;j<3;j++)
             dir[k][j][i]=sol[k][j];
}
	

void inertie(int n,double[][][] I, double[][] J, double dir [][][])
{
   double[][] A = new double [3][3];
   double[][] SOL = new double [3][3];
   double[]B = new double [3];

   for(int i=0;i<n;i++)
   {
       //IJ.showStatus("Inertia calculation ...: "+i+"/"+n);
       transfert1(I,i,A);
       jacobi(A,3,B,SOL);
       eigsrt(B,SOL,3);
       J[0][i]=B[2];
       J[1][i]=B[1];
       J[2][i]=B[0];
       transfert2(SOL,i,dir);
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

