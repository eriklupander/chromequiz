package com.squeed.chromequiz.channel;

public class EventFactory {

	public static Event buildAnswerEvent(String answerLetter) {
		Event evt = new Event();
		evt.setId(EventDef.EVENT_ANSWER.name());
		evt.addParameter(ChannelDef.PRM_ANSWER, answerLetter);
		return evt;
	}
	
	public static Event buildNameResponseEvent(String castId, String name) {
		Event evt = new Event();
		evt.setId(EventDef.EVENT_NAME_REQUEST_RESPONSE.name());
		evt.addParameter(ChannelDef.PRM_CAST_ID, castId);
		evt.addParameter(ChannelDef.PRM_PLAYER_NAME, name);
		return evt;
	}

	public static Message buildStartGameEvent() {
		Event evt = new Event();
		evt.setId(EventDef.EVENT_START_GAME.name());
		return evt;
	}
}
