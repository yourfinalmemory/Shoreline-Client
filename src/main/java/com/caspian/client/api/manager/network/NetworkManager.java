package com.caspian.client.api.manager.network;

import com.caspian.client.Caspian;
import com.caspian.client.api.event.listener.EventListener;
import com.caspian.client.impl.event.network.DisconnectEvent;
import com.caspian.client.mixin.accessor.AccessorClientWorld;
import com.caspian.client.util.Globals;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * @author linus
 * @since 1.0
 */
public class NetworkManager implements Globals
{
    //
    private static final Set<Packet<?>> PACKET_CACHE = new HashSet<>();

    public NetworkManager()
    {
        Caspian.EVENT_HANDLER.subscribe(this);
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        PACKET_CACHE.clear();
    }

    /**
     * 
     * 
     * @param p
     */
    public void sendPacket(final Packet<?> p)
    {
        PACKET_CACHE.add(p);
        mc.player.networkHandler.sendPacket(p);
    }

    /**
     *
     *
     * @param p
     */
    public void sendSequencedPacket(final SequencedPacketCreator p)
    {
        if (mc.world != null)
        {
            PendingUpdateManager updater =
                    ((AccessorClientWorld) mc.world).hookGetPendingUpdateManager().incrementSequence();
            try
            {
                int i = updater.getSequence();
                Packet<ServerPlayPacketListener> packet = p.predict(i);
                sendPacket(packet);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                if (updater != null)
                {
                    try
                    {
                        updater.close();
                    }
                    catch (Throwable e1)
                    {
                        e1.printStackTrace();
                        e.addSuppressed(e1);
                    }
                }
                throw e;
            }
            if (updater != null)
            {
                updater.close();
            }
        }
    }

    /**
     *
     *
     * @return
     */
    public int getClientLatency()
    {
        if (mc.getNetworkHandler() != null)
        {
            final PlayerListEntry playerEntry =
                    mc.getNetworkHandler().getPlayerListEntry(mc.player.getGameProfile().getId());
            if (playerEntry != null)
            {
                return playerEntry.getLatency();
            }
        }
        return 0;
    }

    /**
     *
     *
     * @param p
     * @return
     */
    public boolean isCached(Packet<?> p)
    {
        return PACKET_CACHE.contains(p);
    }
}
