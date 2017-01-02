import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import models.Database;

/**
 * Project 3 task 3 Server side servlet
 * 
 * @author qifanj
 * 
 */
@WebServlet(urlPatterns = {"/ProcessServlet"})
public class ProcessServlet extends HttpServlet {

    // instantiate the database
    Database instance = Database.getInstance();

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // get the requested command
        String command = request.getParameter("command");
        
        if (command.equals("getTemperatures")) {
            response.getWriter().write(Database.getAll());
        } else if (command.equals("getLastTemperature")) {
            String sensorID = request.getParameter("sensorID");
            response.getWriter().write(Database.getLast(Integer.parseInt(sensorID)));
        } else {
            response.getWriter().write("No such operation");
        }
        response.setStatus(200);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.getHeader("Accept");
        String result;
        String[] attributes = new String[4];
        // get request attributes
        String signature = request.getParameter("signature");
        attributes[0] = request.getParameter("sensorID");
        attributes[1] = request.getParameter("timeStamp");
        attributes[2] = request.getParameter("type");
        attributes[3] = request.getParameter("temperature");
        
        // decrypt the signature
        byte[] decrypted = decrypt(signature, Integer.parseInt(attributes[0]));
        byte[] hashed = hashString((attributes[0] + " " + attributes[1] + " " + attributes[2] + " " + attributes[3]).getBytes());
        byte[] adjust = adjust(hashed);
        // compare with the hashed report
        if (Arrays.equals(adjust, decrypted)) {
            Database.store(attributes);
            result = "Successfully sotred the report";
        } else {
            result = "Invalid signature.";
        }
        response.getWriter().write(result);
        response.setStatus(200);
    }

   /**
     * Decrypt the signature
     *
     * @param signature
     * @param sensorID
     * @return decrypt byte array
     */
    private static byte[] decrypt(String signature, int sensorID) {
        BigInteger e, n;

        if (sensorID == 1) {
            e = new BigInteger("65537");
            n = new BigInteger("2688520255179015026237478731436571621031218154515572968727588377065598663770912513333018006654248650656250913110874836607777966867106290192618336660849980956399732967369976281500270286450313199586861977623503348237855579434471251977653662553");
        } else {
            e = new BigInteger("65537");
            n = new BigInteger("3377327302978002291107433340277921174658072226617639935915850494211665206881371542569295544217959391533224838918040006450951267452102275224765075567534720584260948941230043473303755275736138134129921285428767162606432396231528764021925639519");
        }
        BigInteger encrypted = new BigInteger(signature);
        BigInteger decrypted = encrypted.modPow(e, n);
        return decrypted.toByteArray();
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
            Logger.getLogger(ProcessServlet.class.getName()).log(Level.SEVERE, null, ex);
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

}
