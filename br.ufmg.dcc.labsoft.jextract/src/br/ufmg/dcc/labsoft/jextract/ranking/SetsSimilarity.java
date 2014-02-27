package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.HashSet;
import java.util.Set;

import br.ufmg.dcc.labsoft.jextract.model.EntitySet;

public class SetsSimilarity {

	final Set<String> intersection;
	final EntitySet set1;
	final EntitySet set2;

    public SetsSimilarity(EntitySet set1, EntitySet set2) {
    	this.intersection = new HashSet<String>();
	    this.set1 = set1;
	    this.set2 = set2;
    }

	public SetsSimilarity() {
    	this(new EntitySet(), new EntitySet());
    }

	public void addToSet1(String item) {
		set1.add(item);
	}

	public void addToSet2(String item) {
		set2.add(item);
	}

	public SetsSimilarity end() {
		for (String elem : set2) {
			if (set1.contains(elem)) {
				intersection.add(elem);
			}
		}
		return this;
	}

	public int getA() {
		return intersection.size();
	}

	public int getB() {
		return set1.size() - intersection.size();
	}

	public int getC() {
		return set2.size() - intersection.size();
	}
	
	public double sim(Coefficient coefficient) {
		if (this.getA() == 0) {
			return 0.0;
		}
		double a = this.getA();
		double b = this.getB();
		double c = this.getC();
		return coefficient.formula(a, b, c);
	}

	public double dist(Coefficient coefficient) {
		return 1.0 - this.sim(coefficient);
	}

}
