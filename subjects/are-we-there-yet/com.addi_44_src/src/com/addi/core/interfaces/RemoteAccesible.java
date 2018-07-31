// Copyright (C) 2011 Free Software Foundation FSF
//
// This file is part of Addi.
//
// Addi is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or (at
// your option) any later version.
//
// Addi is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Addi. If not, see <http://www.gnu.org/licenses/>.

package com.addi.core.interfaces;

public interface RemoteAccesible
{
	/**Let the actual class call to the close method of the caller class.*/
	void close();

	/**Let the actual class call to the interpretLine method of the caller class.*/
	void interpretLine(String line);

//	/**Unused. Let the actual class call to the init method of the caller class.*/
//	public void init();
}
