/*
 * 本程式是一個自由軟體(Free Software)，你可以自由的散佈以及、或者修改它，但是必須
 * 基於 GNU GPL(GNU General Public License) 的授權條款之下，並且隨時適用於任何
 * 自由軟體基金會(FSF, Free Software Foundation)所制定的最新條款。
 *
 * 這支程式的發表目的是希望將能夠有用、強大，但是不附加任何的保證及擔保任何責任；甚至
 * 暗示保證任何用途、方面的適銷性或適用性。如果你想要了解進一步的授權內容，請詳見於最
 * 新版本的 GPL 版權聲明。
 *
 * 你應該會在本程式的根資料夾底下，見到適用於目前版本的 licenses.txt，這是一個複製
 * 版本的 GPL 授權，如果沒有，也許你可以聯繫自由軟體基金會取得最新的授權。
 *
 * 你可以寫信到 :
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA, 02111-1307, USA.
 *
 * 或者參觀 GNU 的官方網站，以取得 GPL 的進一步資料。
 * http://www.gnu.org/copyleft/gpl.html
 */

package l1j.server.server.clientpackets;

import java.util.Calendar;

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Castle;

public class C_ChangeWarTime_AC2 extends ClientBasePacket {

	public C_ChangeWarTime_AC2(byte decrypt[], ClientThread client)	throws Exception {
		super(decrypt);
		
		int d = readC();
		L1PcInstance player = client.getActiveChar();
		String clan_name = player.getClanname();
		L1Clan clan = L1World.getInstance().getClan(clan_name);
		if(clan.getCastleId()==0 || !clan.getLeaderName().equals(player.getName())){
			return;
		}
		L1Castle castle = CastleTable.getInstance().getCastleTable(clan.getCastleId());
		Calendar cal = castle.getWarTime();
		//加上下次攻城時間基準
		Calendar nextWarTime = Calendar.getInstance();
		nextWarTime.setTime(cal.getTime());
		if(!castle.isSetWarTime()){//還沒設定過攻城時間
			nextWarTime.add(Config.ALT_WAR_INTERVAL_UNIT, Config.ALT_WAR_INTERVAL);
			nextWarTime.add(Calendar.MINUTE, 182*d);
			castle.setWarTime(nextWarTime);
			castle.setWarTime(true);
			CastleTable.getInstance().updateTime(clan.getCastleId(), nextWarTime);
		}
		player.sendPackets(new S_ServerMessage(304,nextWarTime.get(Calendar.YEAR)+"年"+(nextWarTime.get(Calendar.MONTH)+1)+"月"+nextWarTime.get(Calendar.DATE)+"日"+nextWarTime.get(Calendar.HOUR_OF_DAY)+"點"));
	}

	
	@Override
	public String getType() {
		return C_CHANGE_WAR_TIME_AC2;
	}

	private static final String C_CHANGE_WAR_TIME_AC2 = "[C] C_ChangeWarTime_AC2";
}
