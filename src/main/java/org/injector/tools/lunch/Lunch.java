package org.injector.tools.lunch;

import org.injector.tools.config.Config;
import org.injector.tools.config.utils.ManageConfig;
import org.injector.tools.log.Logger;
import org.injector.tools.utils.R;
import org.terminal.Ansi;

public class Lunch {

    public static void main(String[] args) {

        checkArgs(args);
        initApp();
        var service = new InjectionTools(ManageConfig.getAppConfig());

        service.startLocalProxyService();
        service.startJschSSHService();

        // service.jschSSHClient.addSuccessListener(service::StartVPNService);

        // service.StartSSHService();
        //service.StartPolipoService();
        // service.StartRedSocksService();
    }

    public static void checkArgs(String[] args) {
        if (args.length == 1) {
            switch (args[0]) {
                case "-h", "--help", "help" -> {
                    printHelp();
                    System.exit(1);
                }
                case "-t", "--temp", "temp" -> {
                    Config.CreateJsonTemplate();
                    System.exit(1);
                }
                case "-v", "--version", "version" -> {
                    System.out.println(new Config().getVersion());
                    System.exit(1);
                }
                default -> ManageConfig.readConfig(args[0]);
            }
        } else if (args.length == 2) {
            if (args[0].equals("-t") || args[0].equals("--temp") || args[0].equals("temp")) {
                Config.CreateJsonTemplate(args[1]);
                System.exit(1);
            }
        } else {
            ManageConfig.readConfig(R.ConfigJsonFile);
        }
    }

    public static void initApp() {
        R.INIT_CHANGES();
        if (ManageConfig.getAppConfig().getEnableLogs())
//			System.out.println(ManageConfig.toFormatConfig());
            System.out.println(Ansi.Green + ManageConfig.formatLimitConfig() + Ansi.Reset);

//		Logger.debug(Lunch.class,"", ManageConfig.formatLimitConfig());

        // System.out.println(ManageConfig.getAppConfig().toJson());
        R.Save_Changes_Progress();
        Logger.debug(Lunch.class, "start service");
    }

    public static void printHelp() {
        System.out.println("=============================== HELP ==============================");
        System.out.println(" -h			show help message");
        System.out.println(" -v			show app version");
        System.out.println(" [-t <filename>]	create json template configuration file");
        System.out.println(" [<filename>]		load config file");
        System.out.println("   filename:		the name of the file to be load");
        System.out.println("  			if no config file loaded will use default config (cache dir)");
        System.out.println("  -h:help,	-v:version,	-t:temp");
        System.out.println("=============================== HELP ==============================");
    }

}
