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

import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.ClientThread;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Rank extends ClientBasePacket {

	private static final String C_RANK = "[C] C_Rank";
	private static Logger _log = Logger.getLogger(C_Rank.class.getName());

	public C_Rank(byte abyte0[], ClientThread clientthread)
			throws Exception {
		super(abyte0);

		readC(); //unknown data fix by sdcom 2010.9.9
		int rank = readC();
		String name = readS();

		L1PcInstance pc = clientthread.getActiveChar();
		L1PcInstance targetPc = L1World.getInstance().getPlayer(name);

		if (pc == null) {
			return;
		}

		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
		if (clan == null) {
			return;
		}

		if (rank < 1 && 3 < rank) {
			// ランクを変更する人の名前とランクを入力してください。[ランク=ガーディアン、一般、見習い]
			pc.sendPackets(new S_ServerMessage(781));
			return;
		}

		if (pc.isCrown()) { // 君主
			if (pc.getId() != clan.getLeaderId()) { // 血盟主
				pc.sendPackets(new S_ServerMessage(785)); // あなたはもう君主ではありません。
				return;
			}
		} else {
			pc.sendPackets(new S_ServerMessage(518)); // この命令は血盟の君主のみが利用できます。
			return;
		}

		if (targetPc != null) { // オンライン中
			if (pc.getClanid() == targetPc.getClanid()) { // 同じクラン
				try {
					targetPc.setClanRank(rank);
					targetPc.save(); // DBにキャラクター情報を書き込む
					String rankString = "$772";
					if (rank == L1Clan.CLAN_RANK_PROBATION) {
						rankString = "$774";
					} else if (rank == L1Clan.CLAN_RANK_PUBLIC) {
						rankString = "$773";
					} else if (rank == L1Clan.CLAN_RANK_GUARDIAN) {
						rankString = "$772";
					}
					targetPc.sendPackets(new S_ServerMessage(784, rankString)); // あなたのランクが%sに変更されました。
				} catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} else {
				pc.sendPackets(new S_ServerMessage(414)); // 同じ血盟員ではありません。
				return;
			}
		} else { // オフライン中
			L1PcInstance restorePc = CharacterTable.getInstance()
					.restoreCharacter(name);
			if (restorePc != null
					&& restorePc.getClanid() == pc.getClanid()) { // 同じクラン
				try {
					restorePc.setClanRank(rank);
					restorePc.save(); // DBにキャラクター情報を書き込む
				} catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} else {
				pc.sendPackets(new S_ServerMessage(109, name)); // %0という名前の人はいません。
				return;
			}
		}
	}

	@Override
	public String getType() {
		return C_RANK;
	}
}
