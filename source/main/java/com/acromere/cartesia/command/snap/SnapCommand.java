package com.acromere.cartesia.command.snap;

import com.acromere.cartesia.command.Command;

public abstract class SnapCommand extends Command {

	@Override
	public boolean clearSelectionWhenComplete() {
		return false;
	}

	@Override
	public boolean clearReferenceAndPreviewWhenComplete() {
		return false;
	}

}
