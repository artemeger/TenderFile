package sample.database;

import java.io.*;
import java.util.HashMap;

public abstract class Contact {

    public static void saveContact(String name, String value){

        HashMap<String, String> hmap = new HashMap<>();

        hmap.put(name, value);
        try {
            FileOutputStream fos = new FileOutputStream("contacts.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hmap);
            oos.close();
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public static HashMap<String, String> getContacts(){

        HashMap<String, String> hmap = null;
        try
        {
            FileInputStream fis = new FileInputStream("contacts.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            hmap = (HashMap<String,String>) ois.readObject();
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

    public static String getContactValue(String name){
        HashMap<String, String> hmap = getContacts();
        return hmap.get(name);
    }

}
