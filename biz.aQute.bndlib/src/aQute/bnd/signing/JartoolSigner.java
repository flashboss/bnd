package aQute.bnd.signing;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import aQute.bnd.service.*;
import aQute.lib.osgi.*;
import aQute.libg.command.*;
import aQute.libg.reporter.*;

/**
 * Sign the jar file.
 * 
 * -sign : <alias> [ ';' 'password:=' <password> ] [ ';' 'keystore:=' <keystore> ] [
 * ';' 'sign-password:=' <pw> ] ( ',' ... )*
 * 
 * @author aqute
 * 
 */
public class JartoolSigner implements Plugin, SignerPlugin {
    String keystore;
    String storetype;
    String path = "jarsigner";
    String storepass;
    String keypass;
    String sigFile;

    public void setProperties(Map<String, String> map) {
        if (map.containsKey("keystore"))
            this.keystore = map.get("keystore");
        if (map.containsKey("storetype"))
            this.storetype = map.get("storetype");
        if (map.containsKey("storepass"))
            this.storepass = map.get("storepass");
        if (map.containsKey("keypass"))
            this.keypass = map.get("keypass");
        if (map.containsKey("path"))
            this.path = map.get("path");
        if (map.containsKey("sigFile"))
            this.sigFile = map.get("sigFile");

    }

    public void setReporter(Reporter processor) {
    }

    public void sign(Builder builder, String alias) throws Exception {    	    	
    	File f = builder.getFile(keystore);
    	if ( !f.isFile()) {
    		builder.error("Invalid keystore %s", f.getAbsolutePath() );
    		return;
    	}
    	
        Jar jar = builder.getJar();
        File tmp = File.createTempFile("signdjar", ".jar");
        tmp.deleteOnExit();

        jar.write(tmp);

        Command command = new Command();
        command.add(path);
        if (keystore != null) {
            command.add("-keystore");
            command.add(f.getAbsolutePath());
        }

        if (storetype != null) {
        	command.add("-storetype");
        	command.add(storetype);
        }

        if (keypass != null) {
        	command.add("-keypass");
        	command.add(keypass);
        }

        if (storepass != null) {
        	command.add("-storepass");
        	command.add(storepass);
        }

        if (sigFile != null) {
        	command.add("-sigFile");
        	command.add(sigFile);
        }

        command.add(tmp.getAbsolutePath());
        command.add(alias);
        builder.trace("Jarsigner command: %s", command);
        command.setTimeout(20, TimeUnit.SECONDS);
        StringBuffer out = new StringBuffer();
        StringBuffer err = new StringBuffer();
        int exitValue = command.execute(System.in, out, err);
        if (exitValue != 0) {
            builder.error("Signing Jar out: %s\nerr: %s", out, err);
        } else {
            builder.trace("Signing Jar out: %s \nerr: %s", out, err);
        }

        Jar signed = new Jar(tmp);
        builder.addClose(signed);

        Map<String, Resource> dir = signed.getDirectories().get("META-INF");
        for (String path : dir.keySet()) {
            if (path.matches(".*\\.(DSA|RSA|SF|MF)$")) {
                jar.putResource(path, dir.get(path));
            }
        }
        jar.setDoNotTouchManifest();
    }

    StringBuffer collect(final InputStream in) throws Exception {
        final StringBuffer sb = new StringBuffer();
        
        Thread tin = new Thread() {
            public void run() {
                try {
                    BufferedReader rdr = new BufferedReader(new InputStreamReader(in, Constants.DEFAULT_CHARSET));
                    String line = rdr.readLine();
                    while (line != null) {
                        sb.append(line);
                        line = rdr.readLine();
                    }
                    rdr.close();
                    in.close();
                } catch (Exception e) {
                    // Ignore any exceptions
                }
            }
        };
        tin.start();
        return sb;
    }
}
