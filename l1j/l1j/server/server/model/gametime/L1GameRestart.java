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
package l1j.server.server.model.gametime;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.GeneralThreadPool;

//elfooxx
import java.util.Collection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.L1World;
import l1j.server.server.serverpackets.S_SystemMessage;
//elfooxx

public class L1GameRestart {
	private static Logger _log = Logger.getLogger(L1GameRestart.class
			.getName());

	private static L1GameRestart _instance;
	private volatile L1GameTime _currentTime = new L1GameTime();
	private L1GameTime _previousTime = null;

	private List<L1GameTimeListener> _listeners = new CopyOnWriteArrayList<L1GameTimeListener>();

	private static int willRestartTime;
	public  int _remnant ;  /* elfooxx */

	private class TimeUpdaterRestar implements Runnable /*elfooxx*/ { 
	  public void run() {
	    while ( true ) {
	      _previousTime = _currentTime;
	      _currentTime = new L1GameTime();
	      notifyChanged();
	      int remnant = GetRestartTime()* 60 ;
	      System.out.println( "》》正在載入 自動重開 設定...完成! "+ GetRestartTime()+"分鐘後" );
	      while ( remnant > 0 ) {
	        for ( int i = remnant ; i >= 0 ; i -- ) {
	         SetRemnant(i) ;
	         willRestartTime=i;
	         if ( i % 60 == 0 && i <= 300 && i != 0 )  {
	           BroadCastToAll( "伺服器將於 " + i/60 +" 分鐘後自動重啟,請至安全區域準備登出。" );
	           System.out.println( "伺服器將於 " + i/60 +" 分鐘後重新啟動" );     
	         } // if (五分鐘內 一分鐘一次)
	         else if ( i <= 30 && i != 0 )  {
	           BroadCastToAll( "伺服器將於 " + i +"秒後重新啟動,煩請儘速下線！" );
	           System.out.println( "伺服器將於 " + i +" 秒後重新啟動" );     
	         } // else if (30秒內 一秒一次)
	         else if ( i == 0) {
	           BroadCastToAll("伺服器自動重啟。");
	           System.out.println("伺服器重新啟動。");  
	           disconnectAllCharacters();
	           System.exit(1);
	         } // if 1秒
	         try {
	           Thread.sleep( 1000 );
	         } catch ( InterruptedException e ) {
	           _log.log(Level.SEVERE, e.getLocalizedMessage(), e);
	         } // try and catch
	                           
	        } // for
	      } // while ( remnant > 0 ) 
	    } // while true         
	  } // run()
	} // class TimeUpdaterRestar /*elfooxx*/

	public void disconnectAllCharacters() {
		Collection<L1PcInstance> players = L1World.getInstance()
				.getAllPlayers();
		for (L1PcInstance pc : players) {
			pc.getNetConnection().setActiveChar(null);
			pc.getNetConnection().kick();
		}
		// 全員Kickした後に保存処理をする
		for (L1PcInstance pc : players) {
			ClientThread.quitGame(pc);
			L1World.getInstance().removeObject(pc);
		}
	}
	
	private int GetRestartTime()  {
	  return Config.RESTART_TIME;
	} // GetRestartTime() 
	            
	private void BroadCastToAll( String string ) {
	  Collection <L1PcInstance> allpc = L1World.getInstance().getAllPlayers();
	  for ( L1PcInstance pc : allpc )
	    pc.sendPackets( new S_SystemMessage( string ) );
	} // BroadcastToAll()

	public void SetRemnant ( int remnant ) {
	  _remnant = remnant ;
	}
	
	public static int getWillRestartTime(){
		return willRestartTime;
	}
	
	public int GetRemnant ( ) {
	  return _remnant ;
	}
	/*elfooxx*/
	

	private boolean isFieldChanged(int field) {
		return _previousTime.get(field) != _currentTime.get(field);
	}

	private void notifyChanged() {
		if (isFieldChanged(Calendar.MONTH)) {
			for (L1GameTimeListener listener : _listeners) {
				listener.onMonthChanged(_currentTime);
			}
		}
		if (isFieldChanged(Calendar.DAY_OF_MONTH)) {
			for (L1GameTimeListener listener : _listeners) {
				listener.onDayChanged(_currentTime);
			}
		}
		if (isFieldChanged(Calendar.HOUR_OF_DAY)) {
			for (L1GameTimeListener listener : _listeners) {
				listener.onHourChanged(_currentTime);
			}
		}
		if (isFieldChanged(Calendar.MINUTE)) {
			for (L1GameTimeListener listener : _listeners) {
				listener.onMinuteChanged(_currentTime);
			}
		}
	}

	private L1GameRestart() {
		GeneralThreadPool.getInstance().execute(new TimeUpdaterRestar());
	}

	public static void init() {
		_instance = new L1GameRestart();
	}

	public static L1GameRestart getInstance() {
		return _instance;
	}

	public L1GameTime getGameTime() {
		return _currentTime;
	}

	public void addListener(L1GameTimeListener listener) {
		_listeners.add(listener);
	}

	public void removeListener(L1GameTimeListener listener) {
		_listeners.remove(listener);
	}
}
