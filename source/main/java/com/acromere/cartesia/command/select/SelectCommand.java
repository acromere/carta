package com.acromere.cartesia.command.select;

import com.acromere.cartesia.command.Command;

public abstract class SelectCommand extends Command {

	public enum Mode {
		POINT,
		WINDOW
	}

	@Override
	public boolean clearSelectionWhenComplete() {
		return false;
	}

	@Override
	public boolean clearReferenceAndPreviewWhenComplete() {
		return false;
	}

}
