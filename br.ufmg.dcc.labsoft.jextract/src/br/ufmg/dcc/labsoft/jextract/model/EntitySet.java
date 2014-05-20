package br.ufmg.dcc.labsoft.jextract.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntitySet implements Iterable<String> {

	private Map<String, Integer> entities = new HashMap<String, Integer>();

	public void add(String item) {
		Integer count = this.entities.get(item);
		if (count == null) {
			this.entities.put(item, 1);
		} else {
			this.entities.put(item, count + 1);
		}
	}

	@Override
    public Iterator<String> iterator() {
	    return this.entities.keySet().iterator();
    }

	public boolean contains(String item) {
		return this.entities.get(item) != null;
	}

	public int getCount(String item) {
		Integer count = this.entities.get(item);
		return count == null ? 0 : count;
	}

	public int size() {
	    return this.entities.size();
    }

	public EntitySet intersection(EntitySet other) {
		EntitySet intersection = new EntitySet();
		for (String element : this) {
			if (other.contains(element)) {
				intersection.add(element);
			}
		}
		return intersection;
	}

	public EntitySet minus(EntitySet other) {
		EntitySet result = new EntitySet();
		for (String element : this) {
			if (!other.contains(element)) {
				result.add(element);
			}
		}
		return result;
	}

}
