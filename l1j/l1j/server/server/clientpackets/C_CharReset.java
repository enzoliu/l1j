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

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CharReset;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.utils.CalcInitHpMp;
import l1j.server.server.utils.CalcStat;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_CharReset extends ClientBasePacket {

	private static final String C_CHAR_RESET = "[C] C_CharReset";

/**
 * //配置完初期點數 按確定 127.0.0.1 Request Work ID : 120 0000: 78 01 0d 0a 0b 0a 12 0d
 * 
 * //提升10及 127.0.0.1 Request Work ID : 120 0000: 78 02 07 00 //提升1及 127.0.0.1
 * Request Work ID : 120 0000: 78 02 00 04
 * 
 * //提升完等級 127.0.0.1 Request Work ID : 120 0000: 78 02 08 00 x...
 * 
 * //萬能藥 127.0.0.1 Request Work ID : 120 0000: 78 03 23 0a 0b 17 12 0d
 */	

	public C_CharReset(byte abyte0[], ClientThread clientthread) {
		super(abyte0);
		
		// 新創角色能力最小值
		final int[] ORIGINAL_STR
				= new int[] { 13, 16, 11, 8, 12, 13, 11 };
		final int[] ORIGINAL_DEX
				= new int[] { 10, 12, 12, 7, 15, 11, 10 };
		final int[] ORIGINAL_CON
				= new int[] { 10, 14, 12, 12, 8, 14, 12 };
		final int[] ORIGINAL_WIS
				= new int[] { 11, 9, 12, 12, 10, 12, 12 };
		final int[] ORIGINAL_CHA
				= new int[] { 13, 12, 9, 8, 9, 8, 8 };
		final int[] ORIGINAL_INT
				= new int[] { 10, 8, 12, 12, 11, 11, 12 };
		// 新創角色點數分配
		final int[] ORIGINAL_AMOUNT
				= new int[] { 8, 4, 7, 16, 10, 6, 10 };

		
		L1PcInstance pc = clientthread.getActiveChar();
		
		//不是重置狀態
		if(!pc.isInCharReset()){
			return;
		}
		
		int stage = readC();

		if (stage == 0x01) { // 0x01:キャラクター初期化
			int str = readC();
			int intel = readC();
			int wis = readC();
			int dex = readC();
			int con = readC();
			int cha = readC();
			
			boolean isStatusError = false;
			int originalStr = ORIGINAL_STR[pc.getType()];
			int originalDex = ORIGINAL_DEX[pc.getType()];
			int originalCon = ORIGINAL_CON[pc.getType()];
			int originalWis = ORIGINAL_WIS[pc.getType()];
			int originalCha = ORIGINAL_CHA[pc.getType()];
			int originalInt = ORIGINAL_INT[pc.getType()];
			int originalAmount = ORIGINAL_AMOUNT[pc.getType()];

			if (str < originalStr
					|| dex < originalDex
					|| con < originalCon
					|| wis < originalWis
					|| cha < originalCha
					|| intel < originalInt
					|| (str > originalStr + originalAmount
					|| dex > originalDex + originalAmount
					|| con > originalCon + originalAmount
					|| wis > originalWis + originalAmount
					|| cha > originalCha + originalAmount
					|| intel > originalInt + originalAmount)) {
				isStatusError = true;
			}
			
			
			//加入初始化判斷
			int statusAmount = str + intel + wis
				+ dex + con + cha;
			if (statusAmount != 75 || isStatusError) {
				L1World.getInstance().broadcastServerMessage(pc.getName()+"玩家 回憶蠟燭初始能力有問題!");
				pc.sendPackets(new S_Disconnect());
				return;
			}
			//~加入初始化判斷
			
			int hp = CalcInitHpMp.calcInitHp(pc);
			int mp = CalcInitHpMp.calcInitMp(pc);
			pc.sendPackets(new S_CharReset(pc, 1, hp, mp, 10, str, intel, wis,
					dex, con, cha));
			initCharStatus(pc, hp, mp, str, intel, wis, dex, con, cha);
			CharacterTable.saveCharStatus(pc);
		} else if (stage == 0x02) { // 0x02:ステータス再分配
			int type2 = readC();
			if (type2 == 0x00) { // 0x00:Lv1UP
				setLevelUp(pc, 1); 
			} else if (type2 == 0x07) { // 0x07:Lv10UP
				if (pc.getTempMaxLevel() - pc.getTempLevel() < 10) {
					return;
				}
				setLevelUp(pc,10);
			} else if (type2 >= 0x01 && type2<=0x06) {
				if(!checkMaxAttr(pc, type2)){
					return;
				}
				setLevelUp(pc, 1);
			} else if (type2 == 0x08) {
				if(!checkMaxAttr(pc, readC())){
					return;
				}
				/*
				switch(readC()){
				case 1:
					pc.addBaseStr((byte) 1);
					break;
				case 2:
					pc.addBaseInt((byte) 1);
					break;
				case 3:
					pc.addBaseWis((byte) 1);
					break;
				case 4:
					pc.addBaseDex((byte) 1);
					break;
				case 5:
					pc.addBaseCon((byte) 1);
					break;
				case 6:
					pc.addBaseCha((byte) 1);
					break;
				}
				*/
				if (pc.getElixirStats() > 0) {
					pc.sendPackets(new S_CharReset(pc.getElixirStats()));
					return;
				}
				saveNewCharStatus(pc);
				//修正重置完 登出無法登錄問題
				pc.setInCharReset(false);
				//~修正重置完 登出無法登錄問題
			}
		} else if (stage == 0x03) {
			int str = readC();
			int intel = readC();
			int wis = readC();
			int dex = readC();
			int con = readC();
			int cha = readC();
			int statusAmount = str + intel + wis
			+ dex + con + cha;
			if(!checkMaxAttrForTwo(str)){
				return;
			}
			if(!checkMaxAttrForTwo(intel)){
				return;
			}
			if(!checkMaxAttrForTwo(wis)){
				return;
			}
			if(!checkMaxAttrForTwo(dex)){
				return;
			}
			if(!checkMaxAttrForTwo(con)){
				return;
			}
			if(!checkMaxAttrForTwo(cha)){
				return;
			}
			if((75+pc.getElixirStats()+pc.getBonusStats())!= statusAmount){
				return;
			}
			
			pc.addBaseStr((byte) (str - pc.getBaseStr()));
			pc.addBaseInt((byte) (intel - pc.getBaseInt()));
			pc.addBaseWis((byte) (wis - pc.getBaseWis()));
			pc.addBaseDex((byte) (dex - pc.getBaseDex()));
			pc.addBaseCon((byte) (con - pc.getBaseCon()));
			pc.addBaseCha((byte) (cha - pc.getBaseCha()));
			pc.setInCharReset(false);
			saveNewCharStatus(pc);
		}
	}
	
	private boolean checkMaxAttr(L1PcInstance pc,int d){
		int attr=1;
		switch(d){
		case 1:
			attr+=pc.getBaseStr();
			break;
		case 2:
			attr+=pc.getBaseInt();
			break;
		case 3:
			attr+=pc.getBaseWis();
			break;
		case 4:
			attr+=pc.getBaseDex();
			break;
		case 5:
			attr+=pc.getBaseCon();
			break;
		case 6:
			attr+=pc.getBaseCha();
			break;
		}
		if(attr>Config.MAX_ABILITY){
			return false;
		}
		
		switch(d){
		case 1:
			pc.addBaseStr((byte) 1);
			break;
		case 2:
			pc.addBaseInt((byte) 1);
			break;
		case 3:
			pc.addBaseWis((byte) 1);
			break;
		case 4:
			pc.addBaseDex((byte) 1);
			break;
		case 5:
			pc.addBaseCon((byte) 1);
			break;
		case 6:
			pc.addBaseCha((byte) 1);
			break;
		}
		
		return true;
	}
	
	private boolean checkMaxAttrForTwo(int attr){
		if(attr>Math.max(Config.MAX_ABILITY3,Config.MAX_ABILITY)){
			return false;
		}
		return true;
	}

	private void saveNewCharStatus(L1PcInstance pc) {
		//pc.setInCharReset(false);
		if(pc.getOriginalAc() > 0) {
			pc.addAc(pc.getOriginalAc());
		}
		if(pc.getOriginalMr() > 0) {
			pc.addMr(0 - pc.getOriginalMr());
		}
		pc.refresh();
		pc.setCurrentHp(pc.getMaxHp());
		pc.setCurrentMp(pc.getMaxMp());
		if (pc.getTempMaxLevel() != pc.getLevel()) {
			pc.setLevel(pc.getTempMaxLevel());
			pc.setExp(ExpTable.getExpByLevel(pc.getTempMaxLevel()));
		}
		if (pc.getLevel() > 50) {
			pc.setBonusStats(pc.getLevel() - 50);
		}else{
			pc.setBonusStats(0);
		}
		pc.sendPackets(new S_OwnCharStatus(pc));
		/*
		L1ItemInstance item = pc.getInventory().findItemId(49142); // 回憶蠟燭
		if (item != null) {
			try {
				pc.getInventory().removeItem(item, 1);
				pc.save(); // DBにキャラクター情報を書き込む
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		*/
		L1Teleport.teleport(pc, 32628, 32772, (short) 4, 4, false);
	}

	private void initCharStatus(L1PcInstance pc, int hp, int mp, int str,
			int intel, int wis, int dex, int con, int cha) {
		pc.addBaseMaxHp((short)(hp - pc.getBaseMaxHp()));
		pc.addBaseMaxMp((short)(mp - pc.getBaseMaxMp()));
		pc.addBaseStr((byte)(str - pc.getBaseStr()));
		pc.addBaseInt((byte)(intel - pc.getBaseInt()));
		pc.addBaseWis((byte)(wis - pc.getBaseWis()));
		pc.addBaseDex((byte)(dex - pc.getBaseDex()));
		pc.addBaseCon((byte)(con - pc.getBaseCon()));
		pc.addBaseCha((byte)(cha - pc.getBaseCha()));
		pc.addMr(0 - pc.getMr());
    	pc.addDmgup(0 - pc.getDmgup());
    	pc.addHitup(0 - pc.getHitup());
	}

	private boolean setLevelUp(L1PcInstance pc ,int addLv) {
		if((pc.getTempLevel()+ addLv) > pc.getLevel()){
			return false;
		}
		pc.setTempLevel(pc.getTempLevel()+ addLv);
		for (int i = 0; i < addLv; i++) {
			short randomHp = CalcStat.calcStatHp(pc.getType(),
					pc.getBaseMaxHp(), pc.getBaseCon(), pc.getOriginalHpup());
			short randomMp = CalcStat.calcStatMp(pc.getType(),
					pc.getBaseMaxMp(), pc.getBaseWis(), pc.getOriginalMpup());
			pc.addBaseMaxHp(randomHp);
			pc.addBaseMaxMp(randomMp);
		}
		int newAc = CalcStat.calcAc(pc.getTempLevel(), pc.getBaseDex());
		pc.sendPackets(new S_CharReset(pc,pc.getTempLevel(),
				pc.getBaseMaxHp(), pc.getBaseMaxMp(), newAc,
				pc.getBaseStr(), pc.getBaseInt(), pc.getBaseWis(),
				pc.getBaseDex(), pc.getBaseCon(), pc.getBaseCha()));
		return true;
	}

	@Override
	public String getType() {
		return C_CHAR_RESET;
	}

}
