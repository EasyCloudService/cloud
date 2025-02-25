package dev.easycloud.service.security;

import dev.httpmarco.netline.NetChannel;
import dev.httpmarco.netline.security.SecurityHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NetLineSecurity implements SecurityHandler {
    private final String privateKey;

    @Override
    public void detectUnauthorizedAccess(NetChannel netChannel) {
        System.out.println("Unauthorized access from " + netChannel.hostname() + ".");
    }

    @Override
    public boolean authenticate(NetChannel netChannel) {
        return netChannel.id().startsWith(this.privateKey + "-");
    }
}
