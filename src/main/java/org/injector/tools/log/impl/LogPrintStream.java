package org.injector.tools.log.impl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.terminal.ansi.Ansi;


public class LogPrintStream extends PrintStream {

	PrintStream printStream;
	public LogPrintStream(OutputStream out) {
		super(out);
		printStream = System.out;
	}

	public LogPrintStream(String fileName) throws FileNotFoundException {
		super(fileName);
		printStream = System.out;
	}
	
	public LogPrintStream(String fileName, PrintStream out) throws FileNotFoundException {
		super(fileName);
		this.printStream = out;
	}

	public LogPrintStream(File file) throws FileNotFoundException {
		super(file);
		printStream = System.out;
	}

	public LogPrintStream(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
		printStream = System.out;
	}

	public LogPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
		printStream = System.out;
	}

	public LogPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
		printStream = System.out;
	}

	public LogPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);
		printStream = System.out;
	}
	
	//******************************************//

	//const pattern = [
	//'[\\u001B\\u009B][[\\]()#;?]*(?:(?:(?:[a-zA-Z\\d]*(?:;[a-zA-Z\\d]*)*)?\\u0007)',
	//'(?:(?:\\d{1,4}(?:;\\d{0,4})*)?[\\dA-PRZcf-ntqry=><~]))'
	//].join('|');
	
	@Override
	public void println(String x) {
        synchronized (printStream){
            printStream.print(Ansi.EraseLine );
            printStream.println(x);
        }
        //x = x.replaceAll("[\\u001B\\u009B][[\\]()#;?]*(?:(?:(?:[a-zA-Z\\d]*(?:;[a-zA-Z\\d]*)*)?\\u0007)", "");
        x = x.replaceAll("\u001B\\[[;\\d]*m", "");
		super.println(x);
	}

}
