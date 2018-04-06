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
package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.acbi.Communication;
import sample.acbi.HTTPCommunication;
import sample.crypto.CryptoUtil;
import sample.database.Contact;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;


public class MainController {

    private static PublicKey pub;
    private static PrivateKey priv;

    @FXML
    public void Login(ActionEvent event){

        Stage primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Parent window = null;

        KeyPair keys = CryptoUtil.loadAsymKeypair();
        pub = keys.getPublic();
        priv = keys.getPrivate();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
            window = loader.load();
            SampleController con = loader.getController();
            con.showKey(priv, pub);
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setScene(new Scene(window));

    }

    @FXML
    public void Generate(){

        KeyPair keys = null;

        try {
            keys = CryptoUtil.generateAsymKeypair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(keys != null) {
            CryptoUtil.saveAsymKeypair(keys);
            String hash = HTTPCommunication.registerUser(keys.getPublic());
            Contact.saveContact("me", hash);
        }
    }

}
