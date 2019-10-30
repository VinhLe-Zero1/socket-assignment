package com.protocols;

//import org.json.simple.JSONObject;

import java.io.Serializable;

public class PMessage implements Serializable {
    private String sender;
    private String message;
    private String color;
    private byte[] file;
    private PMessageType type;

    public PMessage() {}

//    public PMessage(JSONObject jsonObject) {
//        this.sender = (String) jsonObject.get("sender");
//        this.message = (String) jsonObject.get("message");
//        this.color = (String) jsonObject.get("color");
//        this.file = (byte[]) jsonObject.get("file");
//        this.type = PMessageType.valueOf((String) jsonObject.get("type"));
//    }

    public PMessageType getType() { return type; }

    public byte[] getFile() { return file; }

    public String getColor() { return color; }

    public String getMessage() { return message; }

    public String getSender() { return sender; }

    public void setType(PMessageType type) { this.type = type; }

    public void setColor(String color) { this.color = color; }

    public void setFile(byte[] file) { this.file = file; }

    public void setMessage(String message) {
        if (message.length() > 50) {
            int len = message.length();
            String partA = "", partB = "";
            for (int i = 0; i < len; i += 50) {
                if (i > 0) {
                    partA = message.substring(0, i);
                    partB = message.substring(i + 1, len);
                    message = partA + "\n" + partB;
                }
            }
        }

        this.message = message;
    }

    public void setSender(String sender) { this.sender = sender; }

//    public JSONObject toJSON() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("type", type.toString());
//        jsonObject.put("sender", sender);
//        jsonObject.put("message", message);
//        jsonObject.put("color", color);
//        jsonObject.put("file", file);
//
//        return jsonObject;
//    }
}
