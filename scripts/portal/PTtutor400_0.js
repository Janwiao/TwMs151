load('nashorn:mozilla_compat.js');
importPackage(Packages.tools.packet);

function enter(pi) {
    pi.getClient().getSession().write(CField.NPCPacket.getNPCTalk(1402001, 0, "��..�w�g�}�l�F��?�������঳�t���C", "00 00", 17, 1402001));
    return true;
}