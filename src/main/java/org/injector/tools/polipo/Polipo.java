package org.injector.tools.polipo;

import org.injector.tools.config.PolipoConfig;
import org.injector.tools.log.Logger;
import org.injector.tools.utils.PlatformUtil;
import org.injector.tools.utils.R;
import org.injector.tools.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Polipo {

    List<String> args;
    ProcessBuilder processBuilder;
    Process process;

    PolipoConfig config;

    public Polipo(PolipoConfig config) {
        this.config = config;
        initPolipo();
    }


    private void initPolipo() {
        File destination = null;
        if (PlatformUtil.isWindows()) {
            URL source = getClass().getResource("polipo.exe");
            destination = new File(R.ConfigPath, "polipo.exe");
            Utils.Copy(source, destination);

        } else if (PlatformUtil.isUnix()) {
            String polipoName = PlatformUtil.isAMD64() ? "polipo" : "polipo.i386";
            URL source = getClass().getResource(polipoName);
            destination = new File(R.ConfigPath, "polipo");
            Utils.Copy(source, destination);

        } else if (PlatformUtil.isMac()) {
            URL source = getClass().getResource("polipo.ios");
            destination = new File(R.ConfigPath, "polipo");
            Utils.Copy(source, destination);
        }
        destination.setExecutable(true);
        String polipoFileName = destination.getAbsolutePath();
        try {
            config.createPolipoConfigFile();
        } catch (IOException e) {
            Logger.debug(getClass(), "can't create polipo config file");
            e.fillInStackTrace();
            return;
        }

        args = new ArrayList<String>();
        args.add(polipoFileName);
        args.add("-c");
        args.add(config.getPolipoConfigFile());
    }


    public void start() {
        processBuilder = new ProcessBuilder(args);
        try {
            process = processBuilder.start();
            byte[] b = new byte[1024];
            int l = 0;
            while ((l = process.getErrorStream().read(b)) != -1) {
                Logger.debug(getClass(), new String(b, 0, l));
            }
            process.destroy();
            process = null;
        } catch (IOException e) {
            Logger.debug(getClass(), "error in open " + e.getMessage());
        }
    }


    /**
     * @see Process#destroy()
     */
    public void destroy() {
        process.destroy();
//		args = new ArrayList<String>();
//		args.add("kill");
//		args.add("polipo");
//
//		processBuilder = new ProcessBuilder(args);
//		try {
//			process = processBuilder.start();
//
//		} catch (IOException e) {
//			Logger.debug(this.getClass().getSimpleName(), "error in open " + e.getMessage());
//		}

    }

    /**
     * @return
     * @see Process#isAlive()
     */
    public boolean isAlive() {
        if (process == null) return false;
        return process.isAlive();
    }


}
