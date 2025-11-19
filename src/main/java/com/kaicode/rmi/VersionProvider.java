package com.kaicode.rmi;

import picocli.CommandLine;

import java.io.InputStream;
import java.util.Properties;

/**
 * Version provider for Picocli CLI that reads version from Maven-filtered properties.
 * <p>
 * Retrieves version information from version.properties resource filtered during Maven build,
 * providing dynamic version reporting for the CLI application.
 *
 * @since 1.0.1
 */
public class VersionProvider implements picocli.CommandLine.IVersionProvider {

    private static final String VERSION = loadVersion();

    private static String loadVersion() {
        try (InputStream inputStream = VersionProvider.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return properties.getProperty("version");
            }
        } catch (Exception e) {
            // ignore
        }
        return "1.0.1";
    }

    @Override
    public String[] getVersion() throws Exception {
        return new String[]{VERSION};
    }
}
