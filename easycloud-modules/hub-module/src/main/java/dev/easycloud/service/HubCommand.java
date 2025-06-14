package dev.easycloud.service;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public final class HubCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player player) {
            var lobbyServer = HubModuleVelocity.instance().server().getAllServers().stream()
                    .filter(it -> it.getServerInfo().getName().toLowerCase().startsWith("lobby"))
                    .findFirst()
                    .orElse(null);
            if(lobbyServer == null) {
                invocation.source().sendMessage(Component.text("No lobby server found."));
                return;
            }

            player.createConnectionRequest(lobbyServer).fireAndForget();
        } else {
            invocation.source().sendMessage(Component.text("This command can only be used by players."));
        }
    }
}
