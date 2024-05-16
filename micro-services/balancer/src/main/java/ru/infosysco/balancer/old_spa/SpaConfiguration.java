package ru.infosysco.balancer.old_spa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// @Component
//@RefreshScope
//@ConfigurationProperties(prefix="spa")
@Data
public class SpaConfiguration {

    List<Item> location;
    Map<String, Item> hostItem;

    public boolean enabled;
    public void setLocation(List<Item> cfgSpa) {
        location = cfgSpa;

        this.hostItem = location.stream().collect(Collectors.toMap(Item::getHost, it-> it));
        this.enabled = location.size()>0;
    }

    Item getItem(String host) {
        Item spa =  location.stream()
                .filter(
                    it-> Objects.equals(it.getHost(), host) ||
                         (it.getHost() != null && it.getHost().contains(host))
                ).findFirst().orElse( null);

        if (spa == null) {
            spa = hostItem.get(null);
        }
        return spa;
    }
    boolean hasItem(String host) {
        return hostItem.getOrDefault(host, hostItem.get(null)) != null;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Item {
    private String path;
    private String host;

    public void setHost(String host) {
        if (host == null) return;
        this.host = host.replaceAll("https+://", "");
    }
}
