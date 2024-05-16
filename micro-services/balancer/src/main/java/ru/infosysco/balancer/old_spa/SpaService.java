package ru.infosysco.balancer.old_spa;

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// скорей всего удалим
// @Component
public class SpaService {

    @Autowired
    SpaConfiguration cfg;
    Map<String, Resource> cache = new HashMap<>();
    public boolean isEnabled () {
        return cfg.getLocation().size()>0;
    }
    public boolean hasItem(String host) {
        return cfg.hasItem(host);
    }
    public boolean hasResource(String path, String host) {
        Item spaItem = cfg.getItem(host);
        if (cache.containsKey(path+(host!=null ? host : ""))) return true;
        File fileLink = new File(spaItem.getPath()+"/"+ path);
        return fileLink.exists();
    }

    public ModifiedInfo getModifiedInfo(String path, String host) throws IOException {
        return ModifiedInfo.builder()
                .eTag("1")
                .lastModified("222")
                .build();
    }

    public Resource getResource(String path, String host) throws IOException {
    // https://spring.io/blog/2022/08/26/creating-a-custom-spring-cloud-gateway-filter
         if (cache.containsKey(path+(host!=null ? host : ""))) return cache.get(path+(host!=null ? host : ""));
        Item spaItem = cfg.getItem(host);
        File fileLink = new File(spaItem.getPath()+"/"+ path);
        Resource resource = new FileSystemResource(fileLink);
//         resource = new CustomByteArrayResource(
//                 resource.getInputStream().readAllBytes(),
//                 fileLink.getName(),
//                 resource.lastModified()
//         );
//         cache.put(path+(host!=null ? host : ""), resource);

        return resource;
    }

}

@Data
@Builder
class ModifiedInfo{
    String eTag;
    String lastModified;

}
class CustomByteArrayResource extends ByteArrayResource {
    String fileName;
    long lastModified;
    public CustomByteArrayResource(byte[] byteArray, String name, long lastModified) {
        super(byteArray);
        fileName = name;
        this.lastModified = lastModified;
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public long lastModified() throws IOException {
        return lastModified;
    }
}
