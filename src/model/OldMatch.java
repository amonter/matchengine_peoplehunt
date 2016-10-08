package model;

import java.util.Map;

public class OldMatch extends Match {

	public OldMatch(Profile us, Profile them, Map<Integer,String> what, int key, String which, int baseRank){
		super(us, them, what, key, which, baseRank);
	}
	
	//used to create a match where there might not be things in common (anymore)
	public OldMatch(Profile us, Profile them, int baseRank){
		super(us, them, baseRank);
	}
	
	@Override
	public int compareTo(Match o){
		long ourInteraction = us.getInteractionTime(them.id);
		long theirInteraction = us.getInteractionTime(o.them.id);
		
		if( ourInteraction != 0 && theirInteraction != 0 ){
			//we have two interaction times, compare them
			return (ourInteraction >= theirInteraction ) ? 1 : -1;
		}
		else if(ourInteraction != 0){
			return 1;
		}
		else if(theirInteraction != 0){
			return -1;
		}
		else{
			return 0;
		}
	}
	
}
