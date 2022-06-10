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
import l1j.server.server.Opcodes;
import l1j.server.server.datatables.ChatLogTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_ChatWhisper extends ClientBasePacket {

	private static final String C_CHAT_WHISPER = "[C] C_ChatWhisper";

	public C_ChatWhisper(byte abyte0[], ClientThread client) throws Exception {
		super(abyte0);
		String targetName = readS();
		String text = readS();
		L1PcInstance whisperFrom = client.getActiveChar();
		// チャット禁止中の場合
		if (whisperFrom.hasSkillEffect(1005)) {
			whisperFrom.sendPackets(new S_ServerMessage(242)); // 現在チャット禁止中です。
			return;
		}
		// ウィスパー可能なLv未満の場合
		if (whisperFrom.getLevel() < Config.WHISPER_CHAT_LEVEL) {
			whisperFrom.sendPackets(new S_ServerMessage(404, String
					.valueOf(Config.WHISPER_CHAT_LEVEL))); // %0レベル以下ではウィスパー、パーティーチャットは使用できません。
			return;
		}
		L1PcInstance whisperTo = L1World.getInstance().getPlayer(targetName);
		// ワールドにいない場合
		if (whisperTo == null) {
			whisperFrom.sendPackets(new S_ServerMessage(73, targetName)); // \f1%0はゲームをしていません。
			return;
		}
		// 自分自身に対するwisの場合
		if (whisperTo.equals(whisperFrom)) {
			return;
		}
		// 遮断されている場合
		if (whisperTo.getExcludingList().contains(whisperFrom.getName())) {
			whisperFrom.sendPackets(new S_ServerMessage(117, whisperTo
					.getName())); // %0があなたを遮断しました。
			return;
		}
		// ゲームオプションでOFFにしている場合
		if (!whisperTo.isCanWhisper()) {
			whisperFrom.sendPackets(new S_ServerMessage(205, whisperTo
					.getName()));
			return;
		}

		ChatLogTable.getInstance().storeChat(whisperFrom, whisperTo, text, 1);
		whisperFrom.sendPackets(new S_ChatPacket(whisperTo, text,
				Opcodes.S_OPCODE_GLOBALCHAT, 9));
		whisperTo.sendPackets(new S_ChatPacket(whisperFrom, text,
				Opcodes.S_OPCODE_WHISPERCHAT, 16));
	}

	@Override
	public String getType() {
		return C_CHAT_WHISPER;
	}
}
