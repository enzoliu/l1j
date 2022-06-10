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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.Opcodes;
import l1j.server.server.WarTimeController;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.model.L1World;
import l1j.server.server.templates.L1Castle;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket

public class S_WarTime extends ServerBasePacket {
	private static Logger _log = Logger.getLogger(S_WarTime.class.getName());
	private static final String S_WAR_TIME = "[S] S_WarTime";	
	
	public S_WarTime(Calendar cal) {
		// 1997/01/01 17:00を基点としている
		Calendar base_cal = Calendar.getInstance();
		base_cal.clear();
		base_cal.set(1997, 0, 1, 17, 0);
		long base_millis = base_cal.getTimeInMillis();
		//加上下次攻城時間基準
		Calendar nextWarTime = Calendar.getInstance();
		nextWarTime.setTime(cal.getTime());
		nextWarTime.add(Config.ALT_WAR_INTERVAL_UNIT, Config.ALT_WAR_INTERVAL);
		long millis = nextWarTime.getTimeInMillis();
		long diff = millis - base_millis;
		
		diff -= 1200 * 60 * 1000; // 誤差修正
		diff = diff / 60000; // 分以下切捨て
		//diff -= 99;// 誤差修正 for 2009-5-27
		// timeは1加算すると3:02（182分）進む
		int time = (int) (diff / 182);
		/*
		int diffHour=0;
		int diffSecond=0;
		Calendar 誤差修正 = Calendar.getInstance();
		誤差修正.clear();
		誤差修正.set(2007, 7, 1, 8, 0);
		long diffMillis = millis-誤差修正.getTimeInMillis();
		Calendar 誤差修正2 = Calendar.getInstance();
		誤差修正2.clear();
		誤差修正2.set(2010, 7, 1, 8, 0);
		long diffMillis2 = 誤差修正2.getTimeInMillis()-誤差修正.getTimeInMillis();
		if(diffMillis>0){  //比2009-5-27 遠
			int dif  = (int)(diffMillis*449/diffMillis2);
			diffHour = dif/182;
			diffSecond = (dif*43/60)%182;
		}
		*/
		// writeDの直前のwriteCで時間の調節ができる
		// 0.7倍した時間だけ縮まるが
		// 1つ調整するとその次の時間が広がる？
		writeC(Opcodes.S_OPCODE_WARTIME);
		writeH(6); // リストの数（6以上は無効）
		writeS(Config.TIME_ZONE); // 時間の後ろの（）内に表示される文字列
		
		for (int i = 0; i <= 5; i++) {
			writeH(i); 			// 第幾個時間
			writeC(0);   		// 誤差修正 for 2009-5-27
			writeH(time + i);	// 時間1=3:02
			//時間60=43.5分吧?
			/*
			writeC(diffSecond);   		// 誤差修正 for 2009-5-27
			writeH(time + i - diffHour);	// 時間1=3:02
			*/
		}
		/*
		writeC(0); // ?
		writeC(0); // ?
		writeC(0);
		writeD(time);
		writeC(0);
		writeD(time - 1);
		writeC(0);
		writeD(time - 2);
		writeC(0);
		writeD(time - 3);
		writeC(0);
		writeD(time - 4);
		writeC(0);
		writeD(time - 5);
		writeC(0);
		*/
	}
	
	public S_WarTime(L1Castle castle) {
		// 1997/01/01 17:00を基点としている
		Calendar cal = castle.getWarTime();
		Calendar base_cal = Calendar.getInstance();
		base_cal.clear();
		base_cal.set(1997, 0, 1, 17, 0);
		long base_millis = base_cal.getTimeInMillis();
		//加上下次攻城時間基準
		Calendar nextWarTime = Calendar.getInstance();
		nextWarTime.setTime(cal.getTime());
		if(!castle.isSetWarTime()){//還沒設定過攻城時間
			nextWarTime.add(Config.ALT_WAR_INTERVAL_UNIT, Config.ALT_WAR_INTERVAL);
		}
		long millis = nextWarTime.getTimeInMillis();
		long diff = millis - base_millis;
		
		diff -= 1200 * 60 * 1000; // 誤差修正
		diff = diff / 60000; // 分以下切捨て
		// timeは1加算すると3:02（182分）進む
		int time = (int) (diff / 182);
		// writeDの直前のwriteCで時間の調節ができる
		// 0.7倍した時間だけ縮まるが
		// 1つ調整するとその次の時間が広がる？
		writeC(Opcodes.S_OPCODE_WARTIME);
		writeH((castle.isSetWarTime())?1:6); // リストの数（6以上は無効）
		writeS(Config.TIME_ZONE); // 時間の後ろの（）内に表示される文字列
		
		for (int i = 0; i <= ((castle.isSetWarTime())?0:5); i++) {
			writeH(i); 			// 第幾個時間
			writeC(0);   		
			writeH(time + i);	// 時間1=3:02
		}
	}
	
	@Override
	public byte[] getContent() {
		return getBytes();
	}

	@Override
	public String getType() {
		return S_WAR_TIME;
	}
}
