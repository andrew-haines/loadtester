package com.ahaines.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import com.ahaines.properties.PropertiesSetter;

public class BootStrap {

	public static void main(String args[]) throws Exception{
		
		PropertiesSetter.loadSysPropsFromPropertyDir();
		 
        Server server = new Server(8080);
 
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/g");
        webapp.setWar(PropertiesSetter.getPropertyDir()+"/webapps/g.war");
        server.setHandler(webapp);
 
        server.start();
        System.out.println("started jetty");
        server.join();
	}
}
