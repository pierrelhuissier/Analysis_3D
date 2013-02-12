
import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;



public class median_3D_old implements PlugInFilter {

	protected ImageStack stack,stack2;
	int nb;
	static double valPixel=1;
	
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_8G+STACK_REQUIRED;
	}




void stacktovol (ImageStack stack, byte[][][] vol,int dim1,int dim2, int dim3)
	{
		for (int k=1;k<=dim3;k++)
		 {
			byte[] pixels = (byte[]) stack.getPixels(k);
			IJ.showStatus("processing volume ...: "+k+"/"+dim3);
			for(int  i=0;i<dim1;i++)
			for(int  j=0;j<dim2;j++)			
			{
                                                       int  m=i*(dim2)+j;
			int pix = pixels[m] & 0xff;
                                                        vol[k-1][j][i] =(byte)(pix);
			}
		}
	}


void voltostackbyte(byte[][][] vol,ImageStack stack2,int dim1,int dim2, int dim3)
	{
		for (int k=1;k<=dim3;k++)
		 {
			IJ.showStatus("stacking ...: "+k+"/"+dim3);
			byte[] pixels = new byte[dim1*dim2];	
			for( int i=0;i<dim1;i++)
			for(int  j=0;j<dim2;j++)			
			{
                                                        int m=i*dim2+j;
			pixels[m] =vol[k-1][j][i] ;
			}
			stack2.addSlice("",pixels);
		}

	}

void treatment(byte[][][] m1,int dim1,int dim2,int dim3,byte[][][] m2,int d)

{
  int lw=2*d+1;
  int nw=2*d+1;
  int mw = 2*d+1;
  int lnm=lw*nw*mw;
  int lnm2=lnm/2;
  int lw2=lw/2;
  int nw2=nw/2;
  int mw2=mw/2;
  int lmin=lw2;
  int lmax=dim3-lw2;
  int nmin=nw2;
  int nmax=dim2-nw2;
  int mmin=mw2;
  int mmax=dim1-mw2;
int g;

for (int z=lmin;z<lmax;z++)
{
      int val = (int)((100*z)/lmax);
      IJ.showStatus("3D median filering in progress "+val+'%');

for (int y=nmin;y<nmax;y++)
{
int index=0;
int [] t= new int[lnm];
int [] hist= new int[256];
int sum=0;
int cpt=0;
for (int k=0,zz=z-lw2;k<lw;k++,zz++)
for (int j=0,yy=y-nw2;j<nw;j++,yy++)
for (int i=0,xx=0;i<mw;i++,xx++)
{
sum+=m1[zz][yy][xx] & 0xff;
cpt++;
}

m2[z][y][mmin]=(byte)(sum/cpt);



for(int x=mmin+1;x<mmax;x++)
{
int xxleft=x-mw2-1;
int xxright=x+mw2;
int cpt2=cpt;
for ( int k=0,zz=z-lw2;k<lw;k++,zz++)
for (int j=0,yy=y-nw2;j<nw;j++,yy++)
{
g=m1[zz][yy][xxleft] & 0xff;
sum=sum-g;
cpt2--;
hist[g]--;
g=m1[zz][yy][xxright] & 0xff;
sum=sum+g;
cpt2++;
}


m2[z][y][x]=(byte) (sum/cpt2);
}
}

}



 
}

void borderk(byte[][][] m1,int dim1,int dim2,int dim3,byte[][][] m2,int d)

{
    int i,j,k,ii,jj,kk;
    int vox,vol;
    int jmin,jmax,imin,imax,kmin,kmax;
    int pix;
    for(k=0;k<d;k++)
    {
      for(j=0;j<dim2;j=j+1)
      {
        for(i=0;i<dim1;i=i+1)
        {
        imin = i-d;
        imax = i+d;
        jmin = j-d;
        jmax = j+d;
        kmin = k-d;
        kmax = k+d;
        while (imin < 0) imin++;
        while (jmin < 0) jmin++;
        while (kmin < 0) kmin++;
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        vox=0;
        vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(ii=imin;ii<=imax;ii++)
          for(jj=jmin;jj<=jmax;jj++)
            for(kk=kmin;kk<=kmax;kk++)
	    {
	      vol+= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }

        m2[k][j][i]=(byte) (vol/vox);
	
        }
     }
     }

    for(k=dim3-d;k<dim3;k++)
    {
      for(j=0;j<dim2;j=j+1)
      {
        for(i=0;i<dim1;i=i+1)
        {
        imin = i-d;
        imax = i+d;
        jmin = j-d;
        jmax = j+d;
        kmin = k-d;
        kmax = k+d;
        while (imin < 0) imin++;
        while (jmin < 0) jmin++;
        while (kmin < 0) kmin++;
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        vox=0;
        vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(ii=imin;ii<=imax;ii++)
          for(jj=jmin;jj<=jmax;jj++)
            for(kk=kmin;kk<=kmax;kk++)
	    {
	      vol+= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }

        m2[k][j][i]=(byte) (vol/vox);
	
        }
     }
     }
}

void borderj(byte[][][] m1,int dim1,int dim2,int dim3,byte[][][] m2,int d)

{
    int i,j,k,ii,jj,kk;
    int vox,vol;
    int jmin,jmax,imin,imax,kmin,kmax;
    int pix;
    for(k=0;k<dim3;k=k+1)
    {
      for(j=0;j<d;j++)
      {
        for(i=0;i<dim1;i=i+1)
        {
        imin = i-d;
        imax = i+d;
        jmin = j-d;
        jmax = j+d;
        kmin = k-d;
        kmax = k+d;
        while (imin < 0) imin++;
        while (jmin < 0) jmin++;
        while (kmin < 0) kmin++;
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        vox=0;
        vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(ii=imin;ii<=imax;ii++)
          for(jj=jmin;jj<=jmax;jj++)
            for(kk=kmin;kk<=kmax;kk++)
	    {
	      vol+= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }

        m2[k][j][i]=(byte) (vol/vox);
	
        }
     }
     }

    for(k=0;k<dim3;k=k+1)
    {
      for(j=dim2-d;j<dim2;j++)
      {
        for(i=0;i<dim1;i=i+1)
        {
        imin = i-d;
        imax = i+d;
        jmin = j-d;
        jmax = j+d;
        kmin = k-d;
        kmax = k+d;
        while (imin < 0) imin++;
        while (jmin < 0) jmin++;
        while (kmin < 0) kmin++;
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        vox=0;
        vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(ii=imin;ii<=imax;ii++)
          for(jj=jmin;jj<=jmax;jj++)
            for(kk=kmin;kk<=kmax;kk++)
	    {
	      vol+= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }

        m2[k][j][i]=(byte) (vol/vox);
	
        }
     }
     }
}

void borderi(byte[][][] m1,int dim1,int dim2,int dim3,byte[][][] m2,int d)

{
    int i,j,k,ii,jj,kk;
    int vox,vol;
    int jmin,jmax,imin,imax,kmin,kmax;
    int pix;
    for(k=0;k<dim3;k=k+1)
    {
      for(j=0;j<dim2;j=j+1)
      {
        for(i=0;i<d;i++)
        {
        imin = i-d;
        imax = i+d;
        jmin = j-d;
        jmax = j+d;
        kmin = k-d;
        kmax = k+d;
        while (imin < 0) imin++;
        while (jmin < 0) jmin++;
        while (kmin < 0) kmin++;
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        vox=0;
        vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(ii=imin;ii<=imax;ii++)
          for(jj=jmin;jj<=jmax;jj++)
            for(kk=kmin;kk<=kmax;kk++)
	    {
	     vol += (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }
 
        m2[k][j][i]=(byte) (vol/vox);
	
        }
     }
     }

    for(k=0;k<dim3;k=k+1)
    {
      for(j=0;j<dim2;j=j+1)
      {
        for(i=dim1-d;i<dim1;i++)
        {
        imin = i-d;
        imax = i+d;
        jmin = j-d;
        jmax = j+d;
        kmin = k-d;
        kmax = k+d;
        while (imin < 0) imin++;
        while (jmin < 0) jmin++;
        while (kmin < 0) kmin++;
        while (imax >dim1 - 1) imax--;
        while (jmax > dim2 - 1) jmax--;
        while (kmax >dim3 - 1) kmax--;
        vox=0;
        vol=0;
        int nbvox=(imax-imin+1)*(jmax-jmin+1)*(kmax-kmin+1);
        //IJ.showMessage("nbvox="+nbvox);
        int[] tab=new int[nbvox];
        for(ii=imin;ii<=imax;ii++)
          for(jj=jmin;jj<=jmax;jj++)
            for(kk=kmin;kk<=kmax;kk++)
	    {
	      vol+= (int)(m1[kk][jj][ii] & 0xff);
	      vox++;
	    }

        m2[k][j][i]=(byte) (vol/vox);
	
        }
     }
     }
}

	public void run(ImageProcessor ip) {

		int dimz =stack.getSize();
		int dimy = stack.getWidth();
		int dimx = stack.getHeight();


		GenericDialog dia = new GenericDialog("3D median filter :", IJ.getInstance());	
		dia.addNumericField("Size for mean box filter", valPixel, 0);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		int taille = (int) dia.getNextNumber();

		byte[][][] vol = new byte [dimz][dimy][dimx];	
		byte[][][] mean = new byte [dimz][dimy][dimx];	
		stacktovol(stack,vol,dimx,dimy,dimz);
		treatment(vol,dimx,dimy,dimz,mean,taille);
		borderk(vol,dimx,dimy,dimz,mean,taille);
		borderj(vol,dimx,dimy,dimz,mean,taille);
		borderi(vol,dimx,dimy,dimz,mean,taille);
		vol=null;
		ImageStack stack2 = new ImageStack(stack.getWidth(), stack.getHeight());
		voltostackbyte(mean,stack2,dimx,dimy,dimz);		
		new ImagePlus("mean3D", stack2).show();
		mean=null;


                  }

}

