package sample.crypto;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import java.io.*;
import java.security.Key;


public class PEMFile {

    private PemObject pemObject;

    public PEMFile (Key key, String description) {
        this.pemObject = new PemObject(description, key.getEncoded());
    }

    public PEMFile(String filename) throws IOException {

        PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(filename)));
        try {
            this.pemObject = pemReader.readPemObject();
        } finally {
            pemReader.close();
        }
    }


    public void write(String filename) throws IOException {
        PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)));

        try {
            pemWriter.writeObject(this.pemObject);
        } finally {
            pemWriter.close();
            System.out.println(new File(filename).getPath());
        }

    }

    public PemObject getPemObject() {
        return pemObject;
    }
}
