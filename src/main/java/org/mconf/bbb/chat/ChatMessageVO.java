package org.mconf.bbb.chat;

import java.util.HashMap;
import java.util.Map;

public class ChatMessageVO {
    // The type of chat (PUBLIC or PRIVATE)
    public String chatType;

    // The sender
    public String fromUserID;
    public String fromUsername;
    public String fromColor;

    // Stores the UTC time (milliseconds) when the message was sent.
    public Double fromTime;
    // Stores the timezone offset (minutes) when the message was sent.
    // This will be used by receiver to convert to locale time.
    public Long fromTimezoneOffset;

    public String fromLang;

    // The receiver. For PUBLIC chat this is empty
    public String toUserID = "";
    public String toUsername = "";
    public String message;
    
    public void fromMap(Map<String, Object> msg) {
        this.chatType = msg.get("chatType").toString();
        this.fromUserID = msg.get("fromUserID").toString();
        this.fromUsername = msg.get("fromUsername").toString();
        this.fromColor = msg.get("fromColor").toString();
        this.fromTime = Double.valueOf(msg.get("fromTime").toString());
        this.fromTimezoneOffset = Double.valueOf(msg.get("fromTimezoneOffset").toString()).longValue();
        this.fromLang = msg.get("fromLang").toString();
        this.toUserID = msg.get("toUserID").toString();
        this.toUsername = msg.get("toUsername").toString();
        this.message = msg.get("message").toString();
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("fromUserID", fromUserID);
        msg.put("fromUsername", fromUsername);
        msg.put("fromColor", fromColor);
        msg.put("fromTime", fromTime);
        msg.put("fromLang", fromLang);
        msg.put("fromTime", fromTime);
        msg.put("fromTimezoneOffset", fromTimezoneOffset);
        msg.put("chatType", chatType);
        msg.put("message", message);
        msg.put("toUserID", toUserID);
        msg.put("toUsername", toUsername);

        return msg;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("chatType: ");
		builder.append(chatType);
		builder.append("\nfromUserID: ");
		builder.append(fromUserID);
		builder.append("\nfromUsername: ");
		builder.append(fromUsername);
		builder.append("\nfromColor: ");
		builder.append(fromColor);
		builder.append("\nfromTime: ");
		builder.append(fromTime);
		builder.append("\nfromTimezoneOffset: ");
		builder.append(fromTimezoneOffset);
		builder.append("\nfromLang: ");
		builder.append(fromLang);
		builder.append("\ntoUserID: ");
		builder.append(toUserID);
		builder.append("\ntoUsername: ");
		builder.append(toUsername);
		builder.append("\nmessage: ");
		builder.append(message);
		return builder.toString();
	}
}
