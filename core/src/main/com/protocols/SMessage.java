package com.protocols;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SMessage implements Serializable {

    private String          name;
    private SMessageType    type;
    private String          msg;
    private int             count;
    private ArrayList<User> users;
    private Status          status;
    private byte[]          voiceMsg;
    private byte[]          pictureMsg;
    private String          picture;
    private String          channel;
    private Peer            peer;

    public Peer getPeer() { return peer; }

    public void setPeer(Peer peer) { this.peer = peer; }

    public SMessage() {}

    public byte[] getPictureMsg() {
        return pictureMsg;
    }

    public void setPictureMsg(byte[] pictureMsg) {
        this.pictureMsg = pictureMsg;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public byte[] getVoiceMsg() {
        return voiceMsg;
    }

    public String getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() { return msg; }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SMessageType getType() {
        return type;
    }

    public void setType(SMessageType type) {
        this.type = type;
    }

    public ArrayList<User> getUserlist() {
        return users;
    }

    public void setUserlist(HashMap<String, User> userList) {
        this.users = new ArrayList<>(userList.values());
    }

    public void setOnlineCount(int count){
        this.count = count;
    }

    public int getOnlineCount(){
        return this.count;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setVoiceMsg(byte[] voiceMsg) {
        this.voiceMsg = voiceMsg;
    }
}
