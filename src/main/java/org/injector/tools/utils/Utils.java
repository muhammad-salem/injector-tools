package org.injector.tools.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.injector.tools.log.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Enumeration;

public class Utils {

    /**
     * concate " " to string
     *
     * @param str   the string to concate with " " wigth space
     * @param count the new length
     * @return null if given string length greater than count
     */
    public static String getStringWidth(String str, int count) {
        if (str.length() > count) return null;
        for (int i = str.length(); i < count; i++) {
            str += " ";
        }
        return str;
    }

    public static String getStringMiddle(String str, int count, char ch) {
        if (str.length() > count) return str;
        int apend = (count - str.length()) / 2;
        boolean reminder = ((count - str.length()) % 2) == 1;
        if (reminder) str = ch + str;
        for (int i = 0; i < apend; i++) {
            str = ch + str + ch;
        }
        return str;
    }

    public static String getStringMiddle(String str, int count) {

        return getStringMiddle(str, count, ' ');
    }

    public static String getStringRight(String str, int count) {
        if (str.length() > count) return null;
        for (int i = str.length(); i < count; i++) {
            str = " " + str;
        }
        return str;
    }

    public static String GetWidthBracts(String title, int l) {
        title = getStringWidth(title, l);
        title = '[' + title + ']';
        return title;
    }

    public static String GetWidthBractsRight(String title, int l) {
        title = getStringRight(title, l);
        title = '[' + title + ']';
        return title;
    }

    public static String GetWidthBractsMiddle(String title, int l) {
        title = getStringMiddle(title, l);
        title = '[' + title + ']';
        return title;
    }


    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder()
                .setPrettyPrinting();
//				.excludeFieldsWithoutExposeAnnotation();

        return builder.create();
    }

    public static String toJson(Object object) {
        Gson gson = getGson();
        return gson.toJson(object);
    }

    public static boolean toJsonFile(String file, Object object) {
        return writeJson(file, toJson(object));
    }

    public static boolean writeJson(String filename, String json) {
        try {
            File file = new File(filename);
            FileUtils.write(file, json, StandardCharsets.UTF_8);
//			if(!file.getParentFile().exists()) {
//				FileUtils.forceMkdir(new File(filename).getParentFile());
//			}
//			FileWriter writer = new FileWriter(filename);
//			writer.write(json);
//			writer.close();
        } catch (Exception e) {
            return false;
        }
        // System.out.println(json);
        return true;
    }

    public static <T> T fromJson(File file, Class<T> classT) {
        return fromJson(file.getPath(), classT);
    }

    /**
     * @param <T>  the type of the desired object
     * @param file file path to read from.
     * @throws FileNotFoundException
     */
    public static <T> T fromJson(Class<T> classT, String file) throws FileNotFoundException {
        Gson gson = getGson();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return gson.fromJson(reader, classT);
    }

    /**
     * @param <T>  the type of the desired object
     * @param file file path to read from.
     */
    public static <T> T fromJson(String file, Class<T> classT) {
        Gson gson = getGson();
        T t = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            t = gson.fromJson(reader, classT);
            // System.out.println(url);
        } catch (Exception e) {
            System.err.println("no file found " + file);
            return null;
        }
        return t;
    }

    /***
     * return true only if it is a true URL
     *
     * @param fileURL
     *            {@link String} that represent that URL to test
     * @return {@link Boolean}
     */
    public static boolean verifyURL(String fileURL) {

        String str = fileURL.toLowerCase();
        // Allow FTP, HTTP and HTTPS URLs.
        if (str.startsWith("http://") || str.startsWith("https://") || str.startsWith("ftp://")) {
            // Verify format of URL.
            // URL verifiedUrl = null;
            try {
                /* verifiedUrl = */
                new URL(fileURL);
            } catch (MalformedURLException e) {
                System.err.println("not valid url");
                return false;
            }

            /*
             * // Make sure URL specifies a file. if (verifiedUrl.getFile().length() < 9) {
             * return false; }
             */
            // System.out.println(verifiedUrl.toString() + " >>");
        } else {
            return false;
        }
        return true;
    }

    public static String getStringFromInputStream(InputStream stream) {
        String string = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        try {
            String str = "";
            while ((str = reader.readLine()) != null) {
                string += str + "\n";
            }

            reader.close();
        } catch (IOException e) {
            return null;
        }
        return string;
    }

    /**
     * @param length file path to read from.
     */
    public static String sizeLengthFormate0_0(double length) {
        String hrSize = "";
        DecimalFormat dec = new DecimalFormat("0.0");

        double b = length;
        double k = length / 1024.0;
        double m = ((length / 1024.0) / 1024.0);
        double g = (((length / 1024.0) / 1024.0) / 1024.0);
        double t = ((((length / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        if (t >= 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g >= 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m >= 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k >= 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }

    /**
     * @param length file path to read from.
     */
    public static String getLengthFor(double length) {
        return fileLengthUnite(length);
    }

    /**
     * @param length file path to read from.
     */
    public static String fileLengthUnite(double length) {
        String hrSize = "";
        DecimalFormat dec = new DecimalFormat("0.0");

        double b = length;
        double k = b / 1024;
        double m = k / 1024;
        double g = m / 1024;
        double t = g / 1024;
        /*
		double b = length;
		double k = length / 1024.0;
		double m = ((length / 1024.0) / 1024.0);
		double g = (((length / 1024.0) / 1024.0) / 1024.0);
		double t = ((((length / 1024.0) / 1024.0) / 1024.0) / 1024.0);
		*/

        if (t >= 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g >= 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m >= 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k >= 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }

    public static double fileLength(long length) {
        double k = length / 1024.0;
        double m = ((length / 1024.0) / 1024.0);
        double g = (((length / 1024.0) / 1024.0) / 1024.0);
        double t = ((((length / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        if (t > 1) {
            return t;
        } else if (g > 1) {
            return g;
        } else if (m > 1) {
            return m;
        } else if (k > 1) {
            return k;
        }
        return length;
    }

    public static int gesslChunkesNum(long length) {
        // double k = length / 1024.0;

        if (length <= 0) {
            return 1;
        }
        double m = ((length / 1024.0) / 1024.0);
        double g = (((length / 1024.0) / 1024.0) / 1024.0);
        double t = ((((length / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        if (t > 1) {
            return 32;
        } else if (g > 1) {
            return 16;
        } else if (m >= 700) {
            return 10;
        } else if (m < 700 && m >= 500) {
            return 8;
        } else if (m < 500 && m >= 250) {
            return 6;
        } else if (m < 250 && m >= 200) {
            return 5;
        } else if (m < 200 && m >= 100) {
            return 4;
        } else if (m < 100 && m >= 10) {
            return 3;
        } else if (m < 10 && m >= 4) {
            return 2;
        }
        /*
         * else if (m < 5 && m >= 1 ) { return 1; } else if (k > 1) { return 1; }
         */

        return 1;
    }

    public static String percent(long downloaded, long size) {
        String hrSize = "(";
        DecimalFormat dec = new DecimalFormat("0.000");
        hrSize += dec.format(100 * ((float) downloaded / size)).concat(" %)");
        return hrSize;
    }

    public static double percent(double downloaded, double size) {
        return downloaded / size;
    }

    public static String fileLengthUnite(double progress, final long length) {
        return fileLengthUnite(progress * length);
    }

    public static String getTime(long date) {
        long rmnd = 0;
        long ss = date % 60;
        rmnd = date / 60; // minute
        long mm = rmnd % 60;
        rmnd /= 60; // hours
        long hh = rmnd % 60;
        rmnd /= 60; // days
        long dd = rmnd % 24;
        return dd + ":" + hh + ":" + mm + ":" + ss;
    }

    public static boolean isAnyNetWorkInterfaceUp() {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface networkInterface = networks.nextElement();
                if (networkInterface.isUp()) {
                    if (networkInterface.isLoopback()) {
                        continue;
                    }
                    if (networkInterface.getName().contains("pan")) {
                        continue;
                    }
                    // System.out.println(networkInterface);
                    return true;
                }
            }
        } catch (SocketException e) {
            Logger.debug("Utils", "Can't get Nerwork Interfaces");
        }
        return false;
    }

    public static <T> InputStream getResourceAsStream(Class<T> class1, String res) {
        return class1.getResourceAsStream(res);
    }

    public static <T> URL getResourcem(Class<T> class1, String res) {
        return class1.getResource(res);
    }

    public static void Copy(URL source, File destination) {
        try {
            FileUtils.copyURLToFile(source, destination);

        } catch (IOException e) {
            Logger.debug("Utils", "falied to copy file: [" + source + "]");
        }
    }
}
