/**
 ���A�M��NPC
 LS�s�@
**/

var status = 0;


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)                           						
            status++;
        else
            status--;
        if (status == 0) {
        cm.sendSimple("�z�n�A�ڬO#e#d���A�p����#k\r\n#k#L11#���Ŵ���(�̰���150��)#L12#�۰���¾#L13#¾�~�ഫ#L14#��¾�~�ޯ��I��#L15#�����¾�~�˳�#L16#�W�n�[��50");
        } else if (status == 1) {
        if (selection == 11) {
        if (cm.getLevel() > 150){
        cm.sendOk("�z�w�W�L150��.");
		cm.dispose();
		return;
        }
		else
		{
        cm.getPlayer().levelUp();
        cm.dispose();
		}
       	} else if (selection == 12) {
		cm.dispose();
		cm.openNpc(2007);
		} else if (selection == 13){
	   cm.dispose();
		cm.openNpc(9000065,1);
		} else if (selection == 14) {
		cm.maxSkillsByJob();
		cm.dispose();
		} else if (selection == 15) {
		cm.dispose();
		cm.openNpc(9000065,2);
		} else if (selection == 16){
		cm.getPlayer().setFame(50);
		cm.dispose();
		}
		}
        }
		}
