/*
	Map : Mu Lung Training Center
	Npc : So Gong
        Desc : Training Center Start
 */

var status = -1;
var sel;
var mapid;

function start() {
    mapid = cm.getMapId();

    if (mapid == 925020001) {
	cm.sendSimple("�ڪ��v���O�Z���̳̱j�j���H. �A���Q�n�D�ԧڮv��? ���n���ڨS�����A. \r #b#L0# �ڷQ�n�ۤv�D��.#l \n\r #L1# �ڷQ�n�ն��D��.#l \n\r #L2# �ڭn�I���y�a.#l \n\r #L3# �ڷQ�n�M���ڪ��n��.#l \n\r #L5# ����O�Z���D�]?#l                                                                                            ");
    } else if (isRestingSpot(mapid)) {
	cm.sendSimple("�ګ���r!?�S�Q��A�w�g��F�o�ؤ��ǤF. ���˩O? �n����U�h?#b \n\r #L0# �O��,�ڷ|�~�����U�h��.#l \n\r #L1# �ڭn���}#l \n\r #L2# �ڭn�x�s�ܥ������n��.#l                                              ");
    } else {
	cm.sendYesNo("����? �A�w�g�ǳƦn�n�h�X�F? �A���n�~��D�Զ�. �A�T�w�u���n�h�X?                                              ");
    }
}

function action(mode, type, selection) {
    if (mapid == 925020001) {
	if (mode == 1) {
	    status++;
	} else {
	    cm.dispose();
		return;
	}
	if (status == 0) {
	    sel = selection;

	    if (sel == 5) {
		cm.sendNext("�ڪ��v���O�Z���̳̱j�j���H. and he is responsible for erecting this amazing Mu Lung Training Tower. Mu Lung Training Tower is a colossal training facility that consists of 38 floors. Each floor represents additional levels of difficulty. Of course, with your skills, reaching the top floor will be impossible...");
		cm.dispose();
	    } else if (sel == 3) {
		cm.sendYesNo("�A�n�걼���e�����d������? ���U���N�n�q�Y�}�l�O!                                              ");
	    } else if (sel == 2) {
		cm.sendSimple("�A�Ҿ֦����n�� #b"+cm.getDojoPoints()+"#k. �A�i�H�ϥοn���I���H�U�D��, ���A�ݭn�������n���~�i�I��...\n\r #L0##i1132000:# #t1132000#(200)#l \n\r #L1##i1132001:# #t1132001#(1800)#l \n\r #L2##i1132002:# #t1132002#(4000)#l \n\r #L3##i1132003:# #t1132003#(9200)#l \r\n                                              ");
	    } else if (sel == 1) {
		if (cm.getParty() != null) {
		    if (cm.isLeader()) {
			cm.sendYesNo("�A�{�b�N�n�i�J�F��?                                              ");
		    } else {
			cm.sendOk("�K, �A���O������. ���D�A�Q�j�����J!? �ЧA�������M�ڹ�ܧa...                                              ");
		    }
		}
	    } else if (sel == 0) {
		if (cm.getParty() != null) {
			cm.sendOk("�����}�ն�.                                              ");
			cm.dispose();
			return;
		}
		var record = cm.getQuestRecord(150000);
		var data = record.getCustomData();

		if (data != null) {
		    var idd = get_restinFieldID(parseInt(data));
		    if (idd != 925020002) {
		        cm.dojoAgent_NextMap(true, true, idd);
		        record.setCustomData(null);
		    } else {
			cm.sendOk("�еy�Ԥ@�U�l.                                              ");
		    }
		} else {
		    cm.start_DojoAgent(true, false);
		}
		cm.dispose();
	    // cm.sendYesNo("The last time you took the challenge yourself, you were able to reach Floor #18. I can take you straight to that floor, if you want. Are you interested?");
	    }
	} else if (status == 1) {
	    if (sel == 3) {
		cm.setDojoRecord(true);
		//cm.sendOk("�ڭn�N�ڪ��n���k�s.                                              ");
	    } else if (sel == 2) {
		var record = cm.getDojoRecord();
		var required = 0;
		
		switch (record) {
		    case 0:
			required = 200;
			break;
		    case 1:
			required = 1800;
			break;
		    case 2:
			required = 4000;
			break;
		    case 3:
			required = 9200;
			break;
		}

		if (record == selection && cm.getDojoPoints() >= required) {
		    var item = 1132000 + record;
		    if (cm.canHold(item)) {
			cm.gainItem(item, 1);
			cm.setDojoRecord(false);
		    } else {
			cm.sendOk("�нT�{�A�O�_�֦��ӹD��.                                              ");
		    }
		} else {
		    cm.sendOk("�A�S���������n���i�H�I��,�n���n����L���y�a�O?                                              ");
		}
		cm.dispose();
	    } else if (sel == 1) {
		cm.start_DojoAgent(true, true);
		cm.dispose();
	    }
	}
    } else if (isRestingSpot(mapid)) {
	if (mode == 1) {
	    status++;
	} else {
	    cm.dispose();
	    return;
	}

	if (status == 0) {
	    sel = selection;

	    if (sel == 0) {
		if (cm.getParty() == null || cm.isLeader()) {
		    cm.dojoAgent_NextMap(true, true);
		} else {
		    cm.sendOk("�ЧA�������P�ڹ��.                                              ");
		}
		//cm.getQuestRecord(150000).setCustomData(null);
		cm.dispose();
	    } else if (sel == 1) {
		cm.askAcceptDecline("�A�Q�n�h�X? �A�u���Q���}�o��?                                              ");
	    } else if (sel == 2) {
		if (cm.getParty() == null) {
			var stage = get_stageId(cm.getMapId());

			cm.getQuestRecord(150000).setCustomData(stage);
			cm.sendOk("�ڤw�g���A�O���n�F,�U���D�Ԯɧڷ|���A�ǰe�즹���d.                                              ");
			cm.dispose();
		} else {
			cm.sendOk("�K...�A����O���n����ζ��Ҧ�                                              ");
			cm.dispose();
		}
	    }
	} else if (status == 1) {
	    if (sel == 1) {
		if (cm.isLeader()) {
			cm.warpParty(925020002);
		} else {
			cm.warp(925020002);
		}
	    }
	    cm.dispose();
	}
    } else {
	if (mode == 1) {
		if (cm.isLeader()) {
			cm.warpParty(925020002);
		} else {
			cm.warp(925020002);
		}
	}
	cm.dispose();
    }
}

function get_restinFieldID(id) {
	var idd = 925020002;
    switch (id) {
	case 1:
	    idd =  925020600;
	    break;
	case 2:
	    idd =  925021200;
	    break;
	case 3:
	    idd =  925021800;
	    break;
	case 4:
	    idd =  925022400;
	    break;
	case 5:
	    idd =  925023000;
	    break;
	case 6:
	    idd =  925023600;
	    break;
    }
    for (var i = 0; i < 10; i++) {
	var canenterr = true;
	for (var x = 1; x < 39; x++) {
		var map = cm.getMap(925020000 + 100 * x + i);
		if (map.getCharactersSize() > 0) {
			canenterr = false;
			break;
		}
	}
	if (canenterr) {
		idd += i;
		break;
	}
}
	return idd;
}

function get_stageId(mapid) {
    if (mapid >= 925020600 && mapid <= 925020614) {
	return 1;
    } else if (mapid >= 925021200 && mapid <= 925021214) {
	return 2;
    } else if (mapid >= 925021800 && mapid <= 925021814) {
	return 3;
    } else if (mapid >= 925022400 && mapid <= 925022414) {
	return 4;
    } else if (mapid >= 925023000 && mapid <= 925023014) {
	return 5;
    } else if (mapid >= 925023600 && mapid <= 925023614) {
	return 6;
    }
    return 0;
}

function isRestingSpot(id) {
    return (get_stageId(id) > 0);
}