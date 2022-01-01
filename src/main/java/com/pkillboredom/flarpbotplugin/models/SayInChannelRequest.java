package com.pkillboredom.flarpbotplugin.models;

public class SayInChannelRequest {
    public SayInChannelRequest(String requestId, String guildId, String channelId, String message) {
        RequestId = requestId;
        GuildId = guildId;
        ChannelId = channelId;
        Message = message;
    }
    public String RequestId;
    public String GuildId;
    public String ChannelId;
    public String Message;
}
