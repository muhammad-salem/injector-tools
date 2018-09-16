package org.injector.tools.lunch;

import org.fusesource.jansi.AnsiConsole;
import org.injector.tools.config.Config;
import org.injector.tools.config.utils.ManagConfig;
import org.injector.tools.log.Logger;
import org.injector.tools.utils.R;
import org.terminal.Ansi;

public class Lunch {

	public static void main(String[] args) {
		
		AnsiConsole.systemInstall();

		checkArgs(args);
		initApp();
		InjectionTools service = new InjectionTools(ManagConfig.getAppConfig());
		
		service.StartLocalProxyService();
		service.StartJschSSHService();

		// service.jschSSHClient.addSuccessListener(service::StartVPNService);

		// service.StartSSHService();
		//service.StartPolipoService();
		// service.StartRedSocksService();
	}

	public static void checkArgs(String[] args) {
		if (args.length == 1) {
			if (args[0].equals("-h") || args[0].equals("--help") || args[0].equals("help")) {
				printHelp();
				System.exit(1);
			} else if (args[0].equals("-t") ||args[0].equals("--temp") || args[0].equals("temp")) {
				Config.CreateJsonTemplet();
				System.exit(1);
			} else if (args[0].equals("-v") || args[0].equals("--version") || args[0].equals("version")) {
				System.out.println(new Config().getVersion());
				System.exit(1);
			} else {
				ManagConfig.readConfig(args[0]);
			}
		} else if (args.length == 2) {
			if (args[0].equals("-t") ||args[0].equals("--temp") || args[0].equals("temp")) {
				Config.CreateJsonTemplet(args[1]);
				System.exit(1);
			}

		} else {
			ManagConfig.readConfig(R.ConfigJsonFile);
			ManagConfig.setPolipoDir(R.PolipoConfigFile, R.PolipoCache);
		}
	}

	public static void initApp() {

		R.INIT_CHANGES();
		if(ManagConfig.getAppConfig().isDebuggable())
//			System.out.println(ManagConfig.toFormatConfig());
			System.out.println(Ansi.Green + ManagConfig.formatLimitConfig() + Ansi.Reset);
		
//		Logger.debug(Lunch.class,"", ManagConfig.formatLimitConfig());
		
		// System.out.println(ManagConfig.getAppConfig().toJson());
		R.Save_Changes_Progress();
		Logger.debug(Lunch.class, "start service");
	}

	public static void printHelp() {
		System.out.println("=============================== HELP ==============================");
		System.out.println(" -h			show help message");
		System.out.println(" -v			show app version");
		System.out.println(" [-t <filename>]	create json templete configuration file");
		System.out.println(" [<filename>]		load config file");
		System.out.println("   filename:		the name of the file to be load");
		System.out.println("  			if no config file loaded will use default config (cache dir)");
		System.out.println("  -h:help,	-v:version,	-t:temp");
		System.out.println("=============================== HELP ==============================");
	}

}
