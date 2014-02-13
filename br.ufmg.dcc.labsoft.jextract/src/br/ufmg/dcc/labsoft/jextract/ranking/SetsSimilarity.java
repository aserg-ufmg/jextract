package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.HashSet;
import java.util.Set;

public class SetsSimilarity<T> {

	final Set<T> intersection = new HashSet<T>();
	final Set<T> set1 = new HashSet<T>();
	final Set<T> set2 = new HashSet<T>();

	public void addToSet1(T item) {
		set1.add(item);
	}

	public void addToSet2(T item) {
		set2.add(item);
	}

	public void end() {
		for (T elem : set2) {
			if (set1.contains(elem)) {
				intersection.add(elem);
			}
		}
		for (T elem : intersection) {
			set2.remove(elem);
			set1.remove(elem);
		}
	}

	public int getA() {
		return intersection.size();
	}

	public int getB() {
		return set1.size();
	}

	public int getC() {
		return set2.size();
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
