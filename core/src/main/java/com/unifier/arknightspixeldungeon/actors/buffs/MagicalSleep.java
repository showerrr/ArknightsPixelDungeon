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

package com.unifier.arknightspixeldungeon.actors.buffs;

import com.unifier.arknightspixeldungeon.actors.Char;
import com.unifier.arknightspixeldungeon.actors.hero.Hero;
import com.unifier.arknightspixeldungeon.actors.mobs.Mob;
import com.unifier.arknightspixeldungeon.messages.Messages;
import com.unifier.arknightspixeldungeon.ui.BuffIndicator;
import com.unifier.arknightspixeldungeon.utils.GLog;

public class MagicalSleep extends Buff {

	private static final float STEP = 1f;

	@Override
	public boolean attachTo( Char target ) {
		if (!target.isImmune(Sleep.class) && super.attachTo( target )) {

			if (target instanceof Hero)
				if (target.HP == target.buff(Regeneration.class).regencap()) {
					GLog.i(Messages.get(this, "toohealthy"));
					detach();
					return true;
				} else {
					GLog.i(Messages.get(this, "fallasleep"));
				}
			else if (target instanceof Mob)
				((Mob)target).state = ((Mob)target).SLEEPING;

			target.paralysed++;

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean act(){
		if (target instanceof Mob && ((Mob) target).state != ((Mob) target).SLEEPING){
			detach();
			return true;
		}
		if (target instanceof Hero) {
			target.HP = Math.min(target.HP+1, target.HT);
			((Hero) target).resting = true;
			if (target.HP == target.buff(Regeneration.class).regencap()) {
				GLog.p(Messages.get(this, "wakeup"));
				detach();
			}
		}
		spend( STEP );
		return true;
	}

	@Override
	public void detach() {
		if (target.paralysed > 0)
			target.paralysed--;
		if (target instanceof Hero)
			((Hero) target).resting = false;
		super.detach();
	}

	@Override
	public int icon() {
		return BuffIndicator.MAGIC_SLEEP;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
}