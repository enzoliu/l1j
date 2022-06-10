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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.Account;
import l1j.server.server.BadNamesList;
import l1j.server.server.ClientThread;
import l1j.server.server.IdFactory;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.Beginner;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_AddSkill;
import l1j.server.server.serverpackets.S_CharCreateStatus;
import l1j.server.server.serverpackets.S_NewCharPacket;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.CalcInitHpMp;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_CreateChar extends ClientBasePacket {

	private static Logger _log = Logger.getLogger(C_CreateChar.class.getName());
	private static final String C_CREATE_CHAR = "[C] C_CreateChar";
	// 新創角色能力最小值
	private static final int[] ORIGINAL_STR
			= new int[] { 13, 16, 11, 8, 12, 13, 11 };
	private static final int[] ORIGINAL_DEX
			= new int[] { 10, 12, 12, 7, 15, 11, 10 };
	private static final int[] ORIGINAL_CON
			= new int[] { 10, 14, 12, 12, 8, 14, 12 };
	private static final int[] ORIGINAL_WIS
			= new int[] { 11, 9, 12, 12, 10, 12, 12 };
	private static final int[] ORIGINAL_CHA
			= new int[] { 13, 12, 9, 8, 9, 8, 8 };
	private static final int[] ORIGINAL_INT
			= new int[] { 10, 8, 12, 12, 11, 11, 12 };
	// 新創角色點數分配
	private static final int[] ORIGINAL_AMOUNT
			= new int[] { 8, 4, 7, 16, 10, 6, 10 };

	private static final String CLIENT_LANGUAGE_CODE = Config
			.CLIENT_LANGUAGE_CODE;

	public C_CreateChar(byte[] abyte0, ClientThread client)
			throws Exception {
		super(abyte0);
		L1PcInstance pc = new L1PcInstance();
		String name = readS();

		Account account = Account.load(client.getAccountName());
		int characterSlot = account.getCharacterSlot();
		int maxAmount = Config.DEFAULT_CHARACTER_SLOT + characterSlot;

		name = name.replaceAll("\\s", "");
		name = name.replaceAll("　", "");
		if (name.length() == 0) {
			S_CharCreateStatus s_charcreatestatus = new S_CharCreateStatus(
					S_CharCreateStatus.REASON_INVALID_NAME);
			client.sendPacket(s_charcreatestatus);
			return;
		}

		if (isInvalidName(name)) {
			S_CharCreateStatus s_charcreatestatus = new S_CharCreateStatus(
					S_CharCreateStatus.REASON_INVALID_NAME);
			client.sendPacket(s_charcreatestatus);
			return;
		}

		if (CharacterTable.doesCharNameExist(name)) {
			_log.fine("charname: " + pc.getName()
					+ " already exists. creation failed.");
			S_CharCreateStatus s_charcreatestatus1 = new S_CharCreateStatus(
					S_CharCreateStatus.REASON_ALREADY_EXSISTS);
			client.sendPacket(s_charcreatestatus1);
			return;
		}
		
		if (client.getAccount().countCharacters() >= maxAmount) {
			_log.fine("帳號: " + client.getAccountName()
					+ " 建立超過"+maxAmount+"個角色人物。");
			S_CharCreateStatus s_charcreatestatus1 = new S_CharCreateStatus(
					S_CharCreateStatus.REASON_WRONG_AMOUNT);
			client.sendPacket(s_charcreatestatus1);
			return;
		}

		pc.setName(name);
		pc.setType(readC());
		pc.set_sex(readC());
		pc.addBaseStr((byte) readC());
		pc.addBaseDex((byte) readC());
		pc.addBaseCon((byte) readC());
		pc.addBaseWis((byte) readC());
		pc.addBaseCha((byte) readC());
		pc.addBaseInt((byte) readC());

		boolean isStatusError = false;
		int originalStr = ORIGINAL_STR[pc.getType()];
		int originalDex = ORIGINAL_DEX[pc.getType()];
		int originalCon = ORIGINAL_CON[pc.getType()];
		int originalWis = ORIGINAL_WIS[pc.getType()];
		int originalCha = ORIGINAL_CHA[pc.getType()];
		int originalInt = ORIGINAL_INT[pc.getType()];
		int originalAmount = ORIGINAL_AMOUNT[pc.getType()];

		if ((pc.getBaseStr() < originalStr
				|| pc.getBaseDex() < originalDex
				|| pc.getBaseCon() < originalCon
				|| pc.getBaseWis() < originalWis
				|| pc.getBaseCha() < originalCha
				|| pc.getBaseInt() < originalInt)
				|| (pc.getBaseStr() > originalStr + originalAmount
				|| pc.getBaseDex() > originalDex + originalAmount
				|| pc.getBaseCon() > originalCon + originalAmount
				|| pc.getBaseWis() > originalWis + originalAmount
				|| pc.getBaseCha() > originalCha + originalAmount
				|| pc.getBaseInt() > originalInt + originalAmount)) {
			isStatusError = true;
		}

		int statusAmount = pc.getDex() + pc.getCha() + pc.getCon()
				+ pc.getInt() + pc.getStr() + pc.getWis();

		if (statusAmount != 75 || isStatusError) {
			_log.finest("帳號:"+pc.getName()+" 腳色能力質有問題!");
			S_CharCreateStatus s_charcreatestatus3 = new S_CharCreateStatus(
					S_CharCreateStatus.REASON_WRONG_AMOUNT);
			client.sendPacket(s_charcreatestatus3);
			return;
		}

		_log.fine("角色名稱: " + pc.getName() + " 職業: "
				+ pc.getClassId());
		S_CharCreateStatus s_charcreatestatus2 = new S_CharCreateStatus(
				S_CharCreateStatus.REASON_OK);
		client.sendPacket(s_charcreatestatus2);
		initNewChar(client, pc);
	}

	private static final int[] MALE_LIST = new int[] { 0, 61, 138, 734, 2786, 6658, 6671 };
	private static final int[] FEMALE_LIST = new int[] { 1, 48, 37, 1186, 2796, 6661, 6650 };
	/*private static final int[] LOCX_LIST = new int[] { 32734, 32734, 32734, 32734, 32734, 32734, 32734 };
	private static final int[] LOCY_LIST = new int[] { 32798, 32798, 32798, 32798, 32798, 32798, 32798 };
	private static final short[] MAPID_LIST = new short[] { 8013, 8013, 8013, 8013, 8013, 8013, 8013 };*/
	//TODO 各職業出生地改回台版　by 阿傑
	private static final int[] LOCX_LIST = new int[] { 32780, 32714, 33043, 32780, 32924, 32714, 32714 };
	private static final int[] LOCY_LIST = new int[] { 32781, 32877, 32336, 32781, 32799, 32877, 32877 };
	private static final short[] MAPID_LIST = new short[] { 68, 69, 4, 68, 304, 69, 69 };
	//end
// private static final int[] LOCX_LIST = new int[] { 32780, 32714, 32714,
// 32780, 32714, 32714, 32714 };
// private static final int[] LOCY_LIST = new int[] { 32781, 32877, 32877,
// 32781, 32877, 32877, 32877 };
// private static final short[] MAPID_LIST = new short[] { 68, 69, 69, 68, 69,
// 69, 69 };

	private static void initNewChar(ClientThread client, L1PcInstance pc)
			throws IOException, Exception {

		pc.setId(IdFactory.getInstance().nextId());
		if (pc.get_sex() == 0) {
			pc.setClassId(MALE_LIST[pc.getType()]);
		} else {
			pc.setClassId(FEMALE_LIST[pc.getType()]);
		}
		pc.setX(LOCX_LIST[pc.getType()]);
		pc.setY(LOCY_LIST[pc.getType()]);
		pc.setMap(MAPID_LIST[pc.getType()]);
		pc.setHeading(0);
		pc.setLawful(0);

		int initHp = CalcInitHpMp.calcInitHp(pc);
		int initMp = CalcInitHpMp.calcInitMp(pc);
		pc.addBaseMaxHp((short) initHp);
		pc.setCurrentHp((short) initHp);
		pc.addBaseMaxMp((short) initMp);
		pc.setCurrentMp((short) initMp);
		pc.resetBaseAc();
		pc.setTitle("");
		pc.setClanid(0);
//		 TODO 玩家誕生了 by terry0412 
		 String msg = "";  
		 if (pc.isCrown())//王族
         msg = "君王"; 
        else if (pc.isKnight()) //騎士
         msg = "劍士";
        else if (pc.isElf())//妖精
         msg = "精靈";
        else if (pc.isWizard())//法師
         msg = "巫師"; 
        else if (pc.isDarkelf())//黑妖
         msg = "刺客";
        else if (pc.isDragonKnight()) // TODO　新增龍騎士  by 0968026609
            msg = "龍騎士";
        else if (pc.isIllusionist())// TODO　新增幻術師  by 0968026609
            msg = "幻術師"; 
		for (L1PcInstance allpc : L1World.getInstance().getAllPlayers()) {
		if (allpc != null) {
		allpc.sendPackets(new S_SystemMessage("玩家【"+pc.getName()+"】誕生了，職業：【"+msg+"】"));
		 }  
		  } 
//      end
		pc.setClanRank(0);
		pc.set_food(40);
		pc.setAccessLevel((short) 0);
		pc.setGm(false);
		pc.setMonitor(false);
		pc.setGmInvis(false);
		pc.setExp(0);
		pc.setHighLevel(0);
		pc.setStatus(0);
		pc.setAccessLevel((short) 0);
		pc.setClanname("");
		pc.setBonusStats(0);
		pc.setElixirStats(0);
		pc.resetBaseMr();
		pc.setElfAttr(0);
		pc.set_PKcount(0);
		pc.setExpRes(0);
		pc.setPartnerId(0);
		pc.setOnlineStatus(0);
		pc.setHomeTownId(0);
		pc.setContribution(0);
		pc.setBanned(false);
		pc.setKarma(0);
		if (pc.isWizard()) { // WIZ
			pc.sendPackets(new S_AddSkill(3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
			int object_id = pc.getId();
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(4); // EB
			String skill_name = l1skills.getName();
			int skill_id = l1skills.getSkillId();
			SkillsTable.getInstance().spellMastery(object_id, skill_id,
					skill_name, 0, 0); // DBに登録
		}
		Beginner.getInstance().GiveItem(pc);
		pc.setAccountName(client.getAccountName());
		CharacterTable.getInstance().storeNewCharacter(pc);
		S_NewCharPacket s_newcharpacket = new S_NewCharPacket(pc);
		client.sendPacket(s_newcharpacket);
		//fix by sdcom 2010.9.27
		CharacterTable.saveCharStatus(pc);
		pc.refresh();
	}

	private static boolean isAlphaNumeric(String s) {
		boolean flag = true;
		char ac[] = s.toCharArray();
		int i = 0;
		do {
			if (i >= ac.length) {
				break;
			}
			if (!Character.isLetterOrDigit(ac[i])) {
				flag = false;
				break;
			}
			i++;
		} while (true);
		return flag;
	}

	private static boolean isInvalidName(String name) {
		int numOfNameBytes = 0;
		try {
			numOfNameBytes = name.getBytes(CLIENT_LANGUAGE_CODE).length;
		} catch (UnsupportedEncodingException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return false;
		}

		if (isAlphaNumeric(name)) {
			return false;
		}

		// XXX - 本鯖の仕様と同等か未確認
		// 全角文字が5文字を超えるか、全体で12バイトを超えたら無効な名前とする
		if (5 < (numOfNameBytes - name.length()) || 12 < numOfNameBytes) {
			return false;
		}

		if (BadNamesList.getInstance().isBadName(name)) {
			return false;
		}
		return true;
	}

	@Override
	public String getType() {
		return C_CREATE_CHAR;
	}
}
