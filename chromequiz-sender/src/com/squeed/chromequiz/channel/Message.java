package com.squeed.chromequiz.channel;

import java.io.Serializable;
import java.util.HashMap;

public interface Message extends Serializable {

	String getId();

	HashMap<String,String> getParams();

}
