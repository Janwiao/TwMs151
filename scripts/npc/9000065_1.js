
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
        cm.sendSimple("#l#d#��,��@�ӧA�Q�n��¾�~�a!\r\n#L5024#�s��\r\n#L501#�^��\r\n#L502#�t�M�h\r\n#L504#�j�]�q�h(��,�r)\r\n#L506#�D��\r\n#L507#���g��\r\n#L509#�]�Ϫ�\r\n#L5010#�t�v����\r\n#L5012#���Q\r\n#L5013#�j��\r\n#L5014#���s\r\n#L5015#�Q��\r\n#L5020#�Һ��Ův\r\n#L5021#�g�z���H\r\n#L5022#���Ҿԯ�\r\n#L5016#�ǻ�\r\n#L5023#�g���i�h\r\n#L5011#�v�R��\r\n#L5017#�s�]�q�h\r\n#L5018#���F�C�L\r\n#L5019#�c�]����\r\n#L5025#�ۼv�t�S\r\n#L5026#���s�L��\r\n#L5027#�t�κ޲z��");
        } else if (status == 1) {
       if (selection == 501) { 
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
		}
        }
		}
		}
