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
package l1j.server.server.serverpackets;

import java.io.IOException;
import java.util.List;

import l1j.server.Config;
import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.shop.L1AssessedItem;
import l1j.server.server.model.shop.L1Shop;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.serverpackets.S_NoSell;

public class S_ScrollShopBuyList extends ServerBasePacket {


	/**
	 * 店の品物リストを表示する。キャラクターがBUYボタンを押した時に送る。
	 */
	public S_ScrollShopBuyList(L1Npc npc, L1PcInstance pc) {
		int objId=L1World.getInstance().getObjId(npc);
		L1Object npcObj=L1World.getInstance().findObject(objId);
		L1NpcInstance npcins = (L1NpcInstance) npcObj;
		int npcId = npcins.getNpcTemplate().get_npcId();
		L1Shop shop = ShopTable.getInstance().get(npcId);
		
		//新所有物品販賣 by eric1300460
		List<L1AssessedItem> assessedItems;
		if(Config.AllSell){
			assessedItems = shop.allAssessItems(pc.getInventory());
			if (assessedItems.isEmpty()) {
				pc.sendPackets(new S_NoSell(npcins));
				return;
			}
		}else{
			if (shop == null) {
				pc.sendPackets(new S_NoSell(npcins));
				return;
			}

			assessedItems = shop.assessItems(pc.getInventory());
			if (assessedItems.isEmpty()) {
				pc.sendPackets(new S_NoSell(npcins));
				return;
			}
		}
		//~新所有物品販賣 by eric1300460
		writeC(Opcodes.S_OPCODE_SHOWSHOPSELLLIST);
		writeD(objId);
		writeH(assessedItems.size());

		for (L1AssessedItem item : assessedItems) {
			writeD(item.getTargetId());
			writeD(item.getAssessedPrice());
		}
	}

	@Override
	public byte[] getContent() throws IOException {
		return _bao.toByteArray();
	}
}
