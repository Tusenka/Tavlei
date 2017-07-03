package entity;


import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Extend DataConfiguration, add possibility to loading color by name
 * Created by Irina on 24.08.2016.
 */
public class MyAppConfigurationLoader extends Properties {

    public static class PropertiesException extends IOException {
        PropertiesException(String s) {
            super(s);
        }
    }

    public MyAppConfigurationLoader(Properties properties) {
        super(properties);
    }

    public MyAppConfigurationLoader() {
        super();
    }

    @SuppressWarnings("SameParameterValue")
    public MyAppConfigurationLoader(String resourceName) throws PropertiesException {
        super();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream is = loader.getResourceAsStream(resourceName);
            load(is);
        } catch (IOException e) {
            throw new PropertiesException("Failed load from " + resourceName + " recourse");
        }
    }


    public Color getFXColor(String key) {
        return Color.valueOf(this.getString(key));

    }

    public String getString(String key) {
        return super.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(super.getProperty(key));
    }

}
