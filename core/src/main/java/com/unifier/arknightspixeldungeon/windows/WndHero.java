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

package com.unifier.arknightspixeldungeon.windows;

import com.unifier.arknightspixeldungeon.Assets;
import com.unifier.arknightspixeldungeon.Dungeon;
import com.unifier.arknightspixeldungeon.PDSettings;
import com.unifier.arknightspixeldungeon.Statistics;
import com.unifier.arknightspixeldungeon.actors.buffs.Buff;
import com.unifier.arknightspixeldungeon.actors.hero.Hero;
import com.unifier.arknightspixeldungeon.actors.hero.HeroSubClass;
import com.unifier.arknightspixeldungeon.messages.Messages;
import com.unifier.arknightspixeldungeon.scenes.GameScene;
import com.unifier.arknightspixeldungeon.scenes.PixelScene;
import com.unifier.arknightspixeldungeon.sprites.HeroSprite;
import com.unifier.arknightspixeldungeon.ui.BuffIndicator;
import com.unifier.arknightspixeldungeon.ui.RenderedTextBlock;
import com.unifier.arknightspixeldungeon.ui.ScrollPane;
import com.unifier.arknightspixeldungeon.ui.TalentsPane;
import com.unifier.arknightspixeldungeon.ui.Window;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Locale;

import static com.unifier.arknightspixeldungeon.PDSettings.landscape;

public class WndHero extends WndTabbed {
	
	private static final int WIDTH		= 115;
	private static final int HEIGHT		= 160;
	
	private StatsTab stats;
    private TalentsTab talents;
	private BuffsTab buffs;
	
	private SmartTexture icons;
	private TextureFilm film;
	
	public WndHero() {
		
		super();

		int width = WIDTH;
		int height = HEIGHT;
        if(PDSettings.landscape() != null)
        {
            if(landscape())
            {
                width = 200;
                height = 140;
            }
        }

		resize( width, height );
		
		icons = TextureCache.get( Assets.BUFFS_LARGE );
		film = new TextureFilm( icons, 16, 16 );
		
		stats = new StatsTab();
		add( stats );

		talents = new TalentsTab();
		add(talents);
        talents.setRect(0, 0, width, height);


		buffs = new BuffsTab();
		add( buffs );
		buffs.setRect(0, 0, width, height);
		buffs.setupList();
		
		add( new LabeledTab( Messages.get(this, "stats") ) {
			protected void select( boolean value ) {
				super.select( value );
				talents.reset();
				stats.visible = stats.active = selected;
			};
		} );

        add( new LabeledTab( Messages.get(this, "talents") ) {
            protected void select( boolean value ) {
                super.select( value );
                talents.visible = talents.active = selected;
            };
        } );

		add( new LabeledTab( Messages.get(this, "buffs") ) {
			protected void select( boolean value ) {
				super.select( value );
                talents.reset();
				buffs.visible = buffs.active = selected;
			};
		} );

		layoutTabs();
		
		select( 0 );
	}
	
	private class StatsTab extends Group {
		
		private static final int GAP = 10;
		
		private float pos;
		
		public StatsTab() {
			
			Hero hero = Dungeon.hero;

			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar(hero.heroClass, hero.tier()) );

		//	if (hero.givenName().equals(hero.className()))
		//		title.label( Messages.get(this, "title", hero.lvl, hero.className() ).toUpperCase( Locale.ENGLISH ) );
		//	else
            if (hero.subClass == null || hero.subClass == HeroSubClass.NONE)
            {
                title.label((hero.operatorName() + "\n" + Messages.get(this, "title", hero.lvl)).toUpperCase(Locale.ENGLISH));
            }
            else {
                title.label((hero.operatorName() + "\n" + Messages.get(this, "titlewithmastery", hero.lvl, hero.className())).toUpperCase(Locale.ENGLISH));
            }
			title.color(Window.SHPX_COLOR);
			title.setRect( 0, 0, WIDTH, 0 );
			add(title);

			pos = title.bottom() + 2*GAP;

			statSlot( Messages.get(this, "str"), hero.STR() );
			if (hero.SHLD > 0) statSlot( Messages.get(this, "health"), hero.HP + "+" + hero.SHLD + "/" + hero.HT );
			else statSlot( Messages.get(this, "health"), (hero.HP) + "/" + hero.HT );
			statSlot( Messages.get(this, "exp"), hero.exp + "/" + hero.maxExp() );

			pos += GAP;

			statSlot( Messages.get(this, "gold"), Statistics.goldCollected );
			statSlot( Messages.get(this, "depth"), Statistics.deepestFloor );

			pos += GAP;
		}

		private void statSlot( String label, String value ) {
            RenderedTextBlock txt = PixelScene.renderTextBlock( label, 8 );
            txt.setPos(0, pos);
            add( txt );

            txt = PixelScene.renderTextBlock( value, 8 );
            txt.setPos(WIDTH * 0.6f, pos);
            PixelScene.align(txt);
            add( txt );
			
			pos += GAP + txt.height();
		}
		
		private void statSlot( String label, int value ) {
			statSlot( label, Integer.toString( value ) );
		}
		
		public float height() {
			return pos;
		}
	}

    public class TalentsTab extends Component {

        TalentsPane pane;

        @Override
        protected void createChildren() {
            super.createChildren();
            pane = new TalentsPane(true,Dungeon.hero.talents,Dungeon.hero.talentTiers());
            add(pane);
        }

        @Override
        protected void layout() {
            super.layout();
            pane.setRect(0, 0, width, height);
        }

        protected void reset(){
            pane.reset();
        }
    }
	
	private class BuffsTab extends Component {
		
		private static final int GAP = 2;
		
		private float pos;
		private ScrollPane buffList;
		private ArrayList<BuffSlot> slots = new ArrayList<>();
		
		public BuffsTab() {
			buffList = new ScrollPane( new Component() ){
				@Override
				public void onClick( float x, float y ) {
					int size = slots.size();
					for (int i=0; i < size; i++) {
						if (slots.get( i ).onClick( x, y )) {
							break;
						}
					}
				}
			};
			add(buffList);
		}
		
		@Override
		protected void layout() {
			super.layout();
			buffList.setRect(0, 0, width, height);
		}
		
		private void setupList() {
			Component content = buffList.content();
			for (Buff buff : Dungeon.hero.buffs()) {
				if (buff.icon() != BuffIndicator.NONE) {
					BuffSlot slot = new BuffSlot(buff);
					slot.setRect(0, pos, WIDTH, slot.icon.height());
					content.add(slot);
					slots.add(slot);
					pos += GAP + slot.height();
				}
			}
			content.setSize(buffList.width(), pos);
			buffList.setSize(buffList.width(), buffList.height());
		}

		private class BuffSlot extends Component {

			private Buff buff;

			Image icon;
			RenderedTextBlock txt;

			public BuffSlot( Buff buff ){
				super();
				this.buff = buff;
				int index = buff.icon();

				icon = new Image( icons );
				icon.frame( film.get( index ) );
				buff.tintIcon(icon);
				icon.y = this.y;
				add( icon );

				txt = PixelScene.renderTextBlock( buff.toString(), 8 );
                txt.setPos(
                        icon.width + GAP,
                        this.y + (icon.height - txt.height()) / 2
                );
                PixelScene.align(txt);
                add( txt );

			}

			@Override
			protected void layout() {
				super.layout();
				icon.y = this.y;
                txt.setPos(
                        icon.width + GAP,
                        this.y + (icon.height - txt.height()) / 2
                );
			}
			
			protected boolean onClick ( float x, float y ) {
				if (inside( x, y )) {
					GameScene.show(new WndInfoBuff(buff));
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
