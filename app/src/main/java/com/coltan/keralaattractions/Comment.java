package com.coltan.keralaattractions;

/**
 * Created by robo on 15/9/16.
 */

public class Comment {

    //Commenter info
    private String commenterName;
    private String commenterId;
    private String commenterPhotoUrl;

    //Comment info
    private String comment;
    private String date;
    private String millisecond;

    public Comment() {
    }

    public Comment(String commenterName, String commenterId, String commenterPhotoUrl, String comment, String date, String millisecond) {
        this.commenterName = commenterName;
        this.commenterId = commenterId;
        this.commenterPhotoUrl = commenterPhotoUrl;
        this.comment = comment;
        this.date = date;
        this.millisecond = millisecond;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    public String getCommenterPhotoUrl() {
        return commenterPhotoUrl;
    }

    public void setCommenterPhotoUrl(String commenterPhotoUrl) {
        this.commenterPhotoUrl = commenterPhotoUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMillisecond() {
        return millisecond;
    }

    public void setMillisecond(String millisecond) {
        this.millisecond = millisecond;
    }
}
