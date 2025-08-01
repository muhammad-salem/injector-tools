package org.injector.tools.speed;


import org.injector.tools.utils.Utils;
import org.terminal.Ansi;

public class TerminalNetworkMonitor extends NetworkMonitorSpeed {

    // ↓↑⇔⇧⇩⇅⛗⌚▽△▲▼⬆⬇⬌

    @Override
    public void run() {
        String summery = "\n";
        Ansi ansi = new Ansi();
        long hh, mm, ss;
        while (showSpeed) {
            hh = ((timer / 60) / 60) % 60;
            mm = (timer / 60) % 60;
            ss = timer % 60;

            summery = "";
            summery += Ansi.EraseLine + "\n";
            summery += Ansi.EraseLine + "\n";
            summery += Ansi.EraseLine;

            summery += ansi.blueLight(ansi.dim("⌚ "));

            summery += "[";
            summery += ansi.green(ansi.underscore(
                    (hh > 9 ? hh + "" : "0" + hh) + ':' +
                            (mm > 9 ? mm + "" : "0" + mm) + ':' +
                            (ss > 9 ? ss + "" : "0" + ss)
            ));
            summery += "]";

            //summery += ansi.BlueLight( "   ⇔  ⇅  [  ");
            summery += ansi.blueLight("   ⇔  ⇅");
            summery += "  [  ";

            summery += ansi.yellow(Ansi.Dim + "▼ " + Ansi.BoldOff + Utils.getStringWidth(getTotalReceiveMB(), 15));
            summery += ansi.redLight(Ansi.Dim + "▲ " + Ansi.BoldOff + Utils.getStringWidth(getTotalSendMB(), 15));
            summery += ansi.redLight(Ansi.Dim + "↑ " + Ansi.BoldOff + Utils.getStringWidth(getSpeedTCPSendMB() + "/s", 15));
            summery += ansi.blue(Ansi.Dim + "↓ " + Ansi.BoldOff + Utils.getStringWidth(getSpeedTCPReceiveMB() + "/s", 15));

            //summery += ansi.BlueLight( "  ] ");
            summery += " ] ";

            summery += '\n' + Ansi.CursorUp;
            summery += Ansi.CursorUp;
            summery += Ansi.CursorUp;

            System.out.print(summery);

            demondSpeedNow();
            timer++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
