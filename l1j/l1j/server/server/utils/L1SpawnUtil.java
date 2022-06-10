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
package l1j.server.server.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.IdFactory;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.model.L1NpcDeleteTimer;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
//新增怪物隨機地圖產生  by eric1300460
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.L1Character;
import l1j.server.server.datatables.MapsTable;
import l1j.eric.RandomMobDeleteTimer;
import l1j.eric.RandomMobTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
//~新增怪物隨機地圖產生  by eric1300460
public class L1SpawnUtil {
	private static Logger _log = Logger.getLogger(L1SpawnUtil.class.getName());

	public static void spawn(L1PcInstance pc, int npcId, int randomRange,
			int timeMillisToDelete) {
		try {
			L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(npcId);
			npc.setId(IdFactory.getInstance().nextId());
			npc.setMap(pc.getMapId());
			if (randomRange == 0) {
				npc.getLocation().set(pc.getLocation());
				npc.getLocation().forward(pc.getHeading());
			} else {
				int tryCount = 0;
				do {
					tryCount++;
					npc.setX(pc.getX() + (int) (Math.random() * randomRange)
							- (int) (Math.random() * randomRange));
					npc.setY(pc.getY() + (int) (Math.random() * randomRange)
							- (int) (Math.random() * randomRange));
					if (npc.getMap().isInMap(npc.getLocation())
							&& npc.getMap().isPassable(npc.getLocation())) {
						break;
					}
					Thread.sleep(1);
				} while (tryCount < 50);

				if (tryCount >= 50) {
					npc.getLocation().set(pc.getLocation());
					npc.getLocation().forward(pc.getHeading());
				}
			}

			npc.setHomeX(npc.getX());
			npc.setHomeY(npc.getY());
			npc.setHeading(pc.getHeading());

			L1World.getInstance().storeObject(npc);
			L1World.getInstance().addVisibleObject(npc);

			npc.turnOnOffLight();
			npc.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
			if (0 < timeMillisToDelete) {
				L1NpcDeleteTimer timer = new L1NpcDeleteTimer(npc,
						timeMillisToDelete);
				timer.begin();
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	//新增怪物隨機地圖產生  by eric1300460
	public static void spawn(int randomMobId) {
		try {
			RandomMobTable mobTable = RandomMobTable.getInstance();
			int mobCont=mobTable.getCont(randomMobId);
			int mobId = mobTable.getMobId(randomMobId);
			L1NpcInstance [] npc =new L1NpcInstance[mobCont];
			for(int i=0;i<mobCont;i++){
				short mapId=mobTable.getRandomMapId(randomMobId);
				int x=mobTable.getRandomMapX(mapId);
				int y=mobTable.getRandomMapY(mapId);
				npc[i] = NpcTable.getInstance().newNpcInstance(mobId);
				npc[i].setId(IdFactory.getInstance().nextId());
				npc[i].setMap(mapId);
				
				int tryCount = 0;
				do {
					tryCount++;
					Random rand= new Random();
					npc[i].setX(x+rand.nextInt(200)-rand.nextInt(200));
					npc[i].setY(y+rand.nextInt(200)-rand.nextInt(200));
					
					L1Map map = npc[i].getMap();
					if (map.isInMap(npc[i].getLocation())
							&& map.isPassable(npc[i].getLocation())) {
						break;
					}
					
					Thread.sleep(1);
				} while (tryCount < 50);
				if (tryCount >= 50) {
					boolean find=false;
					L1PcInstance findPc;
					for (Object obj : L1World.getInstance().getVisibleObjects(mapId)
							.values()) {
						if (obj instanceof L1PcInstance)
						{
							findPc=(L1PcInstance)obj;
							npc[i].getLocation().set(findPc.getLocation());
							npc[i].getLocation().forward(findPc.getHeading());
							find=true;
							break;
						}
					}
					if(!find){//隨機怪物創造失敗
						continue;
					}
				}
				
				
				npc[i].setHomeX(npc[i].getX());
				npc[i].setHomeY(npc[i].getY());
				npc[i].setHeading(0);
				L1World.getInstance().storeObject(npc[i]);
				L1World.getInstance().addVisibleObject(npc[i]);
				npc[i].turnOnOffLight();
				npc[i].startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE);
				
				L1World.getInstance().broadcastServerMessage("【"+npc[i].getName()+"】出現在"+MapsTable.getInstance().getLoaction(mapId)+
						" MapID="+mapId+" X="+npc[i].getX()+" Y="+npc[i].getY());
				//System.out.println("【"+npc[i].getName()+"】出現在"+MapsTable.getInstance().getLoaction(mapId)+
				//		"  MapID="+mapId+" X="+npc[i].getX()+" Y="+npc[i].getY());
			}
			int timeSecondToDelete = mobTable.getTimeSecondToDelete(randomMobId);
			if (timeSecondToDelete >0) {
				RandomMobDeleteTimer timer = new RandomMobDeleteTimer(
						randomMobId,npc);
				timer.begin();
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	//~新增怪物隨機地圖產生  by eric1300460
}
