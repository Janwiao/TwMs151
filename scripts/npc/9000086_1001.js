var status = 0;
var dsa = "";

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0 && mode == 0) {
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {

        dsa += "#r �K�K�K������#i4251202##r   �U��ө�   #i4251202# #r�������K�K�K \r\n\r\n#r";

        var selStr = dsa + "#r" + cm.getVipname() + " #r#h # #k�z�n�I\r\n�п�ܱz�ݭn���i�J���ө�:\r\n#r#L0#���l���f�ө�#l  #L1#���l���b�ө�#l   #L2#���l���Űө�#l   \r\n#d#L3#�������F�ө�#l  #L4#�ǵs�ۼv�ө�#l   #L5#�c�]����ө�#l\r\n#b#L6#�_�I�a  �ө�#l  #L7#��O��U�ө�#l   #L8#���������x  �ө�#l  \r\n#L9#�v�Z�̰ө�#l  #L10#�s���ǤH�ө�#l   #L11#�s���g�T�i�h�ө�#l  \r\n\r\n#k  �H�U�O�I���ө��J�f�G#b\r\n#L20#��  ��  �ө�#l  #L21#�����]  �ө�#l   #L22#���������ө�#l  \r\n#L23#���q��  �ө�#l  #L24#�ǻ���  �ө�#l   #L25#8 �������ө�#l  \r\n#L26#��l��  �ө�#l  #L27#�~�P��  �ө�#l   #L28#���A�̹��ө�#l \r\n "; 
        cm.sendSimple(selStr);

    } else if (status == 1) {
        switch (selection) {
        case 0:
            cm.dispose();
	    cm.openShop(100000);
            break;
        case 1:
            cm.dispose();
	    cm.openShop(100001);
            break;
        case 2:
            cm.dispose();
            cm.openShop(100002);
            break;

        case 3:
            cm.dispose();
            cm.openShop(100003);
            break;
        case 4:
            cm.dispose();
            cm.openShop(100004);
            break;
        case 5:
            cm.dispose();
            cm.openShop(100005);
            break;

        case 6:
            cm.dispose();
            cm.openShop(100006);
            break;
        case 7:
            cm.dispose();
            cm.openShop(100007);
            break;
        case 8:
            cm.dispose();
            cm.openShop(100008);
            break;

        case 9:
            cm.dispose();
            cm.openShop(100009);
            break;
        case 10:
            cm.dispose();
            cm.openShop(100010);
            break;
        case 11:
            cm.dispose();
            cm.openShop(100011);
            break;

        case 20:
            cm.dispose();
            cm.openShop(100020);
            break;
        case 21:
            cm.dispose();
            cm.openShop(100021);
            break;
        case 22:
            cm.dispose();
            cm.openShop(100022);
            break;

        case 23:
            cm.dispose();
            cm.openShop(100023);
            break;
        case 24:
            cm.dispose();
            cm.openShop(100024);
            break;
        case 25:
            cm.dispose();
            cm.openShop(100025);
            break;

        case 26:
            cm.dispose();
            cm.openShop(100026);
            break;
        case 27:
            cm.dispose();
            cm.openShop(100027);
            break;
        case 28:
            cm.dispose();
            cm.openShop(100028);
            break;



        }
    }
}