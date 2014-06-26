package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.ArrayList;
import java.util.List;

public class NameFilter {

	private List<String> descendentOf;
	private List<String> childrenOf;
	private List<String> fullNames;

	public NameFilter(String commaSeparatedList) {
		this.descendentOf = new ArrayList<String>();
		this.childrenOf = new ArrayList<String>();
		this.fullNames = new ArrayList<String>();
		final String anyDescendent = "**";
		final String anyChild = "*";
		if (!commaSeparatedList.isEmpty()) {
			String[] items = commaSeparatedList.split(",");
			for (String item : items) {
				if (item.endsWith(anyDescendent)) {
					descendentOf.add(item.substring(0, item.length() - anyDescendent.length()));
				} else if (item.endsWith(anyChild)) {
					childrenOf.add(item.substring(0, item.length() - anyChild.length()));
				} else {
					fullNames.add(item);
				}
			}
		}
	}

	public boolean contains(String name) {
		for (String prefix : this.descendentOf) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		for (String parent : this.childrenOf) {
			if (name.startsWith(parent) && name.lastIndexOf(".") < parent.length()) {
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
