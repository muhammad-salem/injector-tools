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
        while (showSpeed ) {
        	hh = ((timer/60)/60)%60 ;
			mm = (timer/60)%60;
			ss = timer%60;
			
            summery = "";
            summery += Ansi.EraseLine + "\n" ;
            summery += Ansi.EraseLine + "\n" ;
            summery += Ansi.EraseLine ;//+ ANSI.SaveCursor;
            
//            summery += "\r ";
            
            summery += ansi.BlueLight( Ansi.Bright + "⌚ ");
            
//            summery += ANSI.Green + ANSI.Underscore;
//            summery +=  (hh != 0 ? hh+"" : "00") + ':' ;
//            summery +=  (mm != 0 ? mm+"" : "00") + ':' +  ss  ;
//            summery +=  ANSI.ResetAllAttributes;

	    summery += "[";
            summery += Ansi.Green + Ansi.Underscore;
            summery +=  (hh > 9 ? hh+"" : "0"+hh ) + ':' ;
            summery +=  (mm > 9 ? mm+"" : "0"+mm ) + ':' ;
            summery +=  (ss > 9 ? ss+"" : "0"+ss )  ;
            summery +=  Ansi.ResetAllAttributes;
            summery += "]";
            
            //summery += ansi.BlueLight( "   ⇔  ⇅  [  ");
            summery += ansi.BlueLight( "   ⇔  ⇅");
            summery += "  [  ";
            
            summery += ansi.Yellow(Ansi.Bright +"▼ "+Ansi.BoldOff + Utils.getStringWidth( getTotalReceiveMB(), 15));
            summery += ansi.RedLight(Ansi.Bright+"▲ "+Ansi.BoldOff  + Utils.getStringWidth(getTotalSendMB()  , 15));
            summery += ansi.RedLight(Ansi.Bright + "↑ "+Ansi.BoldOff  + Utils.getStringWidth( getSpeedTCPSendMB() + "/s", 15));
            summery += ansi.Blue(Ansi.Bright + "↓ "+Ansi.BoldOff  +Utils.getStringWidth( getSpeedTCPReceiveMB()   + "/s", 15));
            
            //summery += ansi.BlueLight( "  ] ");
            summery += " ] ";
            
            summery += '\n' + Ansi.CursorUp;
            
//            summery += ANSI.UnsaveCursor;
            summery += Ansi.CursorUp;
//            summery += ANSI.EraseLine;
            summery += Ansi.CursorUp;
//            summery += ANSI.EraseLine;

            System.out.print(summery);

            demondSpeedNow();
            timer++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
