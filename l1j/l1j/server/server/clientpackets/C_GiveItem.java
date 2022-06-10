/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l1j.server.server.clientpackets;
import java.util.Random;

import l1j.server.server.ClientThread;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1PetType;

//所有怪物都可抓進化
import l1j.server.Config;
import l1j.server.server.datatables.BossSpawnTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.serverpackets.S_SystemMessage;
//~所有怪物都可抓進化
public class C_GiveItem extends ClientBasePacket {
	private static final String C_GIVE_ITEM = "[C] C_GiveItem";

	private static Random _random = new Random();

	public C_GiveItem(byte decrypt[], ClientThread client) {
		super(decrypt);
		int targetId = readD();
		readH();
		readH();
		int itemId = readD();
		int count = readD();

		L1PcInstance pc = client.getActiveChar();
		if (pc.isGhost()) {
			return;
		}

		L1Object object = L1World.getInstance().findObject(targetId);
		if (object == null || !(object instanceof L1NpcInstance)) {
			return;
		}
		L1NpcInstance target = (L1NpcInstance) object;
		if (!isNpcItemReceivable(target.getNpcTemplate())) {
			return;
		}
		L1Inventory targetInv = target.getInventory();

		L1Inventory inv = pc.getInventory();
		L1ItemInstance item = inv.getItem(itemId);
		if (item == null) {
			return;
		}
		if (item.isEquipped()) {
			pc.sendPackets(new S_ServerMessage(141)); // \f1装備しているものは、人に渡すことができません。
			return;
		}
		if (!item.getItem().isTradable()) {
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); // \f1%0は捨てたりまたは他人に讓ることができません。
			return;
		}
		if (item.getBless() >= 128) { // 封印された装備
			// \f1%0は捨てたりまたは他人に讓ることができません。
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
			return;
		}
		for (Object petObject : pc.getPetList().values()) {
			if (petObject instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) petObject;
				if (item.getId() == pet.getItemObjId()) {
					// \f1%0は捨てたりまたは他人に讓ることができません。
					pc.sendPackets(new S_ServerMessage(210, item.getItem()
							.getName()));
					return;
				}
			}
		}
		if (targetInv.checkAddItem(item, count) != L1Inventory.OK) {
			pc.sendPackets(new S_ServerMessage(942)); // 相手のアイテムが重すぎるため、これ以上あげられません。
			return;
		}
		item = inv.tradeItem(item, count, targetInv);
		target.onGetItem(item);
		target.turnOnOffLight();
		pc.turnOnOffLight();
		
		//所有怪物都可抓進化
		if (target.isDead()) {
			return;
		}
		L1PetType petType = PetTypeTable.getInstance().get(
				target.getNpcTemplate().get_npcId());
		//原本寵物照舊
		if (petType!=null && item.getItemId() == petType.getItemIdForTaming()) {
			tamePet(pc, target);
		}
		//肉抓所有怪物
		if (Config.CE_ACTIVE && item.getItemId() == 40056 && petType==null ) {
			//判斷是不是boss(boss列表在DB的spawnlist_boss TABLE)
			if (BossSpawnTable.getInstance().isBoss(target.getNpcId())) {
				pc.sendPackets(new S_SystemMessage("他是BOSS別亂抓，找死唷！"));
				return;
			}
			tamePet(pc, target);
		}
		//所有怪物都可進化
		if (item.getItemId() == 40070) {
			evolvePet(pc, target);
		}
		//~所有怪物都可抓進化
	}

	private final static String receivableImpls[] = new String[] { "L1Npc", // NPC
			"L1Monster", // モンスター
			"L1Guardian", // エルフの森の守護者
			"L1Teleporter", // テレポーター
			"L1Guard" }; // ガード

	private boolean isNpcItemReceivable(L1Npc npc) {
		for (String impl : receivableImpls) {
			if (npc.getImpl().equals(impl)) {
				return true;
			}
		}
		return false;
	}

	private void tamePet(L1PcInstance pc, L1NpcInstance target) {
		if (target instanceof L1PetInstance
				|| target instanceof L1SummonInstance) {
			return;
		}

		int petcost = 0;
		Object[] petlist = pc.getPetList().values().toArray();
		for (Object pet : petlist) {
			petcost += ((L1NpcInstance) pet).getPetcost();
		}
		int charisma = pc.getCha();
		if (Config.HIDDEN_ATTR_ENABLE) {
			charisma += pc.getAdditionalAttr(3) / 4;
		}
		if (pc.isCrown()) { // 君主
			charisma += 6;
		} else if (pc.isElf()) { // エルフ
			charisma += 12;
		} else if (pc.isWizard()) { // WIZ
			charisma += 6;
		} else if (pc.isDarkelf()) { // DE
			charisma += 6;
		} else if (pc.isDragonKnight()) { // ドラゴンナイト
			charisma += 6;
		} else if (pc.isIllusionist()) { // イリュージョニスト
			charisma += 6;
		}
		charisma -= petcost;

		L1PcInventory inv = pc.getInventory();
		if (charisma >= 6 && inv.getSize() < 180) {
			if (isTamePet(target)) {
				L1ItemInstance petamu = inv.storeItem(40314, 1); // ペットのアミュレット
				if (petamu != null) {
					new L1PetInstance(target, pc, petamu.getId());
					pc.sendPackets(new S_ItemName(petamu));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(324)); // てなずけるのに失敗しました。
			}
		}
	}

	private void evolvePet(L1PcInstance pc, L1NpcInstance target) {
		if (!(target instanceof L1PetInstance)) {
			return;
		}
		L1PcInventory inv = pc.getInventory();
		L1PetInstance pet = (L1PetInstance) target;
		L1ItemInstance petamu = inv.getItem(pet.getItemObjId());
		if (pet.getLevel() >= 30 && // Lv30以上
				pc == pet.getMaster() && // 自分のペット
				petamu != null) {
			L1PetType petType = PetTypeTable.getInstance().get(
					target.getNpcTemplate().get_npcId());
			L1ItemInstance highpetamu = inv.storeItem(40316, 1);
			if (highpetamu != null) {
				//所有怪物都可抓進化
				if(petType==null && Config.CE_ACTIVE){//其他怪物進化
					int npcId = pet.evolvePetAll( // 進化
							highpetamu.getId());
					
					if(npcId!=0){//進化成功
						pc.sendPackets(new S_SystemMessage("恭喜您！寵物進化成"+
								NpcTable.getInstance().newNpcInstance(npcId).getName()+
								"此寵物等級:"+NpcTable.getInstance().newNpcInstance(npcId).getLevel()));
						pc.sendPackets(new S_ItemName(highpetamu));
						inv.removeItem(petamu, 1);
					}else{
						inv.removeItem(highpetamu);
						pc.sendPackets(new S_SystemMessage("進化失敗！寵物進化等級達到頂點LV:"+Config.MAX_CE_PET_LEVEL+"！"));
						return;
					}
				}else if(petType.canEvolve()){
					pet.evolvePet( // 進化させる
							highpetamu.getId());
					pc.sendPackets(new S_ItemName(highpetamu));
					inv.removeItem(petamu, 1);
				}else{
					inv.removeItem(highpetamu);
				}
				//正常寵物
				//~所有怪物都可抓進化
			}
		}
	}

	private boolean isTamePet(L1NpcInstance npc) {
		boolean isSuccess = false;
		int npcId = npc.getNpcTemplate().get_npcId();
		if (npcId == 45313) { // タイガー
			if (npc.getMaxHp() / 3 > npc.getCurrentHp() // HPが1/3未満で1/16の確率
					&& _random.nextInt(16) == 15) {
				isSuccess = true;
			}
		} else {
			if (npc.getMaxHp() / 3 > npc.getCurrentHp()) {
				isSuccess = true;
			}
		}

		if (npcId == 45313 || npcId == 45044 || npcId == 45711) { // タイガー、ラクーン、紀州犬の子犬
			if (npc.isResurrect()) { // RES後はテイム不可
				isSuccess = false;
			}
		}

		return isSuccess;
	}

	@Override
	public String getType() {
		return C_GIVE_ITEM;
	}
}
