package sample.database;

import sample.classes.IPFSFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public abstract class SharedFile{

    private static final String FILEPATH = "shared.ser";

    public static void saveIPFSFile(String name, IPFSFile file){

        Path path = Paths.get(FILEPATH);
        HashMap<String, IPFSFile> hmap;

        if (Files.exists(path))
            hmap = getAllIPFSFiles();
        else
            hmap = new HashMap<>();

        hmap.put(name, file);

        try {
            FileOutputStream fos = new FileOutputStream(FILEPATH);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hmap);
            oos.close();
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public static HashMap<String, IPFSFile> getAllIPFSFiles(){

        HashMap<String, IPFSFile> hmap = null;
        try
        {
            FileInputStream fis = new FileInputStream(FILEPATH);
            ObjectInputStream ois = new ObjectInputStream(fis);
            hmap = (HashMap<String,IPFSFile>) ois.readObject();
            ois.close();
            fis.close();
        }catch(IOException e){
            e.printStackTrace();
            return hmap;
        }catch(ClassNotFoundException c){
            c.printStackTrace();
            return hmap;
        }
        return hmap;
    }

    public static IPFSFile getIPFSFile(String name){
        HashMap<String, IPFSFile> hmap = getAllIPFSFiles();
        return hmap.get(name);
    }
}
