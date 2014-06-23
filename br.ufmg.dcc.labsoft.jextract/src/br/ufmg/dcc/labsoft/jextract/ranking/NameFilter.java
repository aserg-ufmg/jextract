package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.ArrayList;
import java.util.List;

public class NameFilter {

	private List<String> prefixes;
	private List<String> fullNames;

	public NameFilter(String commaSeparatedList) {
		this.prefixes = new ArrayList<String>();
		this.fullNames = new ArrayList<String>();
		if (!commaSeparatedList.isEmpty()) {
			String[] items = commaSeparatedList.split(",");
			for (String item : items) {
				if (item.endsWith("*")) {
					prefixes.add(item.substring(0, item.length() - 1));
				} else {
					fullNames.add(item);
				}
			}
		}
	}

	public boolean contains(String name) {
		for (String prefix : this.prefixes) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		for (String fullName : this.fullNames) {
			if (fullName.equals(name)) {
				return true;
			}
		}
		return false;
	}

}
