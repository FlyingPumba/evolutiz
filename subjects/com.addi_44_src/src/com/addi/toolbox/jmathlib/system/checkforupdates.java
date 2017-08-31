// Copyright (C) 2011 Free Software Foundation FSF
//
// This file is part of Addi.
//
// Addi is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or (at
// your option) any later version.
//
// Addi is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Addi. If not, see <http://www.gnu.org/licenses/>.

package com.addi.toolbox.jmathlib.system;


import java.net.*;
import java.util.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;

/**An external function for checking for updates over the network*/
public class checkforupdates extends ExternalFunction
{
    GlobalValues globals = null;
    
	public OperandToken evaluate(Token[] operands, GlobalValues _globals)
	{
	    
	    // copy pointer to globals
	    globals = _globals;

		String s           = "";
        String lineFile    = "";
 		boolean silentB    = false;
        
 		// check the information of the primary update site
 		String updateSiteS = "http://www.jmathlib.de/updates/";
        s = globals.getProperty("update.site.primary");

        // if update-site information is not available try the secondary site
        if (s==null)
            s = globals.getProperty("update.site.secondary");
            
        // if primary update site or secondary site information is available
        //    take it
        if (s != null)
            updateSiteS = s;

        // check the arguments 
        if (getNArgIn(operands) == 1)
        {    
        	if ((operands[0] instanceof CharToken))
        	{
                String st = ((CharToken)operands[0]).getElementString(0);
                if (st.equals("-silent"))
                {
                    // silent check for updates is requested
                    silentB = true;
                }
                else
                {
                    // argument is maybe a different update site URL
                    updateSiteS = st; 
                    globals.getInterpreter().displayText("New Update Site "+updateSiteS);
                }
        	}
        }

        // inform the user about checking the update site
        if (!silentB)
            globals.getInterpreter().displayText("Checking for Updates at "+updateSiteS);
        
        // check the last date when an update has been performed
        String[] lastUpdateS = globals.getProperty("update.date.last").split("/");
        int year  = Integer.parseInt(lastUpdateS[0]);
        int month = Integer.parseInt(lastUpdateS[1])-1;
        int day   = Integer.parseInt(lastUpdateS[2]);
        //getInterpreter().displayText("check:"+year+"/"+month+"/"+day);
                
        // read the interval between updates
        int intervall = Integer.parseInt(globals.getProperty("update.intervall"));

        // get the current date
        GregorianCalendar calFile = new GregorianCalendar(year,month,day);
        GregorianCalendar calCur  = new GregorianCalendar();
        
        // add update-interval to the current date
        calFile.add(Calendar.DATE,intervall);


        if (silentB)
        {
            // if silent-mode is active only check for updates when update intervall has been reached
            if  (calCur.after(calFile) )
            {
                checkForUpdatesThread ch = new checkForUpdatesThread(updateSiteS, silentB);
            }
        }
        else
        {
            checkForUpdatesThread ch = new checkForUpdatesThread(updateSiteS, silentB);
        }
        return null; 		

    } // end evaluate
    
	
    // create separate thread for checking the update site, because this may take
    //  some time 
    public class checkForUpdatesThread extends Thread
    {
        
        String updateSiteS = "";
        boolean silentB = false;
        
        public checkForUpdatesThread(String _updateSiteS, boolean _silentB)
        {
        
            updateSiteS = _updateSiteS;
            silentB     = _silentB;
            
            Thread runner = new Thread(this);
            runner.start();
            
            System.out.println("checkForUpdates: constructor");
        }
        
        /**
         * separate thread which checks the update site
         * It is a thread in order to cause no time delay for the user.
         */
        public synchronized void run()
        { 
            
            String s;
            

            // open URL
            URL url = null;
            try
            {
                // get local version of jmathlib
                String localVersionS = globals.getProperty("jmathlib.version").replaceAll("/", ".");

                url = new URL(updateSiteS+"?jmathlib_version="+localVersionS+"&command=check");
            }
            catch (Exception e)
            {
                throwMathLibException("checkForUpdates: malformed url");
            }          
            
            // load information from the update server
            Properties props = new Properties();
            try 
            {
                 props.load(url.openStream() );
            }
            catch (Exception e)
            {
               ErrorLogger.debugLine("checkForUpdates: Properties error");    
            }

            String localVersionS  = globals.getProperty("jmathlib.version");
            String updateVersionS = props.getProperty("update.toversion");
            String updateActionS  = props.getProperty("update.action");

            // evaluate the response from the web server
            if (updateActionS.equals("INCREMENTAL_DOWNLOAD"))
            {
                
                if (!silentB)
                {
                    globals.getInterpreter().displayText("A full download ist required");
                    globals.getInterpreter().displayText("A new version "+ updateVersionS +" is available");
                    globals.getInterpreter().displayText("\n Just type    update    at the prompt.");
                }

            }
            else if (updateActionS.equals("FULL_DOWNLOAD_REQUIRED"))
            {
                if (!silentB)
                {
                    globals.getInterpreter().displayText("A full download ist required");
                    globals.getInterpreter().displayText("A new version "+ updateVersionS +" is available");
                    globals.getInterpreter().displayText("Go to www.jmathlib.de and download the latest version");
                }
            }
            else if (updateActionS.equals("NO_ACTION"))
            {
                if (!silentB)
                    globals.getInterpreter().displayText("The local version of JMathLib is up to date");

            }
            else if (updateActionS.equals("VERSION_UNKNOWN"))
            {
                if (!silentB)
                    globals.getInterpreter().displayText("The local version of JMathLib ist not recognized by the server");

            }
            else
            {
                // wrong or unknown or empty "action"-command
                globals.getInterpreter().displayText("check for updates encountered an error.");
            }
            
            debugLine("checkForUpdates: web:" + updateVersionS +" local:"+ localVersionS);
            
            // set current date for property "update.date.last"
            Calendar cal   = Calendar.getInstance();
            String checkedDate  = Integer.toString(cal.get(Calendar.YEAR))     + "/"
                                + Integer.toString(cal.get(Calendar.MONTH)+1)  + "/"
                                + Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            globals.setProperty("update.date.last", checkedDate);

        
           
            // copy properties from webserver to global properties
            Enumeration propnames = props.propertyNames();
            while (propnames.hasMoreElements())
            {
                String propName  = (String)propnames.nextElement();
                String propValue = (String)props.getProperty(propName);
                ErrorLogger.debugLine("Property: "+propName+" = "+propValue);
                globals.setProperty(propName, propValue);
            }


        }
        
    }
}

/*
@GROUP
system
@SYNTAX
checkForUpdates()
checkForUpdates(site)
checkForUpdates("-silent")
@DOC
This functions checks via network if the current 
installation of JMathLib is up to date.

This functions is also called during startup of JMathLib's GUI.
@EXAMPLE
checkforupdates()
@NOTES
This functions checks via network if the current 
installation of JMathLib is up to date.

This functions is also called during startup of JMathLib's GUI.
@SEE
update
*/
