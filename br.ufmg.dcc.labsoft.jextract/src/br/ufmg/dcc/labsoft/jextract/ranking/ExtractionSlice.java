package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ExtractionSlice {

	private final Fragment[] fragments;

	public static ExtractionSlice fromString(String sourceFragments) {
		String[] frags = sourceFragments.split(";");
		List<Fragment> fragments = new ArrayList<Fragment>(frags.length);
		for (int i = 0; i < frags.length; i++) {
			String frag = frags[i];
			char typeFlag = frag.charAt(0);
			boolean duplicate = typeFlag == 'd' ? true : false;
			boolean partialSelection = false;
			int middle = frag.indexOf(':');
			if (middle < 0) {
				middle = frag.indexOf('~');
				partialSelection = true;
			}
			int start = Integer.parseInt(frag.substring(1, middle));
			int length = Integer.parseInt(frag.substring(middle + 1));
			
			fragments.add(new Fragment(start, start + length, duplicate, partialSelection));
			
			// e5373:27;e5411:7;e5429:44;
		}
		Collections.sort(fragments, new Comparator<Fragment>(){
			@Override
            public int compare(Fragment o1, Fragment o2) {
	            return o1.start - o2.start;
            }
		});
		for (int i = fragments.size() - 1; i >= 0; i--) {
			for (int j = i - 1; j >= 0; j--) {
				if (fragments.get(j).encloses(fragments.get(i))) {
					fragments.remove(i);
					break;
				}
			}
		}
		return new ExtractionSlice(fragments.toArray(new Fragment[fragments.size()]));
	}

	public ExtractionSlice(Fragment ... fragments) {
		this.fragments = fragments;
	}

	public boolean belongsToExtracted(int position) {
		return this.find(position) != -1;
	}

	public boolean belongsToMethod(int position) {
		return this.find(position) != 0;
	}

	private int find(int position) {
		for (Fragment frag : this.fragments) {
			if (position >= frag.start && position <= frag.end) {
				return frag.duplicate ? 1 : 0;
			}
		}
		return -1;
	}

	private int findIntersection(int start, int end) {
		for (Fragment frag : this.fragments) {
			boolean startsAfterFrag = start >= frag.end;
			boolean endsBeforeFrag = frag.start >= end;
			if (!startsAfterFrag && !endsBeforeFrag) {
				return frag.duplicate ? 1 : 0;
			}
		}
		return -1;
	}

	public boolean hasIntersectionWithExtracted(int position, int length) {
		return this.findIntersection(position, position + length) != -1;
	}

	public Fragment[] getFragments() {
		return this.fragments;
	}

	public static class Fragment {
		public final int start;
		public final int end;
		public final boolean duplicate;
		public final boolean partialSelection;
		public Fragment(int start, int end, boolean duplicate) {
			this.start = start;
			this.end = end;
			this.duplicate = duplicate;
			this.partialSelection = false;
		}
		public Fragment(int start, int end, boolean duplicate, boolean partialSelection) {
			this.start = start;
			this.end = end;
			this.duplicate = duplicate;
			this.partialSelection = partialSelection;
		}
		public int length() {
			return this.end - this.start;
		}
		public boolean encloses(Fragment other) {
			return !this.partialSelection && !other.partialSelection && this.start <= other.start && this.end >= other.end;
		}
	}

	public ExtractionSlice reduce() {
		return new ExtractionSlice(this.getEnclosingFragment());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Fragment fragment : this.fragments) {
			sb.append(fragment.duplicate ? 'd' : 'e');
			sb.append("" + fragment.start);
			sb.append(fragment.partialSelection ? '~' : ':');
			sb.append("" + (fragment.end - fragment.start));
			sb.append(';');
		}
		return sb.toString();
	}

	public boolean isComposed() {
		return this.fragments.length > 1;
	}

	public boolean hasDuplication() {
		for (Fragment fragment : this.fragments) {
			if (fragment.duplicate) {
				return true;
			}
		}
		return false;
	}

	public Fragment getEnclosingFragment() {
		Fragment first = this.fragments[0];
		Fragment last = this.fragments[this.fragments.length - 1];
		return new Fragment(first.start, last.end, false, false);
	}

}
