package org.injector.tools.utils;

import org.apache.commons.io.FileUtils;
import org.injector.tools.config.utils.ManageConfig;
import org.injector.tools.log.Logger;
import org.injector.tools.log.impl.LogErr;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class R {


    public static String separator = File.separator;
    public static String app_name = "injector-tools";
    public static String UserHome = System.getProperty("user.home");


    public static String ConfigPath = UserHome + separator
            + ".config" + separator + app_name;
//	public static String PolipoApp = ConfigPath + separator + "polipo";
    public static String StaticPoint = ConfigPath + separator + "static.json";
    public static String ConfigJsonFile = ConfigPath + separator + "config.json";
    public static String PolipoConfigFile = ConfigPath + separator + "polipo.prop";
    public static String CachePath = UserHome + separator
            + ".cache" + separator + app_name;
    public static String LockFile = CachePath + separator + "lock";
    public static String PolipoCache = CachePath + separator + "polipo" + separator;
    public static String TempDir = "/tmp/";

    static {
        String cacheFolder = "";
        if (PlatformUtil.isWin7OrLater()) {
            cacheFolder = UserHome + separator + "AppData"
                    + separator + "Roaming" + separator + app_name + separator;
            CachePath = cacheFolder + "cache";
            ConfigPath = cacheFolder + "config";

        } else if (PlatformUtil.isWindows()) {

            cacheFolder = UserHome + separator + "Application Data"
                    + separator + app_name + separator;
            CachePath = cacheFolder + "cache";
            ConfigPath = cacheFolder + "config";

        } else if (PlatformUtil.isMac()) { // Library/Application Support/
            cacheFolder = UserHome + separator + "Library" + separator + "Application Support"
                    + separator + app_name + separator;

            CachePath = cacheFolder + "cache";
            ConfigPath = cacheFolder + "config";
            TempDir = UserHome + "/Library/Caches/TemporaryItems/";
        }

        LockFile = CachePath + separator + "lock";
    }

    /* ========================================================================= */
    /* ========================== Resources Methods Init ======================= */
    public static void InitDirs() {

        File creator = new File(ConfigPath);
        MKdir(creator);
        creator = new File(CachePath);
        MKdir(creator);
        creator = new File(PolipoCache);
        MKdir(creator);
//        creator.setReadable(true,false);
//        creator.setWritable(true,false );
    }

    private static void MKdir(File creator) {
        try {
            FileUtils.forceMkdir(creator);
//			Files.setAttribute(creator.toPath(), "dos:hidden", true);
//			Files.setAttribute(creator.getParentFile().toPath(), "dos:hidden", true);
        } catch (Exception e) {
            if (e instanceof IOException) {
                System.err.println("Can't make hidden");
            } else {
                System.err.println("Can't create directory");
            }
        }
    }

    /* ========================================================================= */
    /* ======================== Resources Methods onSave ======================= */

    public static void ReadConfig() {
        String temp = Utils.fromJson(StaticPoint, String.class);
        if (temp != null) {
            R.ConfigJsonFile = temp;
        }
        ManageConfig.readConfig(ConfigJsonFile);
    }

    public static void writeConfigFiles() {
        Utils.toJsonFile(StaticPoint, ConfigJsonFile);
        ManageConfig.writeConfig(ConfigJsonFile);
    }


    public static void DeleteTemp() {
        File temp = new File(ConfigPath);
        try {
            delete(temp);
        } catch (IOException e) {
            System.out.println("Can't delete Files");
        }
    }

    public static void delete(File file) throws IOException {

        if (file.isDirectory()) {

            // directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
//				System.out.println("Delete Directory : "+ file.getAbsolutePath());

            } else {

                // list all the directory contents
                String[] files = file.list();

                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);

                    // recursive delete
                    delete(fileDelete);
                }

                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
//					System.out.println("Delete Directory : "+ file.getAbsolutePath());
                }
            }

        } else {
            // if file, then delete it
            file.delete();
//			System.out.println("Delete File : " + file.getAbsolutePath());
        }
    }

    public static void openInProcess(String str) {
        List<String> list = new ArrayList<String>();

        if (PlatformUtil.isLinux()) {
            list.add("xdg-open");
            list.add(str);
        } else if (PlatformUtil.isWindows()) {
            list.add("start");
            list.add(str);
        } else if (PlatformUtil.isMac()) {
            list.add("open");
            list.add(str);
        }
        ProcessBuilder builder = new ProcessBuilder(list);
        try {
            builder.start();
        } catch (IOException e) {
            Logger.debug("R", "error in open Application");
            Logger.debug("R", e.getMessage());
        }

    }

    public static void SAVE_CHANGES() {
//		ConfigurationManager.updateConfig();
        writeConfigFiles();
    }

    public static void releaseLockFile() {
        FileUtils.deleteQuietly(new File(LockFile));
    }

    public static void initFileLock() {
        try {
            File file = new File(LockFile);
            FileUtils.writeStringToFile(file, " -- Start App" + System.currentTimeMillis() + "\n", Charset.defaultCharset(), true);
        } catch (Exception ignored) {}
    }


    public static void Save_Changes_Progress() {

//		ConfigurationManager.updateConfig();
        writeConfigFiles();
    }

    public static void INIT_CHANGES() {
        InitDirs();
        var logErr = new LogErr();
        logErr.initDebugger(ConfigPath + separator + "logger.log");
        Logger.setLogger(ManageConfig.getAppConfig().getEnableLogs(), logErr);
        new Thread(logErr).start();
    }

}
