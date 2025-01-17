package handling.channel;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;
import constants.WorldConstants;
import handling.MapleServerHandler;
import handling.login.LoginServer;
import handling.mina.MapleCodecFactory;
import handling.world.CheaterData;
import handling.world.World;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import scripting.EventScriptManager;
import server.MapleSquad;
import server.MapleSquad.MapleSquadType;
import server.events.*;
import server.life.PlayerNPC;
import server.maps.AramiaFireWorks;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.shops.HiredMerchant;
import tools.CollectionUtil;
import tools.ConcurrentEnumMap;
import tools.packet.CWvsContext;

public class ChannelServer {

    public static long serverStartTime;

    public static final ArrayList<ChannelServer> getAllInstances() {
        return new ArrayList<>(instances.values());
    }
    private int world, channel, running_MerchantID = 0;
    public int eventChannel, eventMap = 0;
    private String serverMessage, ip;
    private EventScriptManager eventSM;
    private boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false;
    public boolean eventOn = false, eventClosed = false;
    private PlayerStorage players = new PlayerStorage();
    private IoAcceptor acceptor;
    private final MapleMapFactory mapFactory;
    private AramiaFireWorks works = new AramiaFireWorks();
    private static final Map<Integer, ChannelServer> instances = new HashMap<>();
    private final Map<MapleSquadType, MapleSquad> mapleSquads = new ConcurrentEnumMap<>(MapleSquadType.class);
    private final Map<Integer, HiredMerchant> merchants = new HashMap<>();
    private final List<PlayerNPC> playerNPCs = new LinkedList<>();
    private final ReentrantReadWriteLock merchLock = new ReentrantReadWriteLock(); //merchant
    private final Map<MapleEventType, MapleEvent> events = new EnumMap<>(MapleEventType.class);
    private int port = ServerConstants.ChannelPort;

    private ChannelServer(final int world, final int channel) {
        this.world = world;
        this.channel = channel;
        setChannel(channel);
        mapFactory = new MapleMapFactory(world, channel);
    }

    public static Set<Integer> getAllInstance() {
        return new HashSet<>(instances.keySet());
    }

    public final void init() {
        setChannel(channel);
        serverMessage = ServerConstants.serverMessage;
        eventSM = new EventScriptManager(this, WorldConstants.Events.split(","));
        port = ServerConstants.ChannelPort + this.channel - 1;
        port += (world * 100);
        ip = ServerConstants.SERVER_IP + ":" + port;

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
        ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
        loadEvents();
        try {
            acceptor.setHandler(new MapleServerHandler(world, channel, false));
            acceptor.bind(new InetSocketAddress(port));
            eventSM.init();
        } catch (IOException e) {
            System.out.println("端口  " + port + " 已被使用 (頻道: " + getChannel() + ")" + e);
        }

    }

    public final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        broadcastPacket(CWvsContext.serverNotice(0, "此頻道將被關閉"));
        shutdown = true;
        System.out.println("頻道 " + channel + ", 儲存角色中...");
        players.disconnectAll();
        System.out.println("頻道 " + channel + ", 解除占用端口...");
        acceptor.unbind();
        acceptor = null;
        instances.remove(channel);
        setFinishShutdown();
    }

    public final void unbind() {
        acceptor.unbind();
    }

    public final boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public final MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public static ChannelServer newInstance(final int world, final int channel) {
        return new ChannelServer(world, channel);
    }

    public static ChannelServer getInstance(int world, int channel) {
        return LoginServer.getInstance().getChannel(world, channel);
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public void addPlayer(MapleCharacter chr) {
        players.addPlayer(chr);
        chr.announce(CWvsContext.serverMessage(serverMessage));
    }

    public final void removePlayer(final MapleCharacter chr) {
        players.removePlayer(chr.getId());
    }

    // TODO: Multi-World serverMessage
    public final String getServerMessage() {
        return serverMessage;
    }

    public final void setServerMessage(final String newMessage) {
        serverMessage = newMessage;
        broadcastPacket(CWvsContext.serverMessage(serverMessage));
    }

    public final void broadcastPacket(final byte[] data) {
        players.getAllCharacters().forEach((chr) -> {
            chr.announce(data);
        });
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public final void reloadEvents() {
        eventSM.cancel();
        eventSM = new EventScriptManager(this, WorldConstants.Events.split(","));
        eventSM.init();
    }

    public final void broadcastSmegaPacket(final byte[] data) {
        players.getAllCharacters().stream().filter((chr) -> (chr.getSmega())).forEachOrdered((chr) -> {
            chr.announce(data);
        });
    }

    public final void broadcastGMPacket(final byte[] data) {
        players.getAllCharacters().stream().filter((chr) -> (chr.isGM())).forEachOrdered((chr) -> {
            chr.announce(data);
        });
    }

    public final int getChannel() {
        return channel;
    }

    public final int getWorld() {
        return world;
    }

    public final void setChannel(final int channel) {
        instances.put(channel, this);
        LoginServer.addChannel(channel);
    }

    public final String getIP() {
        return ip;
    }

    public final boolean isShutdown() {
        return shutdown;
    }

    public final int getLoadedMaps() {
        for (World worlds : LoginServer.getWorlds()) { // forloop worlds
            for (ChannelServer cs : worlds.getChannels()) {// forloop channels
                return cs.getMapFactory().getLoadedMaps();
            }
        }
        return mapFactory.getLoadedMaps();
    }

    public final void loadEvents() {
        if (!events.isEmpty()) {
            return;
        }
        events.put(MapleEventType.CokePlay, new MapleCoconut(world, channel, MapleEventType.CokePlay));
        events.put(MapleEventType.Coconut, new MapleCoconut(world, channel, MapleEventType.Coconut));
        events.put(MapleEventType.Fitness, new MapleFitness(world, channel, MapleEventType.Fitness));
        events.put(MapleEventType.OlaOla, new MapleOla(world, channel, MapleEventType.OlaOla));
        events.put(MapleEventType.OxQuiz, new MapleOxQuiz(world, channel, MapleEventType.OxQuiz));
        events.put(MapleEventType.Snowball, new MapleSnowball(world, channel, MapleEventType.Snowball));
        events.put(MapleEventType.Survival, new MapleSurvival(world, channel, MapleEventType.Survival));
    }

    public Map<MapleSquadType, MapleSquad> getAllSquads() {
        return Collections.unmodifiableMap(mapleSquads);
    }

    public final MapleSquad getMapleSquad(final String type) {
        return getMapleSquad(MapleSquadType.valueOf(type.toLowerCase()));
    }

    public final MapleSquad getMapleSquad(final MapleSquadType type) {
        return mapleSquads.get(type);
    }

    public final boolean addMapleSquad(final MapleSquad squad, final String type) {
        final MapleSquadType types = MapleSquadType.valueOf(type.toLowerCase());
        if (types != null && !mapleSquads.containsKey(types)) {
            mapleSquads.put(types, squad);
            squad.scheduleRemoval();
            return true;
        }
        return false;
    }

    public void saveWorlds() {
        players.getAllCharacters().forEach((chr) -> {
            chr.savePlayer();
        });
    }

    public void blueWorldMessage(String msg) {
        players.getAllCharacters().forEach((chr) -> {
            chr.dropMessage(6, msg);
        });
    }

    public void yellowWorldMessage(String msg) {
        getPlayerStorage().getAllCharacters().forEach((mc) -> {
            mc.getClient().getSession().write(CWvsContext.yellowChat(msg));
        });
    }

    public final boolean removeMapleSquad(final MapleSquadType types) {
        if (types != null && mapleSquads.containsKey(types)) {
            mapleSquads.remove(types);
            return true;
        }
        return false;
    }

    public final int closeAllMerchant() {
        int ret = 0;
        merchLock.writeLock().lock();
        try {
            final Iterator<Entry<Integer, HiredMerchant>> merchants_ = merchants.entrySet().iterator();
            while (merchants_.hasNext()) {
                HiredMerchant hm = merchants_.next().getValue();
                hm.closeShop(true, false);
                hm.getMap().removeMapObject(hm);
                merchants_.remove();
                ret++;
            }
        } finally {
            merchLock.writeLock().unlock();
        }

        for (World worlds : LoginServer.getWorlds()) { // forloop every world
            for (ChannelServer channels : worlds.getChannels()) { // forloop every channel of every world
                for (int i = 910000001; i <= 910000022; i++) { // forloop every fm map of every channel of every world
                    for (MapleMapObject mmo : channels.getMapFactory().getMap(i).getAllHiredMerchantsThreadsafe()) { // get ALL merchants
                        ((HiredMerchant) mmo).closeShop(true, false);
                        ret++;
                    }
                }
            }
        }

        return ret;
    }

    public final int addMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();
        try {
            running_MerchantID++;
            merchants.put(running_MerchantID, hMerchant);
            return running_MerchantID;
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public final void removeMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();
        try {
            merchants.remove(hMerchant.getStoreId());
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public final boolean containsMerchant(final int accid, int cid) {
        boolean contains = false;
        merchLock.readLock().lock();
        try {
            final Iterator itr = merchants.values().iterator();
            while (itr.hasNext()) {
                HiredMerchant hm = (HiredMerchant) itr.next();
                if (hm.getOwnerAccId() == accid || hm.getOwnerId() == cid) {
                    contains = true;
                    break;
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return contains;
    }

    public final List<HiredMerchant> searchMerchant(final int itemSearch) {
        final List<HiredMerchant> list = new LinkedList<>();
        merchLock.readLock().lock();
        try {
            final Iterator itr = merchants.values().iterator();
            while (itr.hasNext()) {
                HiredMerchant hm = (HiredMerchant) itr.next();
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return list;
    }

    public final void toggleMegaphoneMuteState() {
        this.MegaphoneMuteState = !this.MegaphoneMuteState;
    }

    public final boolean getMegaphoneMuteState() {
        return MegaphoneMuteState;
    }

    public int getEvent() {
        return eventMap;
    }

    public final void setEvent(final int ze) {
        this.eventMap = ze;
    }

    public MapleEvent getEvent(final MapleEventType t) {
        return events.get(t);
    }

    public final Collection<PlayerNPC> getAllPlayerNPC() {
        return playerNPCs;
    }

    public final void addPlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            return;
        }
        playerNPCs.add(npc);
        LoginServer.getWorlds().forEach((worlds) -> {
            // forloop worlds
            worlds.getChannels().forEach((cs) -> {
                // forloop channels
                cs.getMapFactory().getMap(npc.getMapId()).addMapObject(npc); // add to every world and channel
            });
        });
    }

    public final void removePlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            playerNPCs.remove(npc);
            LoginServer.getWorlds().forEach((worlds) -> {
                // forloop worlds
                worlds.getChannels().forEach((cs) -> {
                    // forloop channels
                    cs.getMapFactory().getMap(npc.getMapId()).removeMapObject(npc); // add to every world and channel
                });
            });
        }
    }

    public final int getPort() {
        return port;
    }

    public final void setShutdown() {
        this.shutdown = true;
        System.out.println("頻道 " + channel + " 正在儲存精靈商人中...");
    }

    public final void setFinishShutdown() {
        this.finishedShutdown = true;
        System.out.println("頻道 " + channel + " 已經完成關閉.");
    }

    public static int getChannelCount() { // needs to be fixed for multi-world
        return instances.size();
    }

    public static Map<Integer, Integer> getChannelLoad() { // needs to be fixed for multi-world
        Map<Integer, Integer> ret = new HashMap<>();
        instances.values().forEach((cs) -> {
            ret.put(cs.getChannel(), cs.getConnectedClients());
        });
        return ret;
    }

    public int getConnectedClients() {
        return players.getAllCharacters().size();
    }

    public void broadcastMessage(byte[] message) {
        broadcastPacket(message);
    }

    public void broadcastSmega(byte[] message) {
        broadcastSmegaPacket(message);
    }

    public void broadcastGMMessage(byte[] message) {
        broadcastGMPacket(message);
    }

    public AramiaFireWorks getFireWorks() {
        return works;
    }

    public int getPvpMaxD() {
        return ServerConstants.pvpmaxdamage;
    }

    public int HellChis() {
        return ServerConstants.HellCh;
    }

    public int PvPis() {
        return ServerConstants.pvpch;
    }

    public static void forceRemovePlayerByCharName(MapleClient client, String Name) {
        client.getWorldServer().getChannels().forEach((ch) -> {
            Collection<MapleCharacter> chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
            for (MapleCharacter c : chrs) {
                if (c.getName().equalsIgnoreCase(Name)) {
                    try {
                        if (c.getClient() != null) {
                            if (c.getClient() != client) {
                                c.getClient().unLockDisconnect();
                            }
                        }
                    } catch (Exception ex) {
                    }
                    chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
                    if (chrs.contains(c)) {
                        ch.removePlayer(c);
                    }
                    c.getMap().removePlayer(c);
                }
            }
        });
    }

    public List<CheaterData> getCheaters() {
        List<CheaterData> cheaters = getPlayerStorage().getCheaters();

        Collections.sort(cheaters);
        return CollectionUtil.copyFirst(cheaters, 20);
    }
}
