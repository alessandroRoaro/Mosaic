package ai.cogmission.mosaic.refimpl.javafx;

import ai.cogmission.mosaic.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleTester {
	
	
	public final static class JavaProcess {

	    private JavaProcess() {}        

	    public static int exec(Class<?> klass, String... modelSpec) throws IOException, InterruptedException {
	        String javaHome = System.getProperty("java.home");
	        String javaBin = javaHome +
	                File.separator + "bin" +
	                File.separator + "java";
	        String classpath = System.getProperty("java.class.path");
	        String className = klass.getCanonicalName();
	        
	        List<String> argList = new ArrayList<String>();
	        argList.add(javaBin);
	        argList.add("-cp");
	        argList.add(classpath);
	        argList.add(className);
	        argList.addAll(Arrays.asList(modelSpec));
	        ProcessBuilder builder = new ProcessBuilder(argList);

	        builder.redirectErrorStream(true);
	        
	        final Process process = builder.start();
	        
	        (new Thread() {
	        	public void run() {
	        		BufferedReader isr = null;
	        		try {
	        			isr = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        			String line = null;
	        			while((line = isr.readLine()) != null) {
	        				Log.d(line);
	        			}
	        		}catch(Exception e) {
	        			e.printStackTrace();
	        		}
	        	}
	        }).start();
	        
	        process.waitFor();
	        
	        return process.exitValue();
	    }

	}
	
	public static void main(String[] args) {
		try {
//		    URL url = MosaicPaneRefImpl.class.getResource("testModel.txt");
//            String path = Paths.get(url.toExternalForm()).toAbsolutePath().toString();
//            try{
//                path = Paths.get(url.toURI()).toAbsolutePath().toString();
//            }catch(Exception e) { e.printStackTrace(); }
			//Pivot
			//JavaProcess.exec(ai.cogmission.layout.mosaic.pivot.MosaicPaneRefImpl.class, "--file=testModel.txt", "--surface=model6");
			//DesktopApplicationContext.main(MosaicPaneRefImpl.class, new String[] { "--file=testModel.txt", "--surface=model6"});
			
			//JavaFX
			String path = "C:/Users/PC-1-/workspace/Mosaic/out/production/resources/testModel.txt";
			JavaProcess.exec(MosaicPaneRefImpl.class, "--file=" + path, "--surface=model6");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
