import ij.*;
import ij.measure.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.io.*;
import java.io.*;

public class Parameters_3D_old implements PlugInFilter {

	protected ImageStack stack;
	InputStream pin;
	int nb;
	int dimx,dimy,dimz;
	static int valPixel = 0;
	
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_16+STACK_REQUIRED;
	}

	int stacktovol (ImageStack stack, short[][][] vol,int dim1,int dim2, int dim3)
	{
                      int i,j,k,m;
                      int max = 0;
                                     
		for (k=1;k<=dim3;k++)
		 {
			short[] pixels = (short[]) stack.getPixels(k);
			//IJ.showStatus("processing volume ...: "+k+"/"+dim3);
			for( i=0;i<dim1;i++)
			for( j=0;j<dim2;j++)			
			{
                                                        m=i*(dim2)+j;
                                                        int  pix = (pixels[m] & 0xffff);
			if (pix>max) max =pix;
			vol[i][j][k-1]=(short)(pix);
			}
		}
	return (max);

	}

void calculvolume(short [][][]vol,int dim1,int dim2,int dim3,int v[])
{
 int i,j,k;
int av;

 for(k=0;k<dim3;k++)
 {
	av = k*100/dim3;
	IJ.showStatus("volume in pixel calculation ...: "+av+"%");
 
	for(j=0;j<dim2;j++)			
		for(i=0;i<dim1;i++)
		{
  		  int pix =  vol[i][j][k];
   		 if (pix != 0)
    		v[pix-2]=v[pix-2]+1;
  		}  
}
}


int appart(short v,short []tab, int n)
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

void calculsurfacemarch2(short [][][]m,int dim1,int dim2,int dim3,double []tri,double []s, int num)

{
   int i,j,k,l,mm,cpt;
   short pixcoul;
   short p1,p2,p3,p4,p5,p6,p7,p8,ptot;
   short [] p = new short[8];
   short [] nontab = new short[8];      
int av;

   for(k=-1;k<dim3;k=k+1)
      {
	av = k*100/dim3;
	if (num==0) IJ.showStatus("surface calculation in progress (marching cubes) ...: "+av+"%");
	else IJ.showStatus("volume calculation in progress (marching cubes) ...: "+av+"%");


      for(j=-1;j<dim2;j=j+1)
         {  
	    for(i=-1;i<dim1;i=i+1)
	    {
              ptot=(short)0;
              if (j<0 || i<0 || k<0) p[0]=(short)0;
              else p[0]=m[i][j][k];
              if (j==dim2-1 || i<0 || k<0) p[1]=(short)0;
              else p[1]=m[i][j+1][k];
              if(k==dim3-1 || i<0 || j<0) p[2]=(short)0;
              else p[2]=m[i][j][k+1];
              if(k==dim3-1 || j==dim2-1 || i<0) p[3]=(short)0;
              else  p[3]=m[i][j+1][k+1];
              if(i==dim1-1 || k<0 || j<0) p[4]=(short)0;
              else  p[4]=m[i+1][j][k];
              if(i==dim1-1 || j==dim2-1 || k<0) p[5]=(short)0;
              else p[5]=m[i+1][j+1][k];
              if(k==dim3-1 || i==dim1-1 || j<0) p[6]=(short)0;
              else p[6]=m[i+1][j][k+1];
              if(k==dim3-1 || j==dim2-1 || i==dim1-1) p[7]=(short)0;
              else p[7]=m[i+1][j+1][k+1];
              pixcoul=0;

              for( l=0;l<8;l++) nontab[l]=0;

              cpt =0;
              // on rep�re les couleurs diff�rentes
              for(l=0;l<8;l++)
	{
	if (p[l]!=0 && appart(p[l],nontab,8)==1)
	{
	 nontab[cpt]=p[l];
	 cpt++;
	}
	}		

              for(mm=0;mm<cpt;mm++)
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
                         p1=(short)1;
                     }
                   if (p[1]!=0 && p[1] == nontab[mm]) 
                     {
                         pixcoul=nontab[mm];
                         p2=(short)4;
                     }
                   if (p[2]!=0 && p[2] == nontab[mm]) 
                     {
                         pixcoul=nontab[mm];
                         p3=(short)2;
                     }
                   if (p[3]!=0 && p[3] == nontab[mm]) 
                     {
                         pixcoul=nontab[mm];
                         p4=(short)8;
                     }
                   if (p[4]!=0 && p[4] == nontab[mm]) 
                     {
                         pixcoul=nontab[mm];
                         p5=(short)16;
                     }
                   if (p[5]!=0 && p[5] == nontab[mm]) 
                     {
                         pixcoul=nontab[mm];
                         p6=(short)64;
                     }
                   if (p[6]!=0 && p[6] == nontab[mm]) 
                     {
                         pixcoul=nontab[mm];
                         p7=(short)32;
                     }
                   if (p[7]!=0 && p[7] == nontab[mm]) 
                     {
                         pixcoul=nontab[mm];
                         p8=(short)128;
                     }
                   ptot=(short)(p1+p2+p3+p4+p5+p6+p7+p8);
	if (pixcoul!=0) 
	{
		s[(int)(pixcoul-2)]+=tri[(int)(ptot)];	
	} 
	}
	      
            }
         } 
      }
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

void calculmarchsurf(short [][][]vol,int dim1,int dim2,int dim3,double s[])
{
 int i,j,k;
 
 double[] cas = new double [22];	
 double[] tri = new double [256];	

 initcas_s2(cas);
 initincrement(tri,cas);
 calculsurfacemarch2(vol,dimx,dimy,dimz,tri,s,0);

}
void calculmarchvol(short [][][]vol,int dim1,int dim2,int dim3,double v[])
{
 int i,j,k;
 
 double[] cas = new double [22];	
 double[] tri = new double [256];	

 initcas_v2_luc(cas);
 initincrement(tri,cas);
 calculsurfacemarch2(vol,dimx,dimy,dimz,tri,v,1);

}

void calculcg(short [][][]vol,int dim1,int dim2,int dim3,double xg[],double yg[],double zg[],int nb)
{
int i,j,k;
double[] v = new double[nb];
int pix;
int av;

for(k=0; k<dim3; k++)	
{
av = k*100/dim3;
IJ.showStatus("gravity center calculation ...: "+av+"%");

for(j=0; j<dim2; j++)
 for(i=0; i<dim1; i++)
 {
    pix = vol[i][j][k];
    if (pix != 0)
    {
          xg[pix-2] = xg[pix-2] + j;
          yg[pix-2] = yg[pix-2] + i;
          zg[pix-2] = zg[pix-2] + k;
          v[pix-2]=v[pix-2]+1;
    }
}
}
for(i=0;i<nb;i++)
{
      xg[i]=(1.0*xg[i]/v[i]);
      yg[i]=(1.0*yg[i]/v[i]);
      zg[i]=(1.0*zg[i]/v[i]);
}
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
	int j,iq,ip,i,nrot; 
	double tresh,theta,tau,t,sm,s,h,g,c;
	double []b=new double[3];
	double []z=new double[3];

     
	for (ip=0;ip<n;ip++)
		{
		for (iq=0;iq<n;iq++) v[ip][iq]=0.0;
		v[ip][ip]=1.0;
		}
	for (ip=0;ip<n;ip++)
		{
		b[ip]=d[ip]=a[ip][ip];
		z[ip]=0.0;
		}
	nrot =0;
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
					++(nrot);
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



void calculinertie(short [][][] m,int dim1,int dim2,int dim3,double [] xg,double [] yg,double [] zg,double [][][] I)
{
   int i,j,k;
   int pix;
   int av;
  
   for(k=0;k<dim3;k++)
      {
      av = k*100/dim3;
      IJ.showStatus("Inertia calculation ...: "+av+"%");
      for(j=0;j<dim2;j++)
         {  
	    for(i=0;i<dim1;i++)
	      {
                         pix=m[i][j][k];
	       if(pix!=0) 
	       {
     		 I[0][0][pix-2]=I[0][0][pix-2]+(i-yg[pix-2])*(i-yg[pix-2])+(k-zg[pix-2])*(k-zg[pix-2])+1.0/6.0;
    		 I[0][1][pix-2]=I[0][1][pix-2]-(j-xg[pix-2])*(i-yg[pix-2]);
    		 I[0][2][pix-2]=I[0][2][pix-2]-(j-xg[pix-2])*(k-zg[pix-2]);
    		 I[1][1][pix-2]=I[1][1][pix-2]+(j-xg[pix-2])*(j-xg[pix-2])+(k-zg[pix-2])*(k-zg[pix-2])+1.0/6.0;
	    	 I[1][2][pix-2]=I[1][2][pix-2]-(i-yg[pix-2])*(k-zg[pix-2]);
	    	 I[2][2][pix-2]=I[2][2][pix-2]+(j-xg[pix-2])*(j-xg[pix-2])+(i-yg[pix-2])*(i-yg[pix-2])+1.0/6.0;
	    	 I[1][0][pix-2]=I[0][1][pix-2];
	    	 I[2][0][pix-2]=I[0][2][pix-2];
	     	 I[2][1][pix-2]=I[1][2][pix-2];
           }
	       else{}	      
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
   int i,nrot; 
   double[][] A = new double [3][3];
   double[][] SOL = new double [3][3];
   double[]B = new double [3];

   for(i=0;i<n;i++)
   {
       //IJ.showStatus("Inertia calculation ...: "+i+"/"+n);
       transfert1(I,i,A);
       jacobi(A,3,B,SOL);
       eigsrt(B,SOL,3);
       J[0][i]=B[0];
       J[1][i]=B[1];
       J[2][i]=B[2];
       transfert2(SOL,i,dir);
   }
}

void border(short [][][]m, int dim1, int dim2, int dim3, byte []b)
{
 int i,j,k;
int pix;
int av;

 for(k=0; k<dim3; k++)
 {
 av = k*100/dim3;
 IJ.showStatus("border finding ...: "+av+"%");
 for(j=0; j<dim2; j++)
 for(i=0; i<dim1; i++)
 {
     pix=m[i][j][k];
    if ((pix != 0) && ( i==0 || j==0 || k==0 || i==dim1-1 || j==dim2-1 || k==dim3-1))
    b[pix-2]=1;
  }
  }
}


void boite(short [][][]m, int dim1, int dim2, int dim3,int n,double xg[],double yg[],double zg[],double [][][] dir,double[][] lmin,double[][] lmax)
{
 int i,j,k;
int pix;
double v1,v2,v3;
int av;

 for(k=0; k<n; k++)
 {
  lmin[k][0]= 100000;
  lmin[k][1]= 100000;
  lmin[k][2]= 100000;
  lmax[k][0]= -100000;
  lmax[k][1]= -100000;
  lmax[k][2]= -100000;
}

 for(k=0; k<dim3; k++)
 {
av = k*100/dim3;
 IJ.showStatus("bouding box finding ...: "+av+"%");
 for(j=0; j<dim2; j++)
 for(i=0; i<dim1; i++)
 {
     pix=m[i][j][k];
    if(pix!=0)
    {
     v1 = (i-xg[pix-2])*dir[0][0][pix-2]+(j-yg[pix-2])*dir[1][0][pix-2]+(k-zg[pix-2])*dir[2][0][pix-2];
     v2 = (i-xg[pix-2])*dir[0][1][pix-2]+(j-yg[pix-2])*dir[1][1][pix-2]+(k-zg[pix-2])*dir[2][1][pix-2];
     v3 = (i-xg[pix-2])*dir[0][2][pix-2]+(j-yg[pix-2])*dir[1][2][pix-2]+(k-zg[pix-2])*dir[2][2][pix-2];
    if (v1<lmin[pix-2][0]) lmin[pix-2][0]=v1;
    if (v1>lmax[pix-2][0]) lmax[pix-2][0]=v1;
    if (v2<lmin[pix-2][1]) lmin[pix-2][1]=v2;
    if (v2>lmax[pix-2][1]) lmax[pix-2][1]=v2;
    if (v3<lmin[pix-2][2]) lmin[pix-2][2]=v3;
    if (v3>lmax[pix-2][2]) lmax[pix-2][2]=v3;
   }
  }
  }
}


void sauvegarde(int v1[],double v2[],double s[],double xg[],double yg[],double zg[],double [][]J, double [][][] dir, int n,byte[]bord,double[][] lmin,double[][] lmax)
{
        double sp;

        SaveDialog sd = new SaveDialog("Save Coordinates as Text...", "res", ".dat");
        String name = sd.getFileName();
        if (name == null)
            return;
        String directory = sd.getDirectory();
        PrintWriter pw = null;
        try {
            FileOutputStream fos = new FileOutputStream(directory+name);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            pw = new PrintWriter(bos);
        }
        catch (IOException e) {
            IJ.write("pb" + e);
            return;
        }


       pw.println("nb xg yg zg volpix volmarch surfacemarch sphericity I1 I2 I3 vI1x vI1y vI1z  vI2x vI2y vI2z  vI3x vI3y vI3z a b c Fca Fba Fcb xmin xmax ymin ymax zmin zmax bx by bz border");

        for (int i=0;i<n;i++) {
                         pw.print(i+2);
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
	     sp = 6*v2[i]*Math.sqrt(3.14159265/(s[i]*s[i]*s[i]));
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
	       //IJ.showMessage("ma="+ma);
                         double mb = (J[0][i]-J[1][i]+J[2][i]);
	      double mc = (-J[0][i]+J[1][i]+J[2][i]);
	      double b1 = ((mb*mb)/Math.sqrt(ma*mc));
	      double b= 0;
	      b = 2*Math.pow(b1,0.2); ;
                	      double a = b*Math.sqrt(mc/mb);
	      double c=b*Math.sqrt(ma/mb);
	      double Fca =c/a;
	      double Fba = b/a;
	      double Fcb = c/b;
	      //double Fcb = Math.sqrt(mc/ma);	     
                         pw.print(a);
                         pw.print(" ");
                         pw.print(b);
                         pw.print(" ");
                         pw.print(c);
                         pw.print(" ");
                         pw.print(Fca);
                         pw.print(" ");
                         pw.print(Fba);
                         pw.print(" ");
                         pw.print(Fcb);
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



void sauvegarde2(double xg[],double yg[],double zg[],double [][][] dir, int n)
{
        double sp;

        SaveDialog sd = new SaveDialog("Save Coordinates as Text...", "gnu", ".dat");
        String name = sd.getFileName();
        if (name == null)
            return;
        String directory = sd.getDirectory();
        PrintWriter pw = null;
        try {
            FileOutputStream fos = new FileOutputStream(directory+name);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            pw = new PrintWriter(bos);
        }
        catch (IOException e) {
            IJ.write("pb" + e);
            return;
        }



        for (int i=0;i<n;i++) {
                         pw.print(xg[i]);
                         pw.print(" ");
                         pw.print(yg[i]);
                         pw.print(" ");
                         pw.println(zg[i]);
                         pw.print(xg[i]+100*dir[0][2][i]);
                         pw.print(" ");
                         pw.print(yg[i]+100*dir[1][2][i]);
                         pw.print(" ");
                         pw.println(zg[i]+100*dir[2][2][i]);
                         pw.println(" ");
                         pw.println(" ");


		}
        pw.close();
   

}
	public void run(ImageProcessor ip) {

		dimz =stack.getSize();
		dimy = stack.getWidth();
		dimx = stack.getHeight();


		short[][][] vollab = new short [dimx][dimy][dimz];	
		nb=stacktovol (stack, vollab,dimx, dimy,dimz)-1;
		if(nb<1)
		{
		IJ.showMessage("volume must be labeled");
		}
		else
		{
       		double[] volume_m = new double[nb];
       		int[] volume_p = new int[nb];
		double[] surface = new double[nb];
		double[][][] I = new double[3][3][nb];
		double[][] J = new double[3][nb];
		double[][][] dir = new double[3][3][nb];
		double[] xg = new double[nb];
		double[] yg = new double[nb];
		double[] zg = new double[nb]; 
		byte[] bord = new byte[nb]; 
		double[] a = new double[nb];
		double[] b = new double[nb];
		double[] c = new double[nb];
		double[] Fab = new double[nb];
		double[] Fac = new double[nb];
		double[] Fbc = new double[nb];
		double[] sp = new double[nb];
		double[][] lmin = new double[nb][3];
		double[][] lmax = new double[nb][3];
      		calculmarchsurf(vollab,dimx,dimy,dimz,surface);
 		calculmarchvol(vollab,dimx,dimy,dimz,volume_m);
		calculvolume(vollab,dimx,dimy,dimz,volume_p);
		calculcg(vollab,dimx,dimy,dimz,xg, yg,zg,nb);
               		calculinertie(vollab,dimx,dimy,dimz,xg,yg,zg,I);
                		inertie(nb,I,J,dir);
		boite(vollab, dimx,dimy,dimz,nb,xg,yg,zg,dir,lmin,lmax);
		border(vollab, dimx, dimy, dimz, bord);
		sauvegarde(volume_p,volume_m,surface,xg,yg,zg,J,dir,nb,bord,lmin,lmax);
		//sauvegarde2(xg,yg,zg,dir,nb);
		vollab = null;
		volume_m = null;
		volume_p = null;
		surface = null;
		xg = null;
		yg = null;
		zg = null;
		}
		//IJ.register(Parameters_3D.class);


                  }

}

