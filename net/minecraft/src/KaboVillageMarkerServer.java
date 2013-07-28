package net.minecraft.src;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public class KaboVillageMarkerServer
{
    static HashMap players = new HashMap();
    static int id = 0;
    private boolean shouldUpdateClients = false;
    private MinecraftServer mc = MinecraftServer.getServer();
    private WorldServer world;
    private int dimension;
    private String dataString;

    public KaboVillageMarkerServer(WorldServer mcWorldServer)
    {
        this.world = mcWorldServer;
        this.dimension = this.world.provider.dimensionId;
    }

    public void flagForUpdate()
    {
        this.shouldUpdateClients = true;
    }

    public boolean needsUpdate()
    {
        return this.shouldUpdateClients;
    }

    public void updateClients()
    {
        id = id >= 999 ? 0 : id + 1;
        this.dataString = this.buildDataString();
        boolean parts = true;
        ArrayList dataStringList = new ArrayList();
        String dim = this.dataString.split(":")[0];

        if (this.dataString.length() > 10000)
        {
            int var14 = (int)Math.ceil((double)this.dataString.length() / 10000.0D);

            for (int i$ = 0; i$ < var14; ++i$)
            {
                if (i$ + 1 == var14)
                {
                    dataStringList.add(id + "<" + dim + ":" + (i$ + 1) + ":" + var14 + ">" + this.dataString.substring(10000 * i$, this.dataString.length()));
                }
                else
                {
                    dataStringList.add(id + "<" + dim + ":" + (i$ + 1) + ":" + var14 + ">" + this.dataString.substring(10000 * i$, 10000 * i$ + 10000));
                }
            }
        }
        else
        {
            dataStringList.add(id + "<" + dim + ":" + "1:1>" + this.dataString);
        }

        Iterator var15 = dataStringList.iterator();

        while (var15.hasNext())
        {
            Object data = var15.next();
            Iterator playerListIter = players.keySet().iterator();

            if (playerListIter.hasNext())
            {
                String stringToSend;

                if (data instanceof String)
                {
                    stringToSend = (String)data;
                }
                else
                {
                    stringToSend = "FAILED: NOT A STRING";
                }

                try
                {
                    ByteArrayOutputStream e = new ByteArrayOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(e);

                    for (int playerName = 0; playerName < stringToSend.length(); ++playerName)
                    {
                        dataOutputStream.writeChar(stringToSend.charAt(playerName));
                    }

                    Packet250CustomPayload packet = new Packet250CustomPayload("KVM|Data", e.toByteArray());

                    while (playerListIter.hasNext())
                    {
                        String var16 = (String)playerListIter.next();
                        EntityPlayerMP player = (EntityPlayerMP)players.get(var16);

                        if (player != null && packet != null)
                        {
                            player.playerNetServerHandler.sendPacket(packet);
                        }
                        else if (player == null)
                        {
                            players.remove(var16);
                        }
                    }
                }
                catch (IOException var13)
                {
                    System.out.println(var13.getLocalizedMessage());
                    var13.printStackTrace();
                }
            }
        }

        this.shouldUpdateClients = false;
    }

    public String buildDataString()
    {
        String data = "";
        VillageCollection villageCollection = this.world.villageCollectionObj;
        List villages = villageCollection.getVillageList();
        data = data + Integer.toString(this.dimension) + ":";

        for (Iterator i$ = villages.iterator(); i$.hasNext(); data = data + ":")
        {
            Village village = (Village)i$.next();
            data = data + Integer.toString(village.getVillageRadius()) + ";" + Integer.toString(village.getCenter().posX) + "," + Integer.toString(village.getCenter().posY) + "," + Integer.toString(village.getCenter().posZ) + ";";
            List doors = village.getVillageDoorInfoList();
            VillageDoorInfo door;

            for (Iterator i$1 = doors.iterator(); i$1.hasNext(); data = data + Integer.toString(door.posX) + "," + Integer.toString(door.posY) + "," + Integer.toString(door.posZ) + ";")
            {
                door = (VillageDoorInfo)i$1.next();
            }

            data = data.substring(0, data.length() - 1);
        }

        data = data.substring(0, data.length() - 1);
        return data;
    }

    public static void addPlayerToList(String playerName, EntityPlayerMP player)
    {
        players.put(playerName, player);
    }

    public static void removePlayerFromList(String playerName)
    {
        players.remove(playerName);
    }
}
