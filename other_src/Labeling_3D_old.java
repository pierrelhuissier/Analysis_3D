import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.util.Vector;

//  Stack labeling
//  labelize a binarized (0/255) volum 
//  requires the type of neighbouring rule (6 or 26 )
//  generates a short int volum
//  Max number of entities is 32000


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
    String [] connect = {"6","26"};
    String [] border = {"yes","no"};
	int size;
	
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
		return DOES_8G+STACK_REQUIRED;
	}

	void stacktovol (ImageStack stack, short[][] vol,int dim1,int dim2, int dim3, int valPixel)
	{

		if(valPixel==0) val = (byte) 0;
		else val = (byte)1;

		for (int i=1;i<=dim3;i++)
		 {
			byte[] pixels = (byte[]) stack.getPixels(i);
			IJ.showStatus("processing volume ... ");
			IJ.showProgress((double)i/dim3);
			for(int j=0;j<dim1*dim2;j++)
			{
                                                        int  pix = 0xff & pixels[j];
			if( (pix!=0) && (pix!=valPixel)) test=0;
			 if (pix==0) pix_0++;
			 if(pix==valPixel) pix_valPixel++;
			if(val==(byte)1)
			{
			if (pix==0) vol[j][i-1]=(byte)0;
			else  vol[j][i-1]=(byte)1;
			nbpix=pix_valPixel;
			}
			else
			{
			if (pix==0) vol[j][i-1]=(byte)1;
			else  vol[j][i-1]=(byte)0;
			nbpix=pix_0;
			}
			}
		}
	nbrestant = nbpix;
	}


int labeling(short[][] data1, int x, int y, int z, int dim1,int dim2, int dim3, short cc, short cp)

{
   int a,b,c,trouve,voisin,niveau=1;
   int bord =0;
   Vector pv6 = new Vector(1);
pv6.setSize(10);

pv6.setElementAt(new Integer(0),0);

   do
   {
      if (data1[dim2*x+y][z]==  cp)
      {
        voisin=0;
        size++;
        a=x;
        b=y;
        c=z;
        data1[dim2*x+y][z]= (short) (cc & 0xffff);
         if ((a==0) || (a==dim1-1) || (b==0) || (b==dim2-1) || (c==0) || (c==dim3-1)) bord =1;

        nbrestant=nbrestant-1;
	trouve=0;
	while((trouve==0) && (niveau!=0))
	{
	  switch(voisin)
	  {
		case 0:
		{
		        voisin=0;
			x=a;
 			y=b;
          		z=c;
		        x++;
		        if(x<dim1)
                {
		          if (data1[dim2*x+y][z]==  cp)
			     {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 1:
        	{
		        voisin=1;
		        x=a;
 		    	y=b;
          		z=c;
          		x++;
		        y--;
		        if ((x<dim1) && (y>=0))
                {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 2:
	        {
		        voisin=2;
            		x=a;
	            	y=b;
		            z=c;
            		x++;
            		y++;
            		if ((x<dim1) && (y<dim2))
                    {
		          if (data1[dim2*x+y][z]== cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 3:
            	{
		        voisin=3;
              		x=a;
              		y=b;
              		z=c;
              		x++;
              		z--;
              		if ((x<dim1) && (z>=0))
                    {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 4:
              	{
		        voisin=4;
                	x=a;
                	y=b;
                	z=c;
                	x++;
                	y--;
                	z--;
                	if ((x<dim1) && (y>=0) && (z>=0))
                    {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 5:
                {
		        voisin=5;
                  	x=a;
                  	y=b;
                  	z=c;
                  	x++;
                  	y++;
                  	z--;
                  	if ((x<dim1) && (y<dim2) && (z>=0))
                    {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 6:
                {
		        voisin=6;
                	x=a;
                    	y=b;
                    	z=c;
                    	x++;
                    	z++;
                    	if ((x<dim1) && (z<dim3))
                        {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 7:
                {
		        voisin=7;
                      x=a;
                      y=b;
                      z=c;
                      x++;
                      y--;
                      z++;
                      if ((x<dim1) && (y>=0) && (z<dim3))
                      {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 8:
                {
		        voisin=8;
                      x=a;
                      y=b;
                      z=c;
                      x++;
                      y++;
                      z++;
                      if ((x<dim1) && (y<dim2) && (z<dim3))
                      {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 9:
                {
		        voisin=9;
                       x=a;
                       y=b;
                       z=c;
                       x--;
                       if (x>=0)
                       {
		          if (data1[dim2*x+y][z]== cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 10:
                {
		        voisin=10;
                       x=a;
                       y=b;
                       z=c;
                       x--;
                       y--;
                       if ((x>=0) && (y>=0))
                       {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 11:
                {
		        voisin=11;
                       x=a;
                       y=b;
                       z=c;
                       x--;
                       y++;
                       if ((x>=0) && (y<dim2))
                       {
		          if (data1[dim2*x+y][z]== cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 12:
                {
		        voisin=12;
                         x=a;
                         y=b;
                         z=c;
                         x--;
                         z--;
                         if ((x>=0) && (z>=0))
                         {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;

			  }
              }
		}
		case 13:
                {
		        voisin=13;
                         x=a;
                         y=b;
                         z=c;
                         x--;
                         y--;
                         z--;
                         if ((x>=0) && (y>=0) && (z>=0))
                         {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 14:
                {
		        voisin=14;
                          x=a;
                          y=b;
                          z=c;
                          x--;
                          y++;
                          z--;
                          if ((x>=0) && (y<dim2) && (z>=0))
                          {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 15:
                {
		        voisin=15;
                          x=a;
                          y=b;
                          z=c;
                          x--;
                          z++;
                          if ((x>=0) && (z<dim3))
                          {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 16:
                {
		        voisin=16;
                           x=a;
                           y=b;
                           z=c;
                           x--;
                           y--;
                           z++;
                           if ((x>=0) && (y>=0) && (z<dim3))
                           {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 17:
                {
		        voisin=17;
                          x=a;
                          y=b;
                          z=c;
                          x--;
                          y++;
                          z++;
                          if ((x>=0) && (y<dim2) && (z<dim3))
                          {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 18:
                {
		        voisin=18;
                          x=a;
                          y=b;
                          z=c;
                          y--;
                          if ((y>=0))
                          {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 19:
                {
		        voisin=19;
                          x=a;
                          y=b;
                          z=c;
                          y++;
                          if ((y<dim2))
                          {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 20:
                {
		        voisin=20;
                        x=a;
                        y=b;
                        z=c;
                        z--;
                        if ((z>=0))
                        {
		          if (data1[dim2*x+y][z]==cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 21:
                {
		        voisin=21;
                        x=a;
                        y=b;
                        z=c;
                        y--;
                        z--;
                        if ((y>=0) && (z>=0))
                        {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 22:
                {
		        voisin=22;
                         x=a;
                         y=b;
                         z=c;
                         y++;
                         z--;
                         if ((y<dim2) && (z>=0))
                         {
		          if (data1[dim2*x+y][z]== cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 23:
                {
		        voisin=23;
                        x=a;
                        y=b;
                        z=c;
                        z++;
                        if ((z<dim3))
                        {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 24:
                {
		        voisin=24;
                        x=a;
                        y=b;
                        z=c;
                        y--;
                        z++;
                        if ((y>=0) && (z<dim3))
                        {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 25:
                {
		        voisin=25;
                       x=a;
                       y=b;
                       z=c;
                       y++;
                       z++;
                       if ((y<dim2) && (z<dim3))
                       {
		          if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              }
		}
		case 26:
                {
			niveau--;
			int vois = ((Integer) pv6.elementAt(niveau)).intValue();
			voisin = vois+1;
			switch(voisin)
			{
				case 1:
				{
					a--;
					break;
				}
				case 2:
				{
					a--;
                		        b++;
					break;
				}
				case 3:
				{
					a--;
                		        b--;
					break;
				}
				case 4:
				{
					a--;
					c++;
					break;
				}
				case 5:
				{
					a--;
                		        b++;
					c++;
					break;
				}
				case 6:
				{
					a--;
                		        b--;
					c++;
					break;
				}
				case 7:
				{
					a--;
					c--;
					break;
				}
				case 8:
				{
					a--;
                		        b++;
					c--;
					break;
				}
				case 9:
				{
					a--;
                		        b--;
					c--;
					break;
				}
				case 10:
				{
					a++;
					break;
				}
				case 11:
				{
					a++;
                		        b++;
					break;
				}
				case 12:
				{
					a++;
                		        b--;
					break;
				}
				case 13:
				{
					a++;
					c++;
					break;
				}
				case 14:
				{
					a++;
                		        b++;
					c++;
					break;
				}
				case 15:
				{
					a++;
                		        b--;
					c++;
					break;
				}
				case 16:
				{
					a++;
					c--;
					break;
				}
				case 17:
				{
					a++;
                		        b++;
					c--;
					break;
				}
				case 18:
				{
					a++;
                		        b--;
					c--;
					break;
				}
				case 19:
				{
                		        b++;
					break;
				}
				case 20:
				{
                		        b--;
					break;
				}
				case 21:
				{
					c++;
					break;
				}
				case 22:
				{
                		        b++;
					c++;
					break;
				}
				case 23:
				{
                		        b--;
					c++;
					break;
				}
				case 24:
				{
					c--;
					break;
				}
				case 25:
				{
                		        b++;
					c--;
					break;
				}
				case 26:
				{
                		        b--;
					c--;
					break;
				}
			}
		}
	  }
	}
     }
   }while(niveau!=0);
  return bord;
}


int labeling6(short[][] data1, int x, int y, int z, int dim1,int dim2, int dim3, short cc, short cp)

{
   int a,b,c,trouve,voisin,niveau=1;
   int bord=0;
   Vector pv6 = new Vector(1);
pv6.setSize(10);

pv6.setElementAt(new Integer(0),0);

   do
   {
      if (data1[dim2*x+y][z]==  cp)
      {
        voisin=0;
        size++;
    	a=x;
        	b=y;
        	c=z;
                  data1[dim2*x+y][z]= (short) (cc & 0xffff);
                  if ((a==0) || (a==dim1-1) || (b==0) || (b==dim2-1) || (c==0) || (c==dim3-1)) bord =1;
                  nbrestant=nbrestant-1;

	trouve=0;
	while((trouve==0) && (niveau!=0))
	{
	  switch(voisin)
	  {
		case 0:
		{
		        voisin=0;
			x=a;
 			y=b;
          			z=c;
		        	x++;
		        if(x<dim1)
              		        {
		          if (data1[dim2*x+y][z]==  cp)
			     {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
             		       }    
		}
		case 1:
              		  {
		        voisin=1;
                       		x=a;
                      		 y=b;
                       		z=c;
                       		x--;
                       		if (x>=0)
                      	 	{
		        	  if (data1[dim2*x+y][z]== cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              			}
		}
		case 2:
                		{
		        voisin=2;
                         		 x=a;
                          		y=b;
                          		z=c;
                          		y--;
                          		if ((y>=0))
                          		{
		          	if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              			}
		}
		case 3:
                		{
		        voisin=3;
                          		x=a;
                          		y=b;
                          		z=c;
                          		y++;
                          		if ((y<dim2))
                          		{
		          	if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              			}
		}
		case 4:
               		 {
		        voisin=4;
                        		x=a;
                        		y=b;
                        		z=c;
                        		z--;
                        		if ((z>=0))
                        		{
		         	 if (data1[dim2*x+y][z]==cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              			}
		}
		case 5:
            		{
		        voisin=5;
                        		x=a;
                        		y=b;
                        		z=c;
                        		z++;
                        		if ((z<dim3))
                        		{
		          	if (data1[dim2*x+y][z]==  cp)
			  {
			  	trouve=1;
				pv6.setElementAt(new Integer(voisin),niveau);
				niveau++;
				pv6.setSize(pv6.size()+1);
				break;
			  }
              			}
		}
		case 6:
                		{
			niveau--;
			//IJ.showMessage("size"+pv6.size()+" niveau="+niveau);
			int vois = ((Integer) pv6.elementAt(niveau)).intValue();
			voisin = vois+1;
			switch(voisin)
			{
				case 1:
				{
					a--;
					break;
				}
				case 2:
				{
					a++;
					break;
				}
				case 3:
				{
                		        			b++;
					break;
				}
				case 4:
				{
                		       			 b--;
					break;
				}
				case 5:
				{
					c++;
					break;
				}
				case 6:
				{
					c--;
					break;
				}
			}
		}
	  }
	}
     }
   }while(niveau!=0);
return bord;

}

void finalise_labeling(short [][] data2, int dim1,int dim2, int dim3, String ind, String bd )
{
double val;
int bord=0;
          for (int k=0; k<dim3; k++)
          {
        val = nbpix-nbrestant;

              for (int j=0; j<dim2; j++)	

              for (int i=0; i<dim1; i++)
	{
	   if (data2[dim2*i+j][k]==1 && color<32700)
	   {
                    	 color++;
  
                  		 if (ind=="6") 
		{
			size=0;
			bord =labeling6( data2, i,j,k,dim1,dim2,dim3,(short)color,(short)1);	
			if (size<(int)valVoxel) 
			{
			size=0;
			bord = labeling6( data2, i,j,k,dim1,dim2,dim3,(short)0,(short)color);
			color--;
			}
			else if((bd=="no") && (bord==1)) 
			{
			size=0;
			bord = labeling6( data2, i,j,k,dim1,dim2,dim3,(short)0,(short)color);
			color--;
			}
		}
		else 
		{
			size=0;
			bord= labeling( data2, i,j,k,dim1,dim2,dim3,(short)color,(short)1);
			if (size<(int)valVoxel) 
			{
			size=0;
			int coloranc = color;
			bord = labeling( data2, i,j,k,dim1,dim2,dim3,(short) 0,(short)color);
			color--;
			}
			else if((bd=="no") && (bord==1)) 
			{
			size=0;
			bord = labeling( data2, i,j,k,dim1,dim2,dim3,(short)0,(short)color);
			color--;
			}

		}
		 int num =color-1;
	      	 IJ.showStatus("labeling  ...   "+num+" objects found ");
	         }

                     }  
                
          }


}
void voltostackshort (short[][] vol,ImageStack stack2,int dim1,int dim2, int dim3)
	{
		for (int i=1;i<=dim3;i++)
		 {
			IJ.showStatus("stacking ...: "+i+"/"+dim3);
			short[] pixels2 = new short[dim1*dim2];	
			for(int j=0;j<dim1*dim2;j++)
			{
			int pix =  (vol[j][i-1] & 0xffff);
                                                        pixels2[j]=(short)(pix);
			}
			stack2.addSlice("",pixels2);
		}

	}


	public void run(ImageProcessor ip) {

		//connect(0]="6";
		GenericDialog dia = new GenericDialog("3D labeling:", IJ.getInstance());	
		dia.addNumericField("color to label (0 or 255)", valPixel, 0);
		dia.addNumericField("minimum volume to label in voxel", valVoxel, 0);
		dia.addChoice("3D connectivity",connect,connect[0]);
		dia.addChoice("border",border,border[0]);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		valPixel = (int) dia.getNextNumber();
		valVoxel = (int) dia.getNextNumber();
		if((valPixel==0) || (valPixel==255))
		{
		// takes pixels of one slice
		int dimz =stack.getSize();
		int dimy = stack.getWidth();
		int dimx = stack.getHeight();

		String index1 = connect[dia.getNextChoiceIndex()];
		String bord = border[dia.getNextChoiceIndex()];

        		 IJ.showStatus("memory allocation  ...   ");

		short[][] vollab = new short [dimx*dimy][dimz];	

		if(valPixel==0) val = (byte) 0;
		else val = (byte)1;
		stacktovol (stack, vollab,dimx, dimy,dimz,valPixel);

		ImageStack stack2 = new ImageStack(stack.getWidth(), stack.getHeight());
		finalise_labeling(vollab,dimx,dimy,dimz,index1,bord);
		if (color<32700)
		{
		String nbe = new Integer(color-1).toString();
		//IJ.showMessage("number of objects = "+ nbe);
		voltostackshort(vollab,stack2,dimx,dimy,dimz);
		vollab = null;
		new ImagePlus("Labelled stack " +nbe, stack2).show();
		}
		else IJ.showMessage("too many objects (should be < 32700)");

		}
		else IJ.showMessage("value should be 0 or 255"); 

		//IJ.register(Labeling_3D.class);
       }

}

