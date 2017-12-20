package sample;

import java.io.*;
import java.util.Properties;


//delegating exception handling to this class, rather than have it in main
public class PropertiesManager extends Properties {

    PropertiesManager () {
        loadProps();
    }

    private void loadProps() {
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            super.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void storeProps(){
        OutputStream output = null;

        try {
            output = new FileOutputStream("config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            super.store(output, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
