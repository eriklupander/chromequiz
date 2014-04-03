package com.squeed.chromequiz.channel;

public class EventFactory {

	public static Event buildAnswerEvent(String answerLetter) {
		Event evt = new Event();
		evt.setId(EventDef.EVENT_ANSWER.name());
		evt.addParameter(ChannelDef.PRM_ANSWER, answerLetter);
		return evt;
	}
}
