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
import l1j.server.server.GMCommands;
import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ChatLogTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import static l1j.server.server.model.skill.L1SkillId.*;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Chat extends ClientBasePacket {

	private static final String C_CHAT = "[C] C_Chat";

	public C_Chat(byte abyte0[], ClientThread clientthread) {
		super(abyte0);

		L1PcInstance pc = clientthread.getActiveChar();
		int chatType = readC();
		String chatText = readS();
		if (pc.hasSkillEffect(SILENCE)
				|| pc.hasSkillEffect(AREA_OF_SILENCE)
				|| pc.hasSkillEffect(STATUS_POISON_SILENCE)) {
			return;
		}
		if (pc.hasSkillEffect(1005)) { // チャット禁止中
			pc.sendPackets(new S_ServerMessage(242)); // 現在チャット禁止中です。
			return;
		}

		if (chatType == 0) { // 通常チャット
			if (pc.isGhost() && !(pc.isGm() || pc.isMonitor())) {
				return;
			}
			
			// GMコマンド
			//TODO 修正一般玩家輸入"."顯示指令錯誤問題
			//if (chatText.startsWith(".")) {
			if (chatText.startsWith(".") && (pc.getAccessLevel()>0 || Config.ALL_GM)) { 
			//end
				String cmd = chatText.substring(1);
				GMCommands.getInstance().handleCommands(pc, cmd);
				return;
			}
			
			// トレードチャット
			// 本来はchatType==12になるはずだが、行頭の$が送信されない
			if (chatText.startsWith("$")) {
				String text = chatText.substring(1);
				chatWorld(pc, text, 12);
				if (!pc.isGm()) {
					pc.checkChatInterval();
				}
				return;
			}
			
			//查看版本
			if(chatText.equals("0.0 version?")){
				//pc.sendPackets(new S_SystemMessage("Rev1841"));
			}
			//~查看版本
			
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,Opcodes.S_OPCODE_NORMALCHAT, 0);
			if (!pc.getExcludingList().contains(pc.getName())) {
				pc.sendPackets(s_chatpacket);
			}
			for (L1PcInstance listner : L1World.getInstance().getRecognizePlayer(pc)) {
				if (!listner.getExcludingList().contains(pc.getName())) {
					listner.sendPackets(s_chatpacket);
				}
			}
			// ドッペル処理
			for (L1Object obj : pc.getKnownObjects()) {
				if (obj instanceof L1MonsterInstance) {
					L1MonsterInstance mob = (L1MonsterInstance) obj;
					if (mob.getNpcTemplate().is_doppel()
							&& mob.getName().equals(pc.getName())) {
						mob.broadcastPacket(new S_NpcChatPacket(mob, chatText,
								0));
					}
				}
			}
		} else if (chatType == 2) { // 叫び
			if (pc.isGhost()) {
				return;
			}
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
					Opcodes.S_OPCODE_NORMALCHAT, 2);
			if (!pc.getExcludingList().contains(pc.getName())) {
				pc.sendPackets(s_chatpacket);
			}
			for (L1PcInstance listner : L1World.getInstance().getVisiblePlayer(
					pc, 50)) {
				if (!listner.getExcludingList().contains(pc.getName())) {
					listner.sendPackets(s_chatpacket);
				}
			}

			// ドッペル処理
			for (L1Object obj : pc.getKnownObjects()) {
				if (obj instanceof L1MonsterInstance) {
					L1MonsterInstance mob = (L1MonsterInstance) obj;
					if (mob.getNpcTemplate().is_doppel()
							&& mob.getName().equals(pc.getName())) {
						for (L1PcInstance listner : L1World.getInstance()
								.getVisiblePlayer(mob, 50)) {
							listner.sendPackets(new S_NpcChatPacket(mob,
									chatText, 2));
						}
					}
				}
			}
		} else if (chatType == 3) { // 全体チャット
			chatWorld(pc, chatText, chatType);
		} else if (chatType == 4) { // 血盟チャット
			if (pc.getClanid() != 0) { // クラン所属中
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if (clan != null
						&& (rank == L1Clan.CLAN_RANK_PUBLIC
								|| rank == L1Clan.CLAN_RANK_GUARDIAN || rank == L1Clan.CLAN_RANK_PRINCE)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText,
							chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
							Opcodes.S_OPCODE_GLOBALCHAT, 4);
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance listner : clanMembers) {
						if (!listner.getExcludingList().contains(pc.getName())) {
							listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		} else if (chatType == 11) { // パーティーチャット
			if (pc.isInParty()) { // パーティー中
				ChatLogTable.getInstance().storeChat(pc, null, chatText,
						chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
						Opcodes.S_OPCODE_GLOBALCHAT, 11);
				L1PcInstance[] partyMembers = pc.getParty().getMembers();
				for (L1PcInstance listner : partyMembers) {
					if (!listner.getExcludingList().contains(pc.getName())) {
						listner.sendPackets(s_chatpacket);
					}
				}
			}
		} else if (chatType == 12) { // トレードチャット
			chatWorld(pc, chatText, chatType);
		} else if (chatType == 13) { // 連合チャット
			if (pc.getClanid() != 0) { // クラン所属中
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if (clan != null
						&& (rank == L1Clan.CLAN_RANK_GUARDIAN || rank == L1Clan.CLAN_RANK_PRINCE)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText,
							chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
							Opcodes.S_OPCODE_GLOBALCHAT, 13);
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance listner : clanMembers) {
						int listnerRank = listner.getClanRank();
						if (!listner.getExcludingList().contains(pc.getName())
								&& (listnerRank == L1Clan.CLAN_RANK_GUARDIAN || listnerRank == L1Clan.CLAN_RANK_PRINCE)) {
							listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		} else if (chatType == 14) { // チャットパーティー
			if (pc.isInChatParty()) { // チャットパーティー中
				ChatLogTable.getInstance().storeChat(pc, null, chatText,
						chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
						Opcodes.S_OPCODE_NORMALCHAT, 14);
				L1PcInstance[] partyMembers = pc.getChatParty().getMembers();
				for (L1PcInstance listner : partyMembers) {
					if (!listner.getExcludingList().contains(pc.getName())) {
						listner.sendPackets(s_chatpacket);
					}
				}
			}
		}
		if (!pc.isGm()) {
			pc.checkChatInterval();
		}
	}

	private void chatWorld(L1PcInstance pc, String chatText, int chatType) {
		if (pc.isGm()) {
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			L1World.getInstance().broadcastPacketToAll(
					new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT,
							chatType));
		} else if (pc.getLevel() >= Config.GLOBAL_CHAT_LEVEL) {
			if (L1World.getInstance().isWorldChatElabled()) {
				if (pc.get_food() >= 2) {
					//TODO 廣播扣飽食度
					if (Config.DeleteFood){
						pc.set_food(pc.get_food() - 2); //TODO 使用公頻不扣肉
					}
					//end
					ChatLogTable.getInstance().storeChat(pc, null, chatText,
							chatType);
					pc.sendPackets(new S_PacketBox(S_PacketBox.FOOD, pc
							.get_food()));
					for (L1PcInstance listner : L1World.getInstance()
							.getAllPlayers()) {
						if (!listner.getExcludingList().contains(pc.getName())) {
							if (listner.isShowTradeChat() && chatType == 12) {
								listner.sendPackets(new S_ChatPacket(pc,
										chatText, Opcodes.S_OPCODE_GLOBALCHAT,
										chatType));
							} else if (listner.isShowWorldChat()
									&& chatType == 3) {
								listner.sendPackets(new S_ChatPacket(pc,
										chatText, Opcodes.S_OPCODE_GLOBALCHAT,
										chatType));
							}
						}
					}
				} else {
					pc.sendPackets(new S_ServerMessage(462)); // \f1空腹のためチャットできません。
				}
			} else {
				pc.sendPackets(new S_ServerMessage(510)); // 現在ワールドチャットは停止中となっております。しばらくの間ご了承くださいませ。
			}
		} else {
			pc.sendPackets(new S_ServerMessage(195, String
					.valueOf(Config.GLOBAL_CHAT_LEVEL))); // レベル%0未満のキャラクターはチャットができません。
		}
	}

	@Override
	public String getType() {
		return C_CHAT;
	}
}
