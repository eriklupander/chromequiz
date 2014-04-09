package com.squeed.chromequiz.channel;

public interface ChannelDef {
	public static final String TYPE = "type";
	
	public static final String COMMAND_TYPE = "command";
	public static final String EVENT_TYPE = "event";
	public static final String RESPONSE_TYPE = "response";
	
	public static final String EVENT_ID = "eventId";
	public static final String RESPONSE_ID = "responseId";
	public static final String COMMAND_ID = "commandId";
	
	public static final String PRM_ANSWER = "answer";
	public static final String PRM_CAST_ID = "castId";
	public static final String PRM_PLAYER_NAME = "name";
	
	
}
