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

package com.unifier.arknightspixeldungeon.actors;



import com.unifier.arknightspixeldungeon.Dungeon;
import com.unifier.arknightspixeldungeon.Statistics;
import com.unifier.arknightspixeldungeon.actors.blobs.Blob;
import com.unifier.arknightspixeldungeon.actors.buffs.Buff;
import com.unifier.arknightspixeldungeon.actors.mobs.Mob;
import com.unifier.arknightspixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;

import java.util.HashSet;

public abstract class Actor implements Bundlable {
	
	public static final float TICK	= 1f;

	private float time;

	private int id = 0;

	//default priority values for general actor categories
	//note that some specific actors pick more specific values
	//e.g. a buff acting after all normal buffs might have priority BUFF_PRIO + 1
	protected static final int VFX_PRIO    = 100;   //visual effects take priority
	protected static final int HERO_PRIO   = 0;     //positive is before hero, negative after
	protected static final int BLOB_PRIO   = -10;   //blobs act after hero, before mobs
	protected static final int MOB_PRIO    = -20;   //mobs act between buffs and blobd
	protected static final int BUFF_PRIO   = -30;   //buffs act last in a turn
	private static final int   DEFAULT     = -100;  //if no priority is given, act after all else

	//used to determine what order actors act in if their time is equal. Higher values act earlier.
	protected int actPriority = DEFAULT;

	protected abstract boolean act();
	
	protected void spend( float time ) {
		this.time += time;
	}
	
	protected void postpone( float time ) {
		if (this.time < now + time) {
			this.time = now + time;
		}
	}
	
	public float cooldown() {
		return time - now;
	}
	
	protected void diactivate() {
		time = Float.MAX_VALUE;
	}
	
	protected void onAdd() {}
	
	protected void onRemove() {}

	private static final String TIME    = "time";
	private static final String ID      = "id";

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( TIME, time );
		bundle.put( ID, id );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		time = bundle.getFloat( TIME );
		id = bundle.getInt( ID );
	}

	private static int nextID = 1;
	public int id() {
		if (id > 0) {
			return id;
		} else {
			return (id = nextID++);
		}
	}

	// **********************
	// *** Static members ***
	
	private static HashSet<Actor> all = new HashSet<>();
	private static HashSet<Char> chars = new HashSet<>();
	private static volatile Actor current;

	private static SparseArray<Actor> ids = new SparseArray<>();

	private static float now = 0;
	
	public static synchronized void clear() {
		
		now = 0;

		all.clear();
		chars.clear();

		ids.clear();
	}
	
	public static synchronized void fixTime() {
		
		if (Dungeon.hero != null && all.contains( Dungeon.hero )) {
			Statistics.duration += now;
		}
		
		float min = Float.MAX_VALUE;
		for (Actor a : all) {
			if (a.time < min) {
				min = a.time;
			}
		}
		for (Actor a : all) {
			a.time -= min;
		}
		now = 0;
	}
	
	public static void init() {
		
		add( Dungeon.hero );
		
		for (Mob mob : Dungeon.level.mobs) {
			add( mob );
		}
		
		for (Blob blob : Dungeon.level.blobs.values()) {
			add( blob );
		}
		
		current = null;
	}

	private static final String NEXTID = "nextid";

	public static void storeNextID( Bundle bundle){
		bundle.put( NEXTID, nextID );
	}

	public static void restoreNextID( Bundle bundle){
		nextID = bundle.getInt( NEXTID );
	}

	public static void resetNextID(){
		nextID = 1;
	}

	/*protected*/public void next() {
		if (current == this) {
			current = null;
		}
	}

	public static boolean processing(){
		return current != null;
	}

    public static int curActorPriority() {
        return current != null ? current.actPriority : DEFAULT;
    }

    public static boolean keepActorThreadAlive = true;

	public static void process() {
		
		boolean doNext;
		boolean interrupted = false;

		do {
			
			current = null;
			if (!interrupted) {
				now = Float.MAX_VALUE;
				
				for (Actor actor : all) {
					
					//some actors will always go before others if time is equal.
					if (actor.time < now ||
							actor.time == now && (current == null || actor.actPriority > current.actPriority)) {
						now = actor.time;
						current = actor;
					}
					
				}
			}

			if  (current != null) {

				Actor acting = current;

				if (acting instanceof Char && ((Char) acting).sprite != null) {
					// If it's character's turn to act, but its sprite
					// is moving, wait till the movement is over
					try {
						synchronized (((Char)acting).sprite) {
							if (((Char)acting).sprite.isMoving) {
								((Char) acting).sprite.wait();
							}
						}
					} catch (InterruptedException e) {
						interrupted = true;
					}
				}
				
				interrupted = interrupted || Thread.interrupted();
				
				if (interrupted){
					doNext = false;
					current = null;
				} else {
					doNext = acting.act();
					if (doNext && (Dungeon.hero == null || !Dungeon.hero.isAlive())) {
						doNext = false;
						current = null;
					}
				}
			} else {
				doNext = false;
			}

			if (!doNext){
				synchronized (Thread.currentThread()) {
					
					interrupted = interrupted || Thread.interrupted();
					
					if (interrupted){
						current = null;
						interrupted = false;
					}
					
					synchronized (GameScene.class){
						//signals to the gamescene that actor processing is finished for now
						GameScene.class.notify();
					}
					
					try {
						Thread.currentThread().wait();
					} catch (InterruptedException e) {
						interrupted = true;
					}
				}
			}

		} while (keepActorThreadAlive);
	}
	
	public static void add( Actor actor ) {
		add( actor, now );
	}
	
	public static void addDelayed( Actor actor, float delay ) {
		add( actor, now + delay );
	}
	
	private static synchronized void add( Actor actor, float time ) {
		
		if (all.contains( actor )) {
			return;
		}

		ids.put( actor.id(),  actor );

		all.add( actor );
		actor.time += time;
		actor.onAdd();
		
		if (actor instanceof Char) {
			Char ch = (Char)actor;
			chars.add( ch );
			for (Buff buff : ch.buffs()) {
				all.add( buff );
				buff.onAdd();
			}
		}
	}
	
	public static synchronized void remove( Actor actor ) {
		
		if (actor != null) {
			all.remove( actor );
			chars.remove( actor );
			actor.onRemove();

			if (actor.id > 0) {
				ids.remove( actor.id );
			}
		}
	}
	
	public static synchronized Char findChar( int pos ) {
		for (Char ch : chars){
			if (ch.pos == pos)
				return ch;
		}
		return null;
	}

	public static synchronized Actor findById( int id ) {
		return ids.get( id );
	}

	public static synchronized HashSet<Actor> all() {
		return new HashSet<Actor>(all);
	}

	public static synchronized HashSet<Char> chars() { return new HashSet<Char>(chars); }
}
