package net.vaulk.server.config;

import net.vaulk.server.config.dto.GlobalConfig;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigManager {

    private final String path;
    private final File settingsFile;

    private GlobalConfig config;

    public ConfigManager(String path) {
        this.path = path;
        this.settingsFile = new File(path);
    }

    public void loadConfigurations() throws IOException {
        final Yaml yaml;

        if(!settingsFile.exists())
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(
                    path))
            {
                if(is != null)
                    Files.copy(is, settingsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                else throw new RuntimeException("Default '"+path+"' not found in resources!");
            }

        try(InputStream is = new FileInputStream(settingsFile)) {
            yaml = new Yaml(new Constructor(GlobalConfig.class, loadOptions()));
            this.config = yaml.load(is);
        }
    }

    public GlobalConfig getConfig() {
        return config;
    }

    private LoaderOptions loadOptions() {
        final LoaderOptions options;

        options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(50);

        return options;
    }
}