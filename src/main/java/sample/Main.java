/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sample;

import com.google.common.io.BaseEncoding;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.acbi.Communication;
import sample.acbi.HTTPCommunication;
import sample.crypto.CryptoUtil;
import sample.ipfs.IPFSDaemon;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import static java.lang.Thread.sleep;

public class Main extends Application {

    private static IPFS ipfs;
    private static Communication com;
    private static PublicKey pub;
    private static PrivateKey priv;

    public static Communication getComunnication(){return com;}
    public static IPFS getDeamon(){return ipfs;}


    @Override
    public void start(Stage primaryStage) throws Exception{


        IPFSDaemon daemon = new IPFSDaemon();
        daemon.binaries(); // Download, extract, load binaries
        daemon.start(); // Init and start the daemon
        daemon.attach(); // Attach the API
        ipfs = daemon.getIPFS(); // Get the API object

        //Open Websocket
        //com = new Communication();
        //while(!com.connected()) sleep(2000);

        //NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File("Path to File"));
        //MerkleNode addResult = ipfs.add(file).get(0);
        //System.out.println(addResult.hash.toString());

        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("Application");
        primaryStage.setScene(new Scene(root, 350, 500));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
