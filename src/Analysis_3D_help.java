/**
 * 
 * @author Luc Salvo and Pierre Lhuissier - SIMaP/GPM2 - Grenoble University
 * Image/J Plugins
 * Copyright (C) 2012 SIMaP/GPM2
 * pierre.lhuissier@simap.grenoble-inp.fr
 * version 1.1 - 06/09/2012
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ij.IJ;
import ij.plugin.PlugIn;
import ij.text.TextWindow;


public class Analysis_3D_help implements PlugIn {
     
public void run(String arg) {
//IJ.showMessage("Documentation writing in progess ....\n\n Please contact Pierre Lhuissier for help : \n\n pierre.lhuissier@simap.grenoble-inp.fr\n\n");	
	//String pluginDir = IJ.getDirectory("plugins");
	//IJ.open(pluginDir+"Analysis_3D.jar/README.txt");
	new TextWindow("Analysis 3D help", getText("Analysis_3D_help.txt"), 450, 450);
}


//Loads a text file from within a JAR file using getResourceAsStream().
String getText(String path) {
    String text = "";
    try {
        // get the text resource as a stream
        InputStream is = getClass().getResourceAsStream(path);
        if (is==null) {
            IJ.showMessage("JAR Demo", "File not found in JAR at "+path);
            return "";
        }
        InputStreamReader isr = new InputStreamReader(is);
        StringBuffer sb = new StringBuffer();
        char [] b = new char [8192];
        int n;
        //read a block and append any characters
        while ((n = isr.read(b)) > 0)
            sb.append(b,0, n);
        // display the text in a TextWindow
        text = sb.toString();
    }
    catch (IOException e) {
        String msg = e.getMessage();
        if (msg==null || msg.equals(""))
            msg = "" + e;	
        IJ.showMessage("JAR Demo", msg);
    }
    return text;
}

}


