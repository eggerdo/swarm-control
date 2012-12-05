package org.dobots.swarmcontrol;

public class SwarmControlTypes {

	public enum SwarmAction {
			
		sa_Dance	("Dance", 		true),
//		sa_Race		("Race", 		true),
		sa_Search	("Search", 		false),
		sa_March	("March", 		false),
		sa_Play		("Play", 		false),
		sa_Guard	("Guard", 		false),
		sa_FollowMe	("Follow Me", 	false),
		sa_FindExit	("Find Exit", 	false);
		
		String strName;
		// enabled means that the action is implemented and can be selected
		boolean bEnabled;
		
		private SwarmAction(String i_strName, boolean i_bEnabled) {
			this.strName = i_strName;
			this.bEnabled = i_bEnabled;
		}
		
		public String toString() {
			return strName;
		}
		
		public boolean isEnabled() {
			return bEnabled;
		}
	}

}
