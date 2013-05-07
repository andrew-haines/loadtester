package com.ahaines.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertiesSetter {

	private final static String PROP_DIR = System.getProperty("property_dir","..");
	public static Properties getPropertiesFromDir(File propDir) throws IOException{
		
		File[] propsFiles = propDir.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				// do not add the DEV.properties file if there is one...
				return pathname.isFile() && !pathname.getName().equals("DEV.properties");
			}
			
		});
		
		Properties props = new Properties();
		
		if (propsFiles != null){
		
			for (File file: propsFiles){
				loadPropsFromFile(file, props);
			}
		}
		
		// now add the DEV properties file
		
		File devFile = new File(propDir, "DEV.properties");
		
		if (devFile.exists()){
			System.out.println("loading dev properties");
			loadPropsFromFile(devFile, props);
		}
		
		return props;
	}

	private static void loadPropsFromFile(File file, Properties props) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		props.load(reader);
	}

	public static void loadSysPropsFromPropertyDir() throws IOException {
		
		
		System.out.println("using property base as: "+PROP_DIR);
		
		// read in the local properties directory
		
		File propDir = new File(PROP_DIR);
		
		Properties props = PropertiesSetter.getPropertiesFromDir(propDir);
		
		props.putAll(System.getProperties());
		
		System.setProperties(props);
	}
	
	public static String getPropertyDir(){
		return PROP_DIR;
	}
}
