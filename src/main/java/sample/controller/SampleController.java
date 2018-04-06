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

import com.jfoenix.controls.JFXTextField;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import sample.Main;
import sample.acbi.HTTPCommunication;
import sample.classes.IPFSFile;
import sample.crypto.CryptoUtil;
import sample.database.Contact;
import sample.database.SharedFile;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.util.*;

public class SampleController implements Initializable{

    @FXML
    private JFXTextField myId;
    @FXML
    private TextField txHash;
    @FXML
    private TextField contactHash;
    @FXML
    private ListView filesView;
    @FXML
    private Label desc;
    @FXML
    private JFXTextField latestHash;

    private PublicKey pubKey;
    private PrivateKey privKey;
    private IPFS deamon;


    @Override
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void showKey(PrivateKey priv, PublicKey pub){
        myId.setText(Contact.getContactValue("me"));
        this.pubKey = pub;
        this.privKey = priv;
        this.deamon = Main.getDeamon();

        Path path = Paths.get("shared.ser");
        if (Files.exists(path))
            populateFileView();
    }

    @FXML
    public void shareFile(ActionEvent event){
        Stage primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        PublicKey recPubKey = CryptoUtil.publicKeyFromString(HTTPCommunication.getPubKeyByHash(contactHash.getText()));
        byte [] symKey = null;
        byte [] encFile = null;
        byte [] encSymKey = null;
        try {
            symKey = CryptoUtil.generateSymKey();
            byte [] filebytes = Files.readAllBytes(file.toPath());
            encFile = CryptoUtil.encryptSym(symKey, filebytes);
            encSymKey = CryptoUtil.encryptAsym(recPubKey, symKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        NamedStreamable.ByteArrayWrapper wrapper = new NamedStreamable.ByteArrayWrapper(encFile);
        MerkleNode addResult = null;
        try {
            addResult = this.deamon.add(wrapper).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String ipfsHash = addResult.hash.toString();

        if((file != null) &  (Contact.getContactValue("me") != null) & (txHash.getText()!= null) & (ipfsHash != null) &(encSymKey != null)) {
            IPFSFile newFile = new IPFSFile(file.getName(), Contact.getContactValue("me"), txHash.getText(), FilenameUtils.getExtension(file.getAbsolutePath()), ipfsHash, encSymKey);
            String resHash = HTTPCommunication.shareIpfsFile(newFile);
            desc.setText("Last Shared File Hash");
            latestHash.setText(resHash);
        }

        Path path = Paths.get("shared.ser");
        if (Files.exists(path))
            populateFileView();
    }

    @FXML
    public void getFile(){
        String fileString = HTTPCommunication.getPubKeyByHash(txHash.getText());
        IPFSFile file = CryptoUtil.ipfsFileFromString(fileString);
        SharedFile.saveIPFSFile(file.getName(), file);

        String hash = file.getIpfshash();
        byte[] key = file.getSecretKey();
        byte[] encKey = null;
        try {
            encKey = CryptoUtil.decryptAsym(privKey, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Multihash filePointer = Multihash.fromBase58(hash);

        try {
            byte [] fileContent = deamon.cat(filePointer);
            if(encKey != null && fileContent != null)
                FileUtils.writeByteArrayToFile(new File("shared/"+file.getName()), CryptoUtil.decryptSym(encKey, fileContent));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Path path = Paths.get("shared.ser");
        if (Files.exists(path))
            populateFileView();
    }

    @FXML
    public void populateFileView(){
        filesView.getItems().clear();
        HashMap<String, IPFSFile> hmap = SharedFile.getAllIPFSFiles();
        if(hmap != null) {
            Collection<String> set = (Collection<String>) (Collection<?>) hmap.keySet();
            filesView.getItems().addAll(set);
        }
    }

    @FXML void openFile(){

        String filename = (String) filesView.getSelectionModel().getSelectedItem();

        try {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            desktop.open(new File("shared/"+filename));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        Path path = Paths.get("shared.ser");
        if (Files.exists(path))
            populateFileView();
    }
}
