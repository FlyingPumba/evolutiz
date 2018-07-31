/*
 * Copyright (C) 2012 Rahul Agarwal
 *
 * This file is part of the World Clock
 * World Clock is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * World Clock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with World Clock.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.irahul.worldclock;
/**
 * Top level exception wrapper for app
 * @author rahul
 *
 */
@SuppressWarnings("serial")
public class WorldClockException extends RuntimeException {

	public WorldClockException() {
		super();
	}

	public WorldClockException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public WorldClockException(String detailMessage) {
		super(detailMessage);
	}

	public WorldClockException(Throwable throwable) {
		super(throwable);
	}
}
