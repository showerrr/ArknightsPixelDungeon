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

package com.unifier.arknightspixeldungeon.sprites;

import com.unifier.arknightspixeldungeon.Assets;
import com.unifier.arknightspixeldungeon.Dungeon;
import com.unifier.arknightspixeldungeon.actors.Actor;
import com.unifier.arknightspixeldungeon.actors.Char;
import com.unifier.arknightspixeldungeon.items.weapon.missiles.Shuriken;
import com.unifier.arknightspixeldungeon.scenes.GameScene;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class TenguSprite extends MobSprite {
	
	private Animation cast;
	
	public TenguSprite() {
		super();
		
		texture( Assets.TENGU );
		
		TextureFilm frames = new TextureFilm( texture, 20, 16 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1 );
		
		run = new Animation( 15, false );
		run.frames( frames, 2, 3, 4, 5, 6, 7,8);
		
		attack = new Animation( 15, false );
		attack.frames( frames, 9, 10, 11, 12,13 );
		
		cast = attack.clone();
		
		die = new Animation( 8, false );
		die.frames( frames, 14,14,15,15,15 );
		
		play( run.clone() );
	}
	
	@Override
	public void move( int from, int to ) {
		
		place( to );
		
		play( run );
		turnTo( from , to );

		isMoving = true;

		if (Dungeon.level.water[to]) {
			GameScene.ripple( to );
		}

	}
	
	@Override
	public void attack( int cell ) {
		if (!Dungeon.level.adjacent( cell, ch.pos )) {

			final Char enemy = Actor.findChar(cell);

			((MissileSprite)parent.recycle( MissileSprite.class )).
				reset( ch.pos, cell, new Shuriken(), new Callback() {
					@Override
					public void call() {
						ch.next();
						if (enemy != null) ch.attack(enemy);
					}
				} );
			
			play( cast );
			turnTo( ch.pos , cell );
			
		} else {
			
			super.attack( cell );
			
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == run) {
			synchronized (this){
				isMoving = false;
				idle();

				notifyAll();
			}
		} else {
			super.onComplete( anim );
		}
	}
}
