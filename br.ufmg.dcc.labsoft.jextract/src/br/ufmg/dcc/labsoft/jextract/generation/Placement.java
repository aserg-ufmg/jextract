package br.ufmg.dcc.labsoft.jextract.generation;

enum Placement {
	BEFORE,
	MOVED_BEFORE(true),
	INSIDE,
	MOVED_AFTER(true),
	UNASSIGNED;

	private final boolean moved;
	
	private Placement(boolean moved) {
		this.moved = moved;
	}

	private Placement() {
		this(false);
	}

	public boolean requiresReordering() {
		return this.moved;
	}
	
}