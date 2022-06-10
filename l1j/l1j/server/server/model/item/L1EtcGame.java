package l1j.server.server.model.item;

import java.util.Random;

import l1j.server.server.ClientThread;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1EtcGame {
	private static Random _random = new Random();
	public L1EtcGame(){
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId) throws Exception {
		if (itemId == 40325){ //二段式魔法骰子by 0919162173
			Random random = new Random();
			int SK = random.nextInt(2) + 1;
			if (SK >= 1 && SK <= 2) {
				switch(SK){
					case 1:
						pc.sendPackets(new S_SystemMessage("(正面)"));
						break;
					case 2:
						pc.sendPackets(new S_SystemMessage("(反面)"));
						break;
				}
				pc.sendPackets(new S_SkillSound(pc.getId(),3236 + SK));// 骰子效果顯示
				pc.broadcastPacket(new S_SkillSound(pc.getId(),3236 + SK));
			}
		} else if (itemId == 40326){ //3段式魔法骰子
			Random random = new Random();
			int SK = random.nextInt(3) + 1;
			if (SK >= 1 && SK <= 3){
				switch(SK){
					case 1:
						pc.sendPackets(new S_SystemMessage("(剪刀)"));
						break;
					case 2:
						pc.sendPackets(new S_SystemMessage("(石頭)"));
						break;
					case 3:
						pc.sendPackets(new S_SystemMessage("(布)"));
						break;
				}
				pc.sendPackets(new S_SkillSound(pc.getId(),3228 + SK));// 骰子效果顯示
				pc.broadcastPacket(new S_SkillSound(pc.getId(),3228 + SK));
			}
		} else if (itemId == 40327){ //4段式魔法骰子
			Random random = new Random();
			int SK = random.nextInt(4) + 1;
			if (SK >= 1 && SK <= 4) {
				switch(SK){
					case 1:
						pc.sendPackets(new S_SystemMessage("(右上)"));
						break;
					case 2:
						pc.sendPackets(new S_SystemMessage("(左上)"));
						break;
					case 3:
						pc.sendPackets(new S_SystemMessage("(右下)"));
						break;
					case 4:
						pc.sendPackets(new S_SystemMessage("(左下)"));
						break;
				}
				pc.sendPackets(new S_SkillSound(pc.getId(),3240 + SK));// 骰子效果顯示
				pc.broadcastPacket(new S_SkillSound(pc.getId(),3240 + SK));
			}
		} else if (itemId == 40328){ //6段式魔法骰子
			Random random = new Random();
			int SK = random.nextInt(5) + 1;
			if (SK >= 1 && SK <= 6){
				pc.sendPackets(new S_SystemMessage("你骰出的點數是(" + (SK) + ")"));
				pc.sendPackets(new S_SkillSound(pc.getId(),3203 + SK));// 骰子效果顯示
				pc.broadcastPacket(new S_SkillSound(pc.getId(),	3203 + SK));
			}
		} else if (itemId == 40325) { // 2面コイン
			if (pc.getInventory().checkItem(40318, 1)) {
				int gfxid = 3237 + _random.nextInt(2);
				pc.sendPackets(new S_SkillSound(pc.getId(), gfxid));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), gfxid));
				pc.getInventory().consumeItem(40318, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40326) { // 3方向ルーレット
			if (pc.getInventory().checkItem(40318, 1)) {
				int gfxid = 3229 + _random.nextInt(3);
				pc.sendPackets(new S_SkillSound(pc.getId(), gfxid));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), gfxid));
				pc.getInventory().consumeItem(40318, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 40327) { // 4方向ルーレット
			if (pc.getInventory().checkItem(40318, 1)) {
				int gfxid = 3241 + _random.nextInt(4);
				pc.sendPackets(new S_SkillSound(pc.getId(), gfxid));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), gfxid));
				pc.getInventory().consumeItem(40318, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40328) { // 6面ダイス
			if (pc.getInventory().checkItem(40318, 1)) {
				int gfxid = 3204 + _random.nextInt(6);
				pc.sendPackets(new S_SkillSound(pc.getId(), gfxid));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), gfxid));
				pc.getInventory().consumeItem(40318, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		}
	}
}
