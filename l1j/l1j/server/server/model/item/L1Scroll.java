package l1j.server.server.model.item;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import l1j.server.server.ClientThread;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1TownLocation;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1EtcItem;

public class L1Scroll {
	public L1Scroll(){
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId,int btele) throws Exception {
		L1BookMark bookm = pc.getBookMark(btele);
		if (bookm != null) {
			if (pc.getMap().isEscapable() || pc.isGm()) {
				int newX = bookm.getLocX();
				int newY = bookm.getLocY();
				short mapId = bookm.getMapId();

				if (itemId == 40086) { // マステレポートスクロール
					for (L1PcInstance member : L1World.getInstance().getVisiblePlayer(pc)) {
						if (pc.getLocation().getTileLineDistance(member.getLocation()) <= 3 && member.getClanid() == pc.getClanid() && pc.getClanid() != 0 && member.getId() != pc.getId()) {
							L1Teleport.teleport(member, newX, newY, mapId, 5, true);
						}
					}
				}
				L1Teleport.teleport(pc, newX, newY, mapId, 5, true);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				L1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getHeading(), false);
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else {
			int locX = ((L1EtcItem) l1iteminstance.getItem()).get_locx();
			int locY = ((L1EtcItem) l1iteminstance.getItem()).get_locy();
			short mapId = ((L1EtcItem) l1iteminstance.getItem()).get_mapid();
			
			int[] itemLoc = getLocFromItem(pc, itemId);
			if (itemLoc[0] == -1) {
				// 無法使用
				return;
			}
			
			if (itemLoc[0] > 0 && itemLoc[1] > 0) {
				locX = itemLoc[0];
				locY = itemLoc[1];
				mapId = (short)itemLoc[2];
			}
			
			if (locX == 0 && locY == 0) {
				// 一般瞬間移動卷軸
				L1Location newLocation = pc.getLocation().randomLocation(200, true);
				locX = newLocation.getX();
				locY = newLocation.getY();
				mapId = (short) newLocation.getMapId();
			}
			if (pc.getMap().isTeleportable() || pc.isGm()) {
				if (itemId == 40086) { // マステレポートスクロール
					for (L1PcInstance member : L1World.getInstance().getVisiblePlayer(pc)) {
						if (pc.getLocation().getTileLineDistance(member.getLocation()) <= 3 && member.getClanid() == pc.getClanid() && pc.getClanid() != 0 && member.getId() != pc.getId()) {
							L1Teleport.teleport(member, locX, locY, mapId, 5, true);
						}
					}
				}
				L1Teleport.teleport(pc, locX, locY, mapId, 5, true);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				//傳送位移對策:使用S_Paralysis.TYPE_TELEPORT
				//延伸問題:在傳送期間再次使用會使畫面卡住
				//延伸問題對策:於鎖定期間再次使用item的地方解除鎖定，因此道具鎖定時間需大於Config.TELEPORT_LOCK_TIME。
				pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT, false));
				pc.sendPackets(new S_ServerMessage(276));
			}
		}
		cancelAbsoluteBarrier(pc); 
	}
	
	private int[] getLocFromItem(L1PcInstance pc, int itemId) {
		if (itemId == 40079 || itemId == 40095){
			// 傳送回家眷軸 & 象牙塔傳送回家的卷軸
			return Getback.GetBack_Location(pc, true);
		} else if (itemId == 40124) {
			// 血盟傳送卷軸
			int castle_id = 0;
			int house_id = 0;
			if (pc.getClanid() != 0) {
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					castle_id = clan.getCastleId();
					house_id = clan.getHouseId();
				}
			}
			if (castle_id != 0) {
				// 主城
				if (pc.getMap().isEscapable() || pc.isGm()) {
					return L1CastleLocation.getCastleLoc(castle_id);
				} else {
					pc.sendPackets(new S_ServerMessage(647));
					return new int[]{-1};
				}
			} else if (house_id != 0) {
				// 血盟小屋
				if (pc.getMap().isEscapable() || pc.isGm()) {
					return L1HouseLocation.getHouseLoc(house_id);
				} else {
					pc.sendPackets(new S_ServerMessage(647));
					return new int[]{-1};
				}
			} else {
				if (pc.getHomeTownId() > 0) {
					return L1TownLocation.getGetBackLoc(pc.getHomeTownId());
				} else {
					return Getback.GetBack_Location(pc, true);
				}
			}
		}
		return new int[]{0, 0, 0};
	}
	
	private void cancelAbsoluteBarrier(L1PcInstance pc) { // アブソルート バリアの解除
		if (pc.hasSkillEffect(ABSOLUTE_BARRIER)) {
			pc.killSkillEffectTimer(ABSOLUTE_BARRIER);
			pc.startHpRegeneration();
			pc.startMpRegeneration();
			pc.startMpRegenerationByDoll();
			pc.startHpRegenerationByDoll();//add 魔法娃娃回血功能
		}
	}
}
