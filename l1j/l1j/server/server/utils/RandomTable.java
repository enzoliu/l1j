package l1j.server.server.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTable {
	private List<Integer> list = new ArrayList<Integer>();
	private int index = 0;
	private static final Random _rnd = new Random();
	
	public RandomTable() {
		while (true) {
			if (this.list.contains(1) && this.list.size() >= 100) {
				break;
			}
			this.list.add(_rnd.nextInt(100) + 1);
		}
	}
	
	public int next() {
		if (this.index >= this.list.size() - 1) {
			this.index = 0;
		}
		return this.list.get(index);
	}
}
