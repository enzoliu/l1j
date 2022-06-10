package l1j.server.server.model.item;

import static l1j.server.server.model.skill.L1SkillId.STATUS_FLOATING_EYE;

import java.util.Random;

import l1j.server.server.ClientThread;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.L1Cooking;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;

public class L1Food extends L1Potion {
	private static Random _random = new Random();
	public L1Food(){
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId) throws Exception {
		if (itemId == 40058) { // きつね色のパン
			UseHeallingPotion(pc, 30, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40071) { // 黒こげのパン
			UseHeallingPotion(pc, 70, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40734) { // 信頼のコイン
			UseHeallingPotion(pc, 50, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41403) { // クジャクの食糧
			UseHeallingPotion(pc, 300, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId >= 41417 && itemId <= 41421) { // 「アデンの夏」イベント限定アイテム
			UseHeallingPotion(pc, 90, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41337) { // 祝福された麦パン
			UseHeallingPotion(pc, 85, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40056 || itemId == 40057 || itemId == 40059 || itemId == 40060 || itemId == 40061 || itemId == 40062 || itemId == 40063 || itemId == 40064
				|| itemId == 40065 || itemId == 40069 || itemId == 40072 || itemId == 40073 || itemId == 140061 || itemId == 140062 || itemId == 140065 || itemId == 140069
				|| itemId == 140072 || itemId == 41296 || itemId == 41297 || itemId == 41266 || itemId == 41267 || itemId == 41274 || itemId == 41275 || itemId == 41276
				|| itemId == 41252 || itemId == 49040 || itemId == 49041 || itemId == 49042 || itemId == 49043 || itemId == 49044 || itemId == 49045 || itemId == 49046 || itemId == 49047) {
			pc.getInventory().removeItem(l1iteminstance, 1);
			short foodvolume1 = (short)(l1iteminstance.getItem().getFoodVolume() / 10);
			short foodvolume2 = 0;
			if (foodvolume1 <= 0) {
				foodvolume1 = 5;
			}
			if (pc.get_food() >= 225) {
				pc.sendPackets(new S_PacketBox(S_PacketBox.FOOD, (short)pc.get_food()));
			} else {
				foodvolume2 = (short)(pc.get_food() + foodvolume1);
				if (foodvolume2 <= 225) {
					pc.set_food(foodvolume2);
					pc.sendPackets(new S_PacketBox(S_PacketBox.FOOD, (short)pc.get_food()));
				} else {
					pc.set_food((short)225);
					pc.sendPackets(new S_PacketBox(S_PacketBox.FOOD, (short)pc.get_food()));
				}
			}
			if (itemId == 40057) { // フローティングアイ肉
				pc.setSkillEffect(STATUS_FLOATING_EYE, 0);
			}
			pc.sendPackets(new S_ServerMessage(76, l1iteminstance.getItem().getNameId()));
		} else if (itemId == 41298) { // ヤングフィッシュ
			UseHeallingPotion(pc, 4, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41299) { // スウィフトフィッシュ
			UseHeallingPotion(pc, 15, 194);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41300) { // ストロングフィッシュ
			UseHeallingPotion(pc, 35, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41301) { // シャイニングレッドフィッシュ
			int chance = _random.nextInt(10);
			if (chance >= 0 && chance < 5) {
				UseHeallingPotion(pc, 15, 189);
			} else if (chance >= 5 && chance < 9) {
				createNewItem(pc, 40019, 1);
			} else if (chance >= 9) {
				int gemChance = _random.nextInt(3);
				if (gemChance == 0) {
					createNewItem(pc, 40045, 1);
				} else if (gemChance == 1) {
					createNewItem(pc, 40049, 1);
				} else if (gemChance == 2) {
					createNewItem(pc, 40053, 1);
				}
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41302) { // シャイニンググリーンフィッシュ
			int chance = _random.nextInt(3);
			if (chance >= 0 && chance < 5) {
				UseHeallingPotion(pc, 15, 189);
			} else if (chance >= 5 && chance < 9) {
				createNewItem(pc, 40018, 1);
			} else if (chance >= 9) {
				int gemChance = _random.nextInt(3);
				if (gemChance == 0) {
					createNewItem(pc, 40047, 1);
				} else if (gemChance == 1) {
					createNewItem(pc, 40051, 1);
				} else if (gemChance == 2) {
					createNewItem(pc, 40055, 1);
				}
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41303) { // シャイニングブルーフィッシュ
			int chance = _random.nextInt(3);
			if (chance >= 0 && chance < 5) {
				UseHeallingPotion(pc, 15, 189);
			} else if (chance >= 5 && chance < 9) {
				createNewItem(pc, 40015, 1);
			} else if (chance >= 9) {
				int gemChance = _random.nextInt(3);
				if (gemChance == 0) {
					createNewItem(pc, 40046, 1);
				} else if (gemChance == 1) {
					createNewItem(pc, 40050, 1);
				} else if (gemChance == 2) {
					createNewItem(pc, 40054, 1);
				}
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41304) { // シャイニングホワイトフィッシュ
			int chance = _random.nextInt(3);
			if (chance >= 0 && chance < 5) {
				UseHeallingPotion(pc, 15, 189);
			} else if (chance >= 5 && chance < 9) {
				createNewItem(pc, 40021, 1);
			} else if (chance >= 9) {
				int gemChance = _random.nextInt(3);
				if (gemChance == 0) {
					createNewItem(pc, 40044, 1);
				} else if (gemChance == 1) {
					createNewItem(pc, 40048, 1);
				} else if (gemChance == 2) {
					createNewItem(pc, 40052, 1);
				}
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId >= 41277 && itemId <= 41292 || itemId >= 49049 && itemId <= 49064 || itemId >= 49244 && itemId <= 49259) { // 料理
			L1Cooking.useCookingItem(pc, l1iteminstance);
		}
	}
	private boolean createNewItem(L1PcInstance pc, int item_id, int count) {
		L1ItemInstance item = ItemTable.getInstance().createItem(item_id);
		item.setCount(count);
		if (item != null) {
			if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
				pc.getInventory().storeItem(item);
			} else { // 持てない場合は地面に落とす 処理のキャンセルはしない（不正防止）
				L1World.getInstance().getInventory(pc.getX(), pc.getY(),
						pc.getMapId()).storeItem(item);
			}
			pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
			return true;
		} else {
			return false;
		}
	}
}
