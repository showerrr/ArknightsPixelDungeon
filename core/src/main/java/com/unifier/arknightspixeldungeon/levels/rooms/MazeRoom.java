/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2018 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.unifier.arknightspixeldungeon.levels.rooms;

import com.unifier.arknightspixeldungeon.levels.Level;
import com.unifier.arknightspixeldungeon.levels.Terrain;
import com.unifier.arknightspixeldungeon.levels.features.Maze;
import com.unifier.arknightspixeldungeon.levels.painters.Painter;

//TODO figure out where this should go, tunnel room type perhaps?
public class MazeRoom extends Room {

	public void paint( Level level ) {
		Painter.fill(level, this, 1, Terrain.EMPTY);

		//true = space, false = wall
		boolean[][] maze = Maze.generate(this);

		Painter.fill(level, this, 1, Terrain.EMPTY);
		for (int x = 0; x < maze.length; x++)
			for (int y = 0; y < maze[0].length; y++) {
				if (maze[x][y] == Maze.FILLED) {
					Painter.fill(level, x + left, y + top, 1, 1, Terrain.WALL);
				}
			}
	}

}
