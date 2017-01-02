package project3task3client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Project 3 task 3 client
 * @author qifanj
 * @reference https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
 */
public class Project3Task3Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        long t;// to store the current time
        String report;// sensor report in the format of: sensorID + " " + time + " " + type + " " + temperature

        // first call
        t = System.currentTimeMillis();
        report = "1 " + t + " Celsius 78.3";
        String signature = encrypt(report, 1);
        System.out.println("Reporting high temperature sensed at sensor 1...(" + report + ")");
        sendPost(report, signature);

        // second call
        t = System.currentTimeMillis();
        report = "2 " + t + " Celsius 25.6";
        signature = encrypt(report, 2);
        System.out.println("Reporting low temperature sensed at sensor 2...(" + report + ")");
        sendPost(report, signature);

        // third call
        t = System.currentTimeMillis();
        report = "1 " + t + " Fahrenheit 181.1";
        signature = encrypt(report, 1);
        System.out.println("Reporting high temperature sensed at sensor 1...(" + report + ")");
        sendPost(report, signature);

        // forth call, invalid signature
        t = System.currentTimeMillis();
        report = "1 " + t + " Fahrenheit 167.0";
        // use sensor 2's d and n to generate a invalid signature for sensor 1
        String InvalidSignature = encrypt(report, 2);
        System.out.println("Reporting high temperature sensed at sensor 1...(unauthenrized)(" + report + ")");
        sendPost(report, InvalidSignature);

        // fifth call
        System.out.println("Retrieving temperature reports from server...");
        sendGet("getTemperatures", -1);

        // sixth call
        System.out.println("Retrieving last temperature report at sensor 1 from server...");
        sendGet("getLastTemperature", 1);
    }

    /**
     * Encrypt the report with sensor's d and n
     *
     * @param s the report
     * @param sensorID
     * @return the generated signature
     */
    private static String encrypt(String s, int sensorID) {
        BigInteger d, n;
        if (sensorID == 1) {
            d = new BigInteger("339177647280468990599683753475404338964037287357290649639740920420195763493261892674937712727426153831055473238029100340967145378283022484846784794546119352371446685199413453480215164979267671668216248690393620864946715883011485526549108913");
            n = new BigInteger("2688520255179015026237478731436571621031218154515572968727588377065598663770912513333018006654248650656250913110874836607777966867106290192618336660849980956399732967369976281500270286450313199586861977623503348237855579434471251977653662553");
        } else {
            d = new BigInteger("3056791181023637973993616177812006199813736824485077865613630525735894915491742310306893873634385114173311225263612601468357849028784296549037885481727436873247487416385339479139844441975358720061511138956514526329810536684170025186041253009");
            n = new BigInteger("3377327302978002291107433340277921174658072226617639935915850494211665206881371542569295544217959391533224838918040006450951267452102275224765075567534720584260948941230043473303755275736138134129921285428767162606432396231528764021925639519");
        }
        // hash the string to be signed
        byte[] hashed = hashString(s.getBytes());
        //adjust first byte to 0(if not)
        byte[] adjust = adjust(hashed);
        BigInteger source = new BigInteger(adjust);
        BigInteger encrypted = source.modPow(d, n);
        return encrypted.toString();
    }

    /**
     * Hash the array by SHA-1 algorithm
     *
     * @param array the input array
     * @return hashed array
     */
    private static byte[] hashString(byte[] array) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(array);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Add one '0' to head if the byte array starts with 1
     *
     * @param hashed the hashed array
     * @return the adjusted array
     */
    private static byte[] adjust(byte[] hashed) {
        byte[] adjust;
        if (hashed[0] < 0) {
            adjust = new byte[hashed.length + 1];
            System.arraycopy(hashed, 0, adjust, 1, hashed.length);
        } else {
            adjust = hashed;
        }
        return adjust;
    }

    /**
     * send post request to server
     * 
     * @param report the report to be send
     * @param signature signature for the report
     */
    private static void sendPost(String report, String signature) {
        try {
            String[] attributes = report.split(" ");
            
            URL url = new URL("http://localhost:8080/Project3Task3Server/ProcessServlet?sensorID=" + attributes[0] + "&timeStamp=" + attributes[1] + "&type=" + attributes[2] + "&temperature=" + attributes[3] + "&signature=" + signature);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "text/xml");
            
            int code = con.getResponseCode();
            // if connected, read the response
            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * send get request to the server
     * @param command getTemperatures or getLastTemperature
     * @param id sensorID
     */
    private static void sendGet(String command, int id) {
        try {
            
            URL url;
            // different url for different command
            if(id < 0) {
                url = new URL("http://localhost:8080/Project3Task3Server/ProcessServlet?command=" + command);
            } else {
                url = new URL("http://localhost:8080/Project3Task3Server/ProcessServlet?command=" + command + "&sensorID=" + id);
            }
            
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "text/xml");
            
            int code = con.getResponseCode();
            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Project3Task3Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
