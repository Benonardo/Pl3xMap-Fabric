package net.pl3x.map.fabric.pl3xmap.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.data.Map;

public class NetworkManager {
    private final Identifier channel = new Identifier("pl3xmap", "pl3xmap");
    private final Pl3xMap pl3xmap;

    public NetworkManager(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
    }

    public void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(this.channel, (client, handler, buf, responseSender) -> {
            ByteArrayDataInput in = in(buf.getWrittenBytes());
            int action = in.readInt();
            switch (action) {
                case Constants.GET_MAP_URL -> {
                    int response = in.readInt();
                    if (response == Constants.RESPONSE_SUCCESS) {
                        Map.MAP_URL = in.readUTF();
                    } else {
                        this.pl3xmap.enabled = false;
                    }
                }
                case Constants.GET_MAP_DATA -> {
                    int response = in.readInt();
                    switch (response) {
                        case Constants.ERROR_NO_SUCH_MAP -> System.out.println("No such map");
                        case Constants.ERROR_NO_SUCH_WORLD -> System.out.println("No such world");
                        case Constants.RESPONSE_SUCCESS -> {
                            int id = in.readInt();
                            byte scale = in.readByte();
                            int x = in.readInt();
                            int z = in.readInt();
                            int zoom = in.readInt();
                            String world = in.readUTF();
                            Map.MAPS.put(id, new Map(this.pl3xmap, scale, x, z, zoom, world));
                        }
                    }
                }
            }
        });
    }

    public void requestMapUrl() {
        ByteArrayDataOutput out = out();
        out.writeInt(Constants.GET_MAP_URL);
        send(out);
    }

    public void requestMapData(int id) {
        ByteArrayDataOutput out = out();
        out.writeInt(Constants.GET_MAP_DATA);
        out.writeInt(id);
        send(out);
    }

    private void send(ByteArrayDataOutput out) {
        ClientPlayNetworking.send(this.channel, new PacketByteBuf(Unpooled.wrappedBuffer(out.toByteArray())));
    }

    @SuppressWarnings("UnstableApiUsage")
    private ByteArrayDataOutput out() {
        return ByteStreams.newDataOutput();
    }

    @SuppressWarnings("UnstableApiUsage")
    private ByteArrayDataInput in(byte[] bytes) {
        return ByteStreams.newDataInput(bytes);
    }
}
