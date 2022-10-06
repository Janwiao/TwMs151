package client.inventory;

import constants.GameConstants;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.MapleItemInformationProvider;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;

public class MaplePet implements Serializable {

    public static enum PetFlag {
        ITEM_PICKUP(0x01, 5190000, 5191000),
        EXPAND_PICKUP(0x02, 5190002, 5191002), //idk
        AUTO_PICKUP(0x04, 5190003, 5191003), //idk
        UNPICKABLE(0x08, 5190005, -1), //not coded
        LEFTOVER_PICKUP(0x10, 5190004, 5191004), //idk
        HP_CHARGE(0x20, 5190001, 5191001),
        MP_CHARGE(0x40, 5190006, -1),
        PET_BUFF(0x80, -1, -1), //idk
        PET_DRAW(0x100, 5190007, -1), //nfs
        PET_DIALOGUE(0x200, 5190008, -1); //nfs

        private final int i, item, remove;

        private PetFlag(int i, int item, int remove) {
            this.i = i;
            this.item = item;
            this.remove = remove;
        }

        public final int getValue() {
            return i;
        }

        public final boolean check(int flag) {
            return (flag & i) == i;
        }

        public static PetFlag getByAddId(final int itemId) {
            for (PetFlag flag : PetFlag.values()) {
                if (flag.item == itemId) {
                    return flag;
                }
            }
            return null;
        }

        public static PetFlag getByDelId(final int itemId) {
            for (PetFlag flag : PetFlag.values()) {
                if (flag.remove == itemId) {
                    return flag;
                }
            }
            return null;
        }
    }

    private static final long serialVersionUID = 9179541993413738569L;
    private String name;
    private int Fh = 0, stance = 0, uniqueid, petitemid, secondsLeft = 0;
    private Point pos;
    private byte fullness = 100, level = 1, summoned = 0;
    private short inventorypos = 0, closeness = 0, flags = 0;
    public long lastAttack;

    private MaplePet(final int petitemid, final int uniqueid) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
    }

    private MaplePet(final int petitemid, final int uniqueid, final short inventorypos) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
        this.inventorypos = inventorypos;
    }

    public static MaplePet loadFromDb(final int itemid, final int petid, final short inventorypos) {
        try {
            final MaplePet ret = new MaplePet(itemid, petid, inventorypos);

            Connection con = DatabaseConnection.getConnection(); // Get a connection to the database
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM pets WHERE petid = ?")) {
                ps.setInt(1, petid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }

                    ret.setName(rs.getString("name"));
                    ret.setCloseness(rs.getShort("closeness"));
                    ret.setLevel(rs.getByte("level"));
                    ret.setFullness(rs.getByte("fullness"));
                    ret.setSecondsLeft(rs.getInt("seconds"));
                    ret.setFlags(rs.getShort("flags"));
                }
            }

            return ret;
        } catch (SQLException ex) {
            Logger.getLogger(MaplePet.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public final void saveToDb() {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?, seconds = ?, flags = ? WHERE petid = ?")) {
                ps.setString(1, name); // Set name
                ps.setByte(2, level); // Set Level
                ps.setShort(3, closeness); // Set Closeness
                ps.setByte(4, fullness); // Set Fullness
                ps.setInt(5, secondsLeft);
                ps.setShort(6, flags);
                ps.setInt(7, uniqueid); // Set ID
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
        }
    }

    public static MaplePet createPet(final int itemid, final int uniqueid) {
        return createPet(itemid, MapleItemInformationProvider.getInstance().getName(itemid), 1, 0, 100, uniqueid, itemid == 5000054 ? 18000 : 0, (short) (itemid == 5000067 && !GameConstants.GMS ? 0x37 : 0));
    }

    public static MaplePet createPet(int itemid, String name, int level, int closeness, int fullness, int uniqueid, int secondsLeft, short flag) {
        if (uniqueid <= -1) { //wah
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        try {
            try (PreparedStatement pse = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (petid, name, level, closeness, fullness, seconds, flags) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                pse.setInt(1, uniqueid);
                pse.setString(2, name);
                pse.setByte(3, (byte) level);
                pse.setShort(4, (short) closeness);
                pse.setByte(5, (byte) fullness);
                pse.setInt(6, secondsLeft);
                pse.setShort(7, flag);
                pse.executeUpdate();
            }
        } catch (final SQLException ex) {
            return null;
        }
        final MaplePet pet = new MaplePet(itemid, uniqueid);
        pet.setName(name);
        pet.setLevel(level);
        pet.setFullness(fullness);
        pet.setCloseness(closeness);
        pet.setFlags(flag);
        pet.setSecondsLeft(secondsLeft);

        return pet;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final boolean getSummoned() {
        return summoned > 0;
    }

    public final byte getSummonedValue() {
        return summoned;
    }

    public final void setSummoned(final int summoned) {
        this.summoned = (byte) summoned;
    }

    public final short getInventoryPosition() {
        return inventorypos;
    }

    public final void setInventoryPosition(final short inventorypos) {
        this.inventorypos = inventorypos;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public final short getCloseness() {
        return closeness;
    }

    public final void gainCloseness(final int amount) {
        this.closeness += (short) amount;
    }

    public final void setCloseness(final int closeness) {
        this.closeness = (short) closeness;
    }

    public final byte getLevel() {
        return level;
    }

    public final void setLevel(final int level) {
        this.level = (byte) level;

    }

    public final byte getFullness() {
        return 100;
    }

    public final void setFullness(final int fullness) {
        this.fullness = (byte) fullness;
    }

    public final short getFlags() {
        return flags;
    }

    public final void setFlags(final int fffh) {
        this.flags = (short) fffh;

    }

    public final int getFh() {
        return Fh;
    }

    public final void setFh(final int Fh) {
        this.Fh = Fh;
    }

    public final Point getPos() {
        return pos;
    }

    public final void setPos(final Point pos) {
        this.pos = pos;
    }

    public final int getStance() {
        return stance;
    }

    public final void setStance(final int stance) {
        this.stance = stance;
    }

    public final int getPetItemId() {
        return petitemid;
    }

    public final boolean canConsume(final int itemId) {
        final MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        return mii.getItemEffect(itemId).getPetsCanConsume().stream().anyMatch((petId) -> (petId == petitemid));
    }

    public final void updatePosition(final List<LifeMovementFragment> movement) {
        movement.stream().filter((move) -> (move instanceof LifeMovement)).map((move) -> {
            if (move instanceof AbsoluteLifeMovement) {
                setPos(((LifeMovement) move).getPosition());
            }
            return move;
        }).forEachOrdered((move) -> {
            setStance(((LifeMovement) move).getNewstate());
        });
    }

    public final int getSecondsLeft() {
        return secondsLeft;
    }

    public final void setSecondsLeft(int sl) {
        this.secondsLeft = sl;

    }
}
