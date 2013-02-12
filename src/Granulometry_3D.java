/**
 * Granulometry 3D 
 * version v1.1.2 - 06/09/2012
 * Pierre Lhuissier - Luc Salvo - Vincent Boulos 
 * SIMaP/GPM2 - Grenoble University - CNRS - France
 */

import ij.*;
import ij.plugin.PlugIn;
import ij.gui.*;
import ij.io.*;
import java.io.*;

import segmentation_tools_bit.DiErToolsBit;


public class Granulometry_3D implements PlugIn {
	
	static double valPixel=1;
	ImagePlus Imp;
    protected ImageStack stack;
    String [] ele = {"oct","cube"};
	String element;

/*public int setup(String arg, ImagePlus imp) {
		stack=imp.getStack();
		Imp=imp;
		return DOES_8G+STACK_REQUIRED;
}*/

 public void run(String arg) {

	    Imp = WindowManager.getCurrentImage();
	    stack=Imp.getStack();
	 	GenericDialog dia = new GenericDialog("3D granulometry :", IJ.getInstance());	
		dia.addChoice("Structural_element",ele,ele[0]);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		element = dia.getNextChoice();

        SaveDialog sd = new SaveDialog("Save Data in ...", "volume", ".txt");
        String name = sd.getFileName();
        if (name == null)
            return;
        String directory = sd.getDirectory();
		PrintStream pw = null;
		
		IJ.showStatus("Data conversion ...");
		DiErToolsBit DilEro = new DiErToolsBit(stack);
	
		stack=null;
		Imp.changes=false; //In order to avoid "save" dialog window
		Imp.close();
		System.gc();
		
		long[] src = DilEro.getData();
		int nbnoir = DilEro.pixelsCount();
		int noir2anc = nbnoir;
		int noir = nbnoir;
		int noir2=0;
		int taille = 1;
   
		
		IJ.showProgress(0);
		try {
            pw = new PrintStream(new AppendFileStream(directory+name));
			}
			catch (IOException e) {
				// IJ.write("pb" + e);
				return;
			}
		
		   pw.print(0);
		   pw.print(" ");
		   pw.print(nbnoir);
		   pw.print(" ");
		   pw.println(0.);
		   pw.close();
		   
		while (noir !=0)
		{
			DilEro.setData(src);
			DilEro.erode(1, element);
			noir = DilEro.pixelsCount();
			noir2 = 0;
			src = null;
			src = DilEro.getData();

			if (noir!=0)
	        {
				DilEro.dilate(taille, element);
				noir2 = DilEro.pixelsCount();
	        }
			
			double val = 1.0*(noir2anc-noir2)/nbnoir;
			IJ.showStatus("3D granulometry in progress .."+ (int)(100.*(nbnoir-noir2)/nbnoir)+"% done         particules_size=" + (2*taille));
			IJ.showProgress((nbnoir-noir2),nbnoir);
			try {
               pw = new PrintStream(new AppendFileStream(directory+name));
			}
			catch (IOException e) {
           // IJ.write("pb" + e);
            return;
			}
			
		   pw.print(2*taille);
		   pw.print(" ");
		   pw.print(noir2);
		   pw.print(" ");
		   pw.println(val);
		   pw.close();
		   
		   noir2anc = noir2;
		   taille++;
	   }    
	double val =0;
   try {
               pw = new PrintStream(new AppendFileStream(directory+name));
        }
        catch (IOException e) {
           // IJ.write("pb" + e);
            return;
        }	

   pw.print(2*taille);
   pw.print(" ");
   pw.print(noir2);
   pw.print(" ");
   pw.print(val);
  pw.println(" ");
  pw.println(" ");
  pw.close();
 
 
  //IJ.showMessage("time ms : "+endTime1);
   //IJ.showMessage("3D granulometry completed");
  // IJ.register(granu_3D.class);	
  }


class AppendFileStream extends OutputStream {
   RandomAccessFile fd;
   public AppendFileStream(String file) throws IOException {
     fd = new RandomAccessFile(file,"rw");
     fd.seek(fd.length());
     }
   public void close() throws IOException {
     fd.close();
     }
   public void write(byte[] b) throws IOException {
     fd.write(b);
     }
   public void write(byte[] b,int off,int len) throws IOException {
     fd.write(b,off,len);
     }
   public void write(int b) throws IOException {
     fd.write(b);
     }
   }
  }
