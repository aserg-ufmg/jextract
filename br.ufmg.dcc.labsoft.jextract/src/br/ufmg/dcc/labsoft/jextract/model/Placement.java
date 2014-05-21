package br.ufmg.dcc.labsoft.jextract.model;

public enum Placement {
	BEFORE('-', false),
	MOVED_BEFORE('\u2191', true),
	INSIDE('X', false),
	MOVED_AFTER('\u2193', true),
	UNASSIGNED('-', false);

	private final boolean moved;
	private final char character;
	
	private Placement(char c, boolean moved) {
		this.character = c;
		this.moved = moved;
	}

	public boolean requiresReordering() {
		return this.moved;
	}

	public char getCharacter() {
		return this.character;
	}
	
}