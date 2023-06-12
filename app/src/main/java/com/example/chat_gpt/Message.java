package com.example.chat_gpt;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

public class Message implements IMessage, MessageContentType.Image {
    String id, text;
    IUser user;
    Date createdAt;
    String imageUrl;

    public Message(String id, String text, IUser user, Date createdAt, String imageUrl) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return user;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return imageUrl;
    }
}
