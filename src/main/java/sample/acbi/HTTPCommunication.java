package sample.acbi;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sample.crypto.CryptoUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.PublicKey;

public abstract class HTTPCommunication {

    private static String SERVER = "http://192.168.178.32:46657/";

    public static String getPubKeyByHash(String hash){

        JSONObject json = null;
        try {
            json = new JSONObject(IOUtils.toString(new URL(SERVER+"tx?hash=0x"+hash), Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(json != null)
            return json.getJSONObject("result").get("tx").toString();
        else
            return "Error";
    }

    public static String registerUser(PublicKey pubKey){

        JSONObject json = null;

        String test = SERVER+"broadcast_tx_commit?tx="+"\""+CryptoUtil.publicKeyToString(pubKey)+"\"";
        System.out.println(test);
        try {
            json = new JSONObject(IOUtils.toString(new URL(test), Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(json != null){
            return json.getJSONObject("result").get("hash").toString();}
        else
            return null;
    }
}
