package com.deepspring.transaction;

import java.util.ArrayDeque;
import java.util.Deque;

public class FakeConnection
{
	private final Deque<Runnable> undoLog = new ArrayDeque<>();

	public void execute(Runnable action, Runnable undo)
	{
		action.run();
		undoLog.push(undo);
	}

	public void commit()
	{
		undoLog.clear();
	}

	public void rollback()
	{
		while(!undoLog.isEmpty())
		{
			undoLog.pop().run();
		}
	}
}
