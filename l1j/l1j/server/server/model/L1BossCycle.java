package l1j.server.server.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import l1j.server.server.datatables.BossSpawnTable;
import l1j.server.server.utils.PerformanceTimer;

@XmlAccessorType(XmlAccessType.FIELD)
public class L1BossCycle {
	@XmlAttribute(name = "Name")
	private String _name;
	@XmlElement(name = "Base")
	private Base _base;

	@XmlAccessorType(XmlAccessType.FIELD)
	private static class Base {
		@XmlAttribute(name = "Date")
		private String _date;
		@XmlAttribute(name = "Time")
		private String _time;

		public String getDate() {
			return _date;
		}

		public void setDate(String date) {
			this._date = date;
		}

		public String getTime() {
			return _time;
		}

		public void setTime(String time) {
			this._time = time;
		}
	}

	@XmlElement(name = "Cycle")
	private Cycle _cycle;

	@XmlAccessorType(XmlAccessType.FIELD)
	private static class Cycle {
		@XmlAttribute(name = "Period")
		private String _period;
		@XmlAttribute(name = "Start")
		private String _start;
		@XmlAttribute(name = "End")
		private String _end;

		public String getPeriod() {
			return _period;
		}

		public String getStart() {
			return _start;
		}

		public String getEnd() {
			return _end;
		}
	}

	private static final Random _rnd = new Random();
	private Calendar _baseDate;
	private int _period; // åæįŽ
	private int _periodDay;
	private int _periodHour;
	private int _periodMinute;

	private int _startTime; // åæįŽ
	private int _endTime; // åæįŽ
	private static SimpleDateFormat _sdfYmd = new SimpleDateFormat("yyyy/MM/dd");
	private static SimpleDateFormat _sdfTime = new SimpleDateFormat("HH:mm");
	private static SimpleDateFormat _sdf = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm");
	private static Date _initDate = new Date();
	private static String _initTime = "0:00";
	private static final Calendar START_UP = Calendar.getInstance();

	public void init() throws Exception {
		// åēæēæĨæãŽč¨­åŽ
		Base base = getBase();
		// åēæēããĒããã°ãįžå¨æĨäģãŽ0:00åēæē
		if (base == null) {
			setBase(new Base());
			getBase().setDate(_sdfYmd.format(_initDate));
			getBase().setTime(_initTime);
			base = getBase();
		} else {
			try {
				_sdfYmd.parse(base.getDate());
			} catch (Exception e) {
				base.setDate(_sdfYmd.format(_initDate));
			}
			try {
				_sdfTime.parse(base.getTime());
			} catch (Exception e) {
				base.setTime(_initTime);
			}
		}
		// åēæēæĨæãæąēåŽ
		Calendar baseCal = Calendar.getInstance();
		baseCal.setTime(_sdf.parse(base.getDate() + " " + base.getTime()));

		// åēįžå¨æãŽåæå,ãã§ãã¯
		Cycle spawn = getCycle();
		if (spawn == null || spawn.getPeriod() == null) {
			throw new Exception("CycleįPeriodåŋé");
		}

		String period = spawn.getPeriod();
		_periodDay = getTimeParse(period, "d");
		_periodHour = getTimeParse(period, "h");
		_periodMinute = getTimeParse(period, "m");

		String start = spawn.getStart();
		int sDay = getTimeParse(start, "d");
		int sHour = getTimeParse(start, "h");
		int sMinute = getTimeParse(start, "m");
		String end = spawn.getEnd();
		int eDay = getTimeParse(end, "d");
		int eHour = getTimeParse(end, "h");
		int eMinute = getTimeParse(end, "m");

		// åæįŽ
		_period = (_periodDay * 24 * 60) + (_periodHour * 60) + _periodMinute;
		_startTime = (sDay * 24 * 60) + (sHour * 60) + sMinute;
		_endTime = (eDay * 24 * 60) + (eHour * 60) + eMinute;
		if (_period <= 0) {
			throw new Exception("must be Period > 0");
		}
		// startčŖæ­Ŗ
		if (_startTime < 0 || _period < _startTime) { // čŖæ­Ŗ
			_startTime = 0;
		}
		// endčŖæ­Ŗ
		if (_endTime < 0 || _period < _endTime || end == null) { // čŖæ­Ŗ
			_endTime = _period;
		}
		if (_startTime > _endTime) {
			_startTime = _endTime;
		}
		// start,endãŽį¸éĸčŖæ­Ŗ(æäŊã§ã1åãŽéãããã)
		// start==endã¨ããæåŽã§ããåēįžæéãæŦĄãŽå¨æãĢčĸĢããĒããããĢãããã
		if (_startTime == _endTime) {
			if (_endTime == _period) {
				_startTime--;
			} else {
				_endTime++;
			}
		}

		// æčŋãŽå¨æãžã§čŖæ­Ŗ(åč¨įŽããã¨ããĢåŗå¯ãĢįŽåēãããŽã§ãããã§ã¯čŋããžã§éŠåŊãĢčŖæ­Ŗããã ã)
		while (!(baseCal.after(START_UP))) {
			baseCal.add(Calendar.DAY_OF_MONTH, _periodDay);
			baseCal.add(Calendar.HOUR_OF_DAY, _periodHour);
			baseCal.add(Calendar.MINUTE, _periodMinute);
		}
		_baseDate = baseCal;
	}

	/*
	 * æåŽæĨæãåĢãå¨æ(ãŽéå§æé)ãčŋã
	 * ex.å¨æã2æéãŽå ´å
	 *  target base æģãå¤
	 *   4:59  7:00 3:00
	 *   5:00  7:00 5:00
	 *   5:01  7:00 5:00
	 *   6:00  7:00 5:00
	 *   6:59  7:00 5:00
	 *   7:00  7:00 7:00
	 *   7:01  7:00 7:00
	 *   9:00  7:00 9:00
	 *   9:01  7:00 9:00
	 */
	private Calendar getBaseCycleOnTarget(Calendar target) {
		// åēæēæĨæååž
		Calendar base = (Calendar) _baseDate.clone();
		if (target.after(base)) {
			// target <= baseã¨ãĒããžã§įš°ãčŋã
			while (target.after(base)) {
				base.add(Calendar.DAY_OF_MONTH, _periodDay);
				base.add(Calendar.HOUR_OF_DAY, _periodHour);
				base.add(Calendar.MINUTE, _periodMinute);
			}
		}
		if (target.before(base)) {
			while (target.before(base)) {
				base.add(Calendar.DAY_OF_MONTH, -_periodDay);
				base.add(Calendar.HOUR_OF_DAY, -_periodHour);
				base.add(Calendar.MINUTE, -_periodMinute);
			}
		}
		// įĩäēæéãįŽåēããĻãŋãĻãéåģãŽæåģãĒãããšæéãéããĻããâæŦĄãŽå¨æãčŋãã
		Calendar end = (Calendar) base.clone();
		end.add(Calendar.MINUTE, _endTime);
		if (end.before(target)) {
			base.add(Calendar.DAY_OF_MONTH, _periodDay);
			base.add(Calendar.HOUR_OF_DAY, _periodHour);
			base.add(Calendar.MINUTE, _periodMinute);
		}
		return base;
	}

	/**
	 * æåŽæĨæãåĢãå¨æãĢå¯žããĻãåēįžãŋã¤ããŗã°ãįŽåēããã
	 * @return åēįžããæé
	 */
	public Calendar calcSpawnTime(Calendar now) {
		// åēæēæĨæååž
		Calendar base = getBaseCycleOnTarget(now);
		// åēįžæéãŽč¨įŽ
		base.add(Calendar.MINUTE, _startTime);
		// åēįžæéãŽæąēåŽ startīŊendčŋãŽéã§ãŠãŗãã ãŽį§
		int diff = (_endTime - _startTime) * 60;
		int random = diff > 0 ? _rnd.nextInt(diff) : 0;
		base.add(Calendar.SECOND, random);
		return base;
	}

	/**
	 * æåŽæĨæãåĢãå¨æãĢå¯žããĻãåēįžéå§æéãįŽåēããã
	 * @return å¨æãŽåēįžéå§æé
	 */
	public Calendar getSpawnStartTime(Calendar now) {
		// åēæēæĨæååž
		Calendar startDate = getBaseCycleOnTarget(now);
		// åēįžæéãŽč¨įŽ
		startDate.add(Calendar.MINUTE, _startTime);
		return startDate;
	}

	/**
	 * æåŽæĨæãåĢãå¨æãĢå¯žããĻãåēįžįĩäēæéãįŽåēããã
	 * @return å¨æãŽåēįžįĩäēæé
	 */
	public Calendar getSpawnEndTime(Calendar now) {
		// åēæēæĨæååž
		Calendar endDate = getBaseCycleOnTarget(now);
		// åēįžæéãŽč¨įŽ
		endDate.add(Calendar.MINUTE, _endTime);
		return endDate;
	}

	/**
	 * æåŽæĨæãåĢãå¨æãĢå¯žããĻãæŦĄãŽå¨æãŽåēįžãŋã¤ããŗã°ãįŽåēããã
	 * @return æŦĄãŽå¨æãŽåēįžããæé
	 */
	public Calendar nextSpawnTime(Calendar now) {
		// åēæēæĨæååž
		Calendar next = (Calendar) now.clone();
		next.add(Calendar.DAY_OF_MONTH, _periodDay);
		next.add(Calendar.HOUR_OF_DAY, _periodHour);
		next.add(Calendar.MINUTE, _periodMinute);
		return calcSpawnTime(next);
	}

	/**
	 * æåŽæĨæãĢå¯žããĻãæčŋãŽåēįžéå§æéãčŋå´ããã
	 * @return æčŋãŽåēįžéå§æé
	 */
	public Calendar getLatestStartTime(Calendar now) {
		// åēæēæĨæååž
		Calendar latestStart = getSpawnStartTime(now);
		if (!now.before(latestStart)) { // now >= latestStart
		} else {
			// now < latestStartãĒã1ååãæčŋãŽå¨æ
			latestStart.add(Calendar.DAY_OF_MONTH, -_periodDay);
			latestStart.add(Calendar.HOUR_OF_DAY, -_periodHour);
			latestStart.add(Calendar.MINUTE, -_periodMinute);
		}

		return latestStart;
	}

	private static int getTimeParse(String target, String search) {
		if (target == null) {
			return 0;
		}
		int n = 0;
		Matcher matcher = Pattern.compile("\\d+" + search).matcher(target);
		if (matcher.find()) {
			String match = matcher.group();
			n = Integer.parseInt(match.replace(search, ""));
		}
		return n;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "BossCycleList")
	static class L1BossCycleList {
		@XmlElement(name = "BossCycle")
		private List<L1BossCycle> bossCycles;

		public List<L1BossCycle> getBossCycles() {
			return bossCycles;
		}

		public void setBossCycles(List<L1BossCycle> bossCycles) {
			this.bossCycles = bossCycles;
		}
	}

	public static void load() {
		PerformanceTimer timer = new PerformanceTimer();
		System.out.print("ããæ­Ŗå¨čŧåĨ boss cycle č¨­åŽ...");
		try {
			// BookOrder ã¯ãŠãšããã¤ãŗããŖãŗã°ãããŗãŗãã­ãšããįæ
			JAXBContext context = JAXBContext
					.newInstance(L1BossCycle.L1BossCycleList.class);

			// XML -> POJO å¤æãčĄããĸãŗããŧãˇãŖãŠãįæ
			Unmarshaller um = context.createUnmarshaller();

			// XML -> POJO å¤æ
			File file = new File("./data/xml/Cycle/BossCycle.xml");
			L1BossCycleList bossList = (L1BossCycleList) um.unmarshal(file);

			for (L1BossCycle cycle : bossList.getBossCycles()) {
				cycle.init();
				_cycleMap.put(cycle.getName(), cycle);
			}

			// userããŧãŋãããã°ä¸æ¸ã
			File userFile = new File("./data/xml/Cycle/users/BossCycle.xml");
			if (userFile.exists()) {
				bossList = (L1BossCycleList) um.unmarshal(userFile);

				for (L1BossCycle cycle : bossList.getBossCycles()) {
					cycle.init();
					_cycleMap.put(cycle.getName(), cycle);
				}
			}
			// spawnlist_bossããčĒ­ãŋčžŧãã§éįŊŽ
			BossSpawnTable.fillSpawnTable();
		} catch (Exception e) {
			_log.log(Level.SEVERE, "BossCycle įĄæŗčŧåĨ", e);
			System.exit(0);
		}
		System.out.println("åŽæ! " + timer.get() + "æ¯Ģį§");
	}

	/**
	 * å¨æåã¨æåŽæĨæãĢå¯žããåēįžæéãåēįžæéããŗãŗãŊãŧãĢåēå
	 * @param now å¨æãåēåããæĨæ
	 */
	public void showData(Calendar now) {
		System.out.println("[Type]" + getName());
		System.out.print("  [åēįžæé]");
		System.out.print(_sdf.format(getSpawnStartTime(now).getTime()) + " - ");
		System.out.println(_sdf.format(getSpawnEndTime(now).getTime()));
	}

	private static HashMap<String, L1BossCycle> _cycleMap = new HashMap<String, L1BossCycle>();

	public static L1BossCycle getBossCycle(String type) {
		return _cycleMap.get(type);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public Base getBase() {
		return _base;
	}

	public void setBase(Base base) {
		this._base = base;
	}

	public Cycle getCycle() {
		return _cycle;
	}

	public void setCycle(Cycle cycle) {
		this._cycle = cycle;
	}

	private static Logger _log = Logger.getLogger(L1BossCycle.class.getName());
}
