package br.ufmg.dcc.labsoft.jextract.ranking;


public class ExtractionSlice {

	private final Fragment[] fragments;

	public static ExtractionSlice fromString(String sourceFragments) {
		String[] frags = sourceFragments.split(";");
		Fragment[] fragments = new Fragment[frags.length];
		for (int i = 0; i < frags.length; i++) {
			String frag = frags[i];
			char typeFlag = frag.charAt(0);
			boolean duplicate = typeFlag == 'd' ? true : false;
			int middle = frag.indexOf(':');
			int start = Integer.parseInt(frag.substring(1, middle));
			int length = Integer.parseInt(frag.substring(middle + 1));
			
			fragments[i] = new Fragment(start, start + length, duplicate);
			
			// e5373:27;e5411:7;e5429:44;
		}
		return new ExtractionSlice(fragments);
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

	public Fragment[] getFragments() {
		return this.fragments;
	}

	public static class Fragment {
		public final int start;
		public final int end;
		public final boolean duplicate;
		public Fragment(int start, int end, boolean duplicate) {
			this.start = start;
			this.end = end;
			this.duplicate = duplicate;
		}
		public int length() {
			return this.end - this.start;
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
			sb.append(':');
			sb.append("" + (fragment.end - fragment.start));
			sb.append(';');
		}
		return sb.toString();
	}

	public boolean isComposed() {
		return this.fragments.length > 1;
	}
	
	public Fragment getEnclosingFragment() {
		Fragment first = this.fragments[0];
		Fragment last = this.fragments[this.fragments.length - 1];
		return new Fragment(first.start, last.end, false);
	}

}
