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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;


public class C_DropItem extends ClientBasePacket {
	private static final String C_DROP_ITEM = "[C] C_DropItem";

	public C_DropItem(byte[] decrypt, ClientThread client)
			throws Exception {
		super(decrypt);
		int x = readH();
		int y = readH();
		int objectId = readD();
		int count = readD();

		L1PcInstance pc = client.getActiveChar();
		if (pc.isGhost()) {
			return;
		}

		L1ItemInstance item = pc.getInventory().getItem(objectId);
		if (item != null) {
			if (!item.getItem().isTradable()) {
				// \f1%0は捨てたりまたは他人に讓ることができません。
				pc.sendPackets(new S_ServerMessage(210, item.getItem()
						.getName()));
				return;
			}

			Object[] petlist = pc.getPetList().values().toArray();
			for (Object petObject : petlist) {
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
			//TODO 物品丟地上系統會自動刪除 by bill00148 
			if(Config.Drop_Item && !pc.isGm()
				&& pc.getLevel() <= Config.DropItemMinLv){ 
				pc.getInventory().removeItem(objectId,count); 
				pc.sendPackets(new S_SystemMessage("物品請勿丟地上，已遭系統刪除。"));
				return;
			}

			//end
			if (item.isEquipped()) {
				// \f1削除できないアイテムや装備しているアイテムは捨てられません。
				pc.sendPackets(new S_ServerMessage(125));
				return;
			}
			if (item.getBless() >= 128) { // 封印された装備
				// \f1%0は捨てたりまたは他人に讓ることができません。
				pc.sendPackets(new S_ServerMessage(210, item.getItem()
						.getName()));
				return;
			}
			//TODO 丟棄物品記錄 by bill00148 
			dropitem("IP"
					+ "(" + pc.getNetConnection().getIp() + ")"
					+"玩家"
					+ ":【" + pc.getName() + "】 "
					+ "的" 
					+ "【+" + item.getEnchantLevel()
					+ " " + item.getName() + 
					"(" + count + ")" + "】"
					+ " 丟棄到地上,"
					+ "時間:" + "(" + new Timestamp(System.currentTimeMillis()) + ")。");
			//end

			pc.getInventory().tradeItem(item, count,
					L1World.getInstance().getInventory(x, y, pc.getMapId()));
			pc.turnOnOffLight();
		}
	}
	
    //TODO 記錄文件檔  by 阿傑
    public static void dropitem(String info) {
    	try {
    		BufferedWriter out = new BufferedWriter(new FileWriter("dropitem.txt", true));
    		out.write(info + "\r\n");
    		out.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

	@Override
	public String getType() {
		return C_DROP_ITEM;
	}
}
