package br.ufmg.dcc.labsoft.jextract.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EntitySet implements Iterable<String> {

	private Set<String> entities = new HashSet<String>();

	public void add(String item) {
		this.entities.add(item);
	}

	@Override
    public Iterator<String> iterator() {
	    return this.entities.iterator();
    }

	public boolean contains(String item) {
		return this.entities.contains(item);
	}

	public int size() {
	    return this.entities.size();
    }

}
