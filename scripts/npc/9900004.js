/** Author: nejevoli
	NPC Name: 		NimaKin
	Map(s): 		Victoria Road : Ellinia (180000000)
	Description: 		Maxes out your stats and able to modify your equipment stats
*/
importPackage(java.lang);

var status = 0;
var slot = Array();
var stats = Array( "�O�q", "�ӱ�", "���O", "���B", "HP", "MP", "���z�����O", "�]�k�����O", "���z���m�O", "�]�k���m�O", "�R���v", "�j�׭�", "����", "���ʳt��", "���D�O", "�Ѿl�ı�����", "�����K�l", "�w�ı�����", "�P���ż�", "��1�Ƽ��", "��2�Ƽ��", "��3�Ƽ��", "��4�Ƽ��", "��5�Ƽ��", "������");
var selected;
var statsSel;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 0 && mode == 0) {
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;

    if (status == 0) {
	if (cm.getPlayerStat("ADMIN") == 1) {
		cm.sendSimple("�A�n������?#b\r\n#L9#���Ŵ���\r\n#L10#GM�s��\r\n#L11#�H�N�I�q\r\n#L12#�˳ƻ��\r\n#L50#�޲z����¾NPC\r\n#L0#��O�ȥ���!#l\r\n#L1#�ޯ����!#l\r\n#L2#���ܸ˳ƪ��ƭ�!#l\r\n#L3#Look at potential values#l\r\n#L4#�]�mAP/SP��0#l\r\n#L5#�M���ޯ�#l\r\n#L6#¾�~�ޯ����#l\r\n#L7#�M���ڪ���O��!#b\r\n#L8#����O50�t��\r\n");
	} else if (cm.getPlayerStat("GM") == 1) {
		cm.sendSimple("�A�n������?#b\r\n#L0#��O�ȥ���!#l\r\n#L1#�ޯ����!#l\r\n#L4�]�mAP/SP��0#l\r\n#L7#�M���ڪ���O��!#k");
	} else {
	    cm.dispose();
	}
    } else if (status == 1) {
	
	if (selection == 0) {
	    if (cm.getPlayerStat("GM") == 1) {
		cm.maxStats();
		cm.sendOk("�z����O�Ȥw����");
	    }
	    cm.dispose();
	} else if (selection == 1) {
	    //Beginner
	    if (cm.getPlayerStat("GM") == 1) {
		cm.maxAllSkills();
	    }
	    cm.dispose();
	} else if (selection == 2 && cm.getPlayerStat("ADMIN") == 1) {
	    var avail = "";
	    for (var i = -1; i > -199; i--) {
		if (cm.getInventory(-1).getItem(i) != null) {
		    avail += "#L" + Math.abs(i) + "##t" + cm.getInventory(-1).getItem(i).getItemId() + "##l\r\n";
		}
		slot.push(i);
	    }
	    cm.sendSimple("�п�ܧA�n�s�誺�˳�?\r\n#b" + avail);
	} else if (selection == 3 && cm.getPlayerStat("ADMIN") == 1) {
		var eek = cm.getAllPotentialInfo();
		var avail = "#L0#�j�Mpotential ���~#l\r\n";
		for (var ii = 0; ii < eek.size(); ii++) {
			avail += "#L" + eek.get(ii) + "#Potential ID " + eek.get(ii) + "#l\r\n";
		}
		cm.sendSimple("�A�n�ק���Ӽƭ�?\r\n#b"+ avail);
		status = 9;
	} else if (selection == 4) {
		cm.getPlayer().resetAPSP();
		cm.dispose();
	} else if (selection == 7) {
	    if (cm.getPlayerStat("GM") == 1) {
		cm.getPlayer().resetStats(4, 4, 4, 4);
		cm.sendOk("�ڤw�M���z����O��");
	    }
	    cm.dispose();

	//���Ŵ���
	} else if (selection == 9) {
		if (cm.getPlayerStat("GM") == 1) {
                cm.getPlayer().levelUp();
		//cm.gainExp(2147483647);
		//cm.gainExp(10000);
	    }
	    cm.dispose();
	//Gm�s��
	} else if (selection == 10) {
		if (cm.getPlayerStat("GM") == 1) {
		//********************���r���i(�]���O)********************
		//���t�Ρ� 
		//
		//cm.YellowMessage("�y�t�Τ��i�z:����s���A���������O�A�y��3:30���N�i�歫�Ұʧ@.");
    	//cm.YellowMessage("�y�C�����i�z:�y��4�I45���N�Ȯ��������A���A�w�p15�������|�A���}�A�p�����K�Ш��̡ABlueLineMS�޲z�ζ�");
		//cm.YellowMessage("�y���v���i�z: ���v�w�o��A�n����ɽЦܦۥѥ����A���Y�W�g�ۡy�L�i�`��ڡz��NPC�I��.");
		//cm.YellowMessage("�y���ʤ��i�z: ���y�w�o��A�n�I���ɽЦܦۥѥ����A���Y�W�g�ۡy�L�i�`��ڡz��NPC�I��.");	
		//cm.YellowMessage("�y�A�������z:�Y�B��Ʀr���`���˳ƽЧY�ɳq���C���޲z���A�Y���^���èp�ۯd�U�ϥξD�d��ɤ@�ߵ����~����.");
		//cm.YellowMessage("�y���v���i�z:���a�y�z�A");
		//
		//���@���
		//
		//cm.YellowMessage("������A�������ɶ����ߤW12�I��. Today the server closed time at PM 11:00.");
	    //cm.YellowMessage("");
		//
		//********************�Ŧr���i(�@���W�D)********************
		//���t�Ρ� 
		//
		//cm.worldMessage("�y�t�Τ��i�z:����s���A���������O�A�y��3:30���N�i�歫�Ұʧ@.");
		//cm.worldMessage("�y�C�����i�z:�y��4�I45���N�Ȯ��������A���A�w�p15�������|�A���}�A�p�����K�Ш��̡ABlueLineMS�޲z�ζ�");
		//cm.worldMessage("�y���v���i�z: ���v�w�o��A�n����ɽЦܦۥѥ����A���Y�W�g�ۡy�L�i�`��ڡz��NPC�I��.");	
		//cm.worldMessage("�y���ʤ��i�z: ���y�w�o��A�n�I���ɽЦܦۥѥ����A���Y�W�g�ۡy�L�i�`��ڡz��NPC�I��.");	
		//cm.worldMessage("�y�A�������z:�Y�B��Ʀr���`���˳ƽЧY�ɳq���C���޲z���A�Y���^���èp�ۯd�U�ϥξD�d��ɤ@�ߵ����~����");
        //cm.worldMessage("�y���v���i�z:���a�y�z�A");
		//
		//���@���
		//
		//cm.worldMessage("1.�������C���ȱ������v.");
        //cm.worldMessage("2.���Ǳ��I���̦h5�I.");
        //cm.worldMessage("3.�C���I�ƧI���վ㬰5�U:1�I.");
		//cm.worldMessage("�n����,�{�bRC�s�w�[�}�ۥѲ�ѫ��o�A�Q�}MIC�����a�i�H�ӳ�.");
		cm.worldMessage("BlueLineMS-LS:�n�ѥ[���ֵn�O��");
		//cm.worldMessage("");
		//
		cm.dispose();
		}
	} else if (selection == 11) {
		if (cm.getPlayerStat("GM") == 1) {
		cm.playMusic("BgmUI/Title");
		cm.dispose();
		}	
	} else if (selection == 12) {
		if (cm.getPlayerStat("GM") == 1) {
		cm.dispose();
		cm.openNpc(9000065, 3)
		//cm.gainItem(1002140,1); //GM��
	    //cm.gainItem(1042003,1); //GM��
		//cm.gainItem(1062007,1); //GM��
		//cm.gainItem(1072010,1); //GM��
		//cm.gainItem(13220,1); //GM��
		}		
  //�H�U�Ҭ���¾NPC�� 
    } else if (selection == 50){
			cm.sendSimple("#l#d#��,�޲z���M����¾NPC,��@�ӧA�Q�n��¾�~�a!\r\n#L5024#�s��\r\n#L501#�^��\r\n#L502#�t�M�h\r\n#L504#�j�]�q�h(��,�r)\r\n#L506#�D��\r\n#L507#���g��\r\n#L509#�]�Ϫ�\r\n#L5010#�t�v����\r\n#L5012#���Q\r\n#L5013#�j��\r\n#L5014#���s\r\n#L5015#�Q��\r\n#L5020#�Һ��Ův\r\n#L5021#�g�z���H\r\n#L5022#���Ҿԯ�\r\n#L5016#�ǻ�\r\n#L5023#�g���i�h\r\n#L5011#�v�R��\r\n#L5017#�s�]�q�h\r\n#L5018#���F�C�L\r\n#L5019#�c�]����\r\n#L5025#�ۼv�t�S\r\n#L5026#���s�L��\r\n#L5027#�t�κ޲z��");
	} else if (selection == 5) {
		cm.clearSkills();
		cm.dispose();
	} else if (selection == 8) {
		cm.openNpc(2000);
		cm.dispose();
	//	return;
	} else if (selection == 6) {
		//cm.maxSkillsByJob();
                 cm.getPlayer().maxSkillsByJobN();
		cm.dispose();
	} else {
		cm.dispose();
	}
	} else if (status == 51){
	 } else if (selection == 501) { 
        cm.changeJob (112);
        return;	
      } else if (selection == 502) { 
        cm.changeJob (122);
		return;
      } else if (selection == 503) { 
        cm.changeJob (132);
		return;
      } else if (selection == 504) { 
        cm.changeJob (212);
		return;
      } else if (selection == 505) { 
        cm.changeJob (222);
		return;
      } else if (selection == 506) { 
        cm.changeJob (232); 
		cm.dispose();
      } else if (selection == 507) { 
        cm.changeJob (312); 
		cm.dispose();
      } else if (selection == 508) { 
        cm.changeJob (322); 
		cm.dispose();
      } else if (selection == 509) { 
        cm.changeJob (412); 
		cm.dispose();
      } else if (selection == 5010) { 
        cm.changeJob (422); 
		cm.dispose();
      } else if (selection == 5011) { 
        cm.changeJob (434); 
		cm.dispose();
      } else if (selection == 5012) { 
        cm.changeJob (512); 
		cm.dispose();
      } else if (selection == 5013) { 
        cm.changeJob (522); 
		cm.dispose();
      } else if (selection == 5014) { 
        cm.changeJob (532); 
		cm.dispose();
      } else if (selection == 5015) { 
        cm.changeJob (1000); 
		cm.dispose();
      } else if (selection == 5016) { 
        cm.changeJob (2000); 
		cm.dispose();
      } else if (selection == 5017) { 
        cm.changeJob (2218); 
		cm.dispose();
      } else if (selection == 5018) { 
        cm.changeJob (2312); 
		cm.dispose();
      } else if (selection == 5019) { 
        cm.changeJob (3112); 
		cm.dispose();
      } else if (selection == 5020) { 
        cm.changeJob (3212); 
		cm.dispose();
      } else if (selection == 5021) { 
        cm.changeJob (3312); 
		cm.dispose();
      } else if (selection == 5022) { 
        cm.changeJob (3512); 
		cm.dispose();
      } else if (selection == 5023) { 
        cm.changeJob (2112); 
		cm.dispose();
      } else if (selection == 5024) { 
        cm.changeJob (0); 
		cm.dispose();
      } else if (selection == 5025) { 
        cm.changeJob (2003); 
		cm.dispose();
      //} else if (selection == 5026) { 
        //cm.changeJob (572); 
		//cm.dispose();
      } else if (selection == 5027) { 
        cm.dispose();
		cm.changeJob (910); 
		return;
		
//�H�W�Ҭ���¾NPC��		
    } else if (status == 2 && cm.getPlayerStat("ADMIN") == 1) {
	selected = selection - 1;
	var text = "";
	for (var i = 0; i < stats.length; i++) {
	    text += "#L" + i + "#" + stats[i] + "#l\r\n";
	}
	cm.sendSimple("�A�M�w�ק�A��#b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k.\r\n��@���A�Q�ק諸��O�a! \r\n#b" + text);
	} else if (status == 3 && cm.getPlayerStat("ADMIN") == 1) {
	statsSel = selection;
	if (selection == 24) {
		cm.sendGetText("�A�Q���A��#b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k�����֪�" + stats[statsSel] + "�O?");
	} else {
		cm.sendGetNumber("�A�Q�n���A��#b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k�קאּ�h��" + stats[statsSel] + "�O?", 0, 0, 60004);
	}
    } else if (status == 4 && cm.getPlayerStat("ADMIN") == 1) {
	cm.changeStat(slot[selected], statsSel, selection);
	cm.sendOk("Your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k's " + stats[statsSel] + " has been set to " + selection + ".");
	cm.dispose();
	} else if (status == 10 && cm.getPlayerStat("ADMIN") == 1) {
		if (selection == 0) {
			cm.sendGetText("�A�n�j�M���� (e.g. STR %)");
			return;
		}
		cm.sendSimple("#L3#" + cm.getPotentialInfo(selection) + "#l");
		status = 0;
	} else if (status == 11 && cm.getPlayerStat("ADMIN") == 1) {
		var eek = cm.getAllPotentialInfoSearch(cm.getText());
		for (var ii = 0; ii < eek.size(); ii++) {
			avail += "#L" + eek.get(ii) + "#Potential ID " + eek.get(ii) + "#l\r\n";
		}
		cm.sendSimple("What would you like to learn about?\r\n#b"+ avail);
		status = 9;
	} else {
		cm.dispose();
    }
}
