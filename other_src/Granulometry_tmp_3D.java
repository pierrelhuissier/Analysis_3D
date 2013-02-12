
import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import java.io.*;

import segmentation_tools_bit.DiErToolsBit;


public class Granulometry_tmp_3D implements PlugInFilter {

	static double valPixel=1;
	ImagePlus Imp;
    protected ImageStack stack;
    String [] ele = {"oct","cube"};
	String element;

public int setup(String arg, ImagePlus imp) {
		stack=imp.getStack();
		return DOES_8G+STACK_REQUIRED;
}

 public void run(ImageProcessor ip) {

	    Imp = WindowManager.getCurrentImage();
	 	/**
	    GenericDialog dia = new GenericDialog("3D granulometry :", IJ.getInstance());	
		dia.addChoice("Structural element",ele,ele[0]);
		dia.showDialog();
		
		if (dia.wasCanceled()) return;
		
		if(dia.invalidNumber()) {
			IJ.showMessage("Error", "Invalid input Number");
			return;
		}
		
		element = dia.getNextChoice();
	*/
	    element=ele[0];
	    
	    String directory = IJ.getDirectory("image");
	    String name = "granulometry_result.txt";
	    
/**        SaveDialog sd = new SaveDialog("Save Data in ...", "volume", ".txt");
        String name = sd.getFileName();
        if (name == null)
            return;
        String directory = sd.getDirectory();
	*/
	    PrintStream pw = null;
		
		DiErToolsBit DilEro = new DiErToolsBit(stack);
	

		stack=null;
		Imp.close();
		
		IJ.showStatus("memory allocation ...");
		
		long[] src = DilEro.getData();
		int nbnoir = DilEro.pixelsCount();

		
		int noir2anc = nbnoir;
		int noir = nbnoir;
		int noir2=0;
		int taille = 1;
   
		long startTime = System.currentTimeMillis();
		
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
		   pw.print(0.);
		   pw.print(" ");
		   pw.println(0.);
		   pw.close();
		   
		while (noir !=0)
		{
			//IJ.showMessage("erode");
			//IJ.showMessage("info on src");
			DilEro.setData(src);
			DilEro.erode(1, element);
			noir = DilEro.pixelsCount();
			noir2 = 0;
			//src = new long[DilEro.getSize()];
			src = DilEro.getData();
			
			if (noir!=0)
	        {
				//IJ.showMessage("dilate "+taille);
				//IJ.showMessage("info on data");
				DilEro.dilate(taille, element);
				noir2 = DilEro.pixelsCount();
	        }
			double val = 1.0*(noir2anc-noir2)/nbnoir;
			IJ.showStatus("3D granulometry in progress .."+ (int)(100.*(nbnoir-noir2)/nbnoir)+"% done         particules_size=" + (2*taille));
	   
			try {
               pw = new PrintStream(new AppendFileStream(directory+name));
			}
			catch (IOException e) {
           // IJ.write("pb" + e);
            return;
			}
			  long currentTime = System.currentTimeMillis()-startTime ;
		   pw.print(2*taille);
		   pw.print(" ");
		   pw.print(noir2);
		   pw.print(" ");
		   pw.print(val);
		   pw.print(" ");
		   pw.println(currentTime);
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
        long endTime1 = System.currentTimeMillis()-startTime ;		

   pw.print(2*taille);
   pw.print(" ");
   pw.print(noir2);
   pw.print(" ");
   pw.print(val);
   pw.print(" ");
   pw.println(endTime1);
  pw.println(" ");
  pw.println(" ");
  pw.close();
 
  IJ.getInstance().quit();
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
